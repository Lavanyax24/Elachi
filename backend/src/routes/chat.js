// chat.js - AI Chef Assistant proxy.
// The Android app sends a message here; the backend forwards it to
// Cohere's chat API along with a cooking-only system prompt and the
// user's pantry contents, then returns the reply. The Cohere API key
// never leaves the server.

const express = require('express');
const { v4: uuidv4 } = require('uuid');
const { pool } = require('../db');
const { requireAuth } = require('../middleware/auth');

const router = express.Router();
router.use(requireAuth);

// Cohere endpoint and model
const COHERE_CHAT_URL = 'https://api.cohere.com/v2/chat';
const COHERE_MODEL = 'command-r-plus-08-2024';

// Base system prompt - restricts the assistant to cooking topics.
const BASE_SYSTEM_PROMPT = 'You are the Elachi AI Chef Assistant, a friendly cooking '
  + 'expert embedded in a recipe app. Only answer questions about recipes, '
  + 'ingredient substitutions, cooking techniques, nutrition, and meal ideas from '
  + 'available ingredients. If asked about anything unrelated to cooking, politely '
  + 'redirect the conversation back to cooking. Keep responses concise and '
  + 'practical — a home cook reading this on a phone screen while cooking.';

// Appends the user's current pantry items to the system prompt so the
// assistant can suggest recipes using what they already have.
async function buildSystemPrompt(userId) {
  let pantryContext = '\n\nThe user has not added anything to their pantry yet.';
  try {
    const pantry = await pool.query(
      'SELECT name, quantity, unit FROM pantry_items WHERE user_id = $1',
      [userId],
    );
    if (pantry.rows.length > 0) {
      const itemsList = pantry.rows.map((p) => p.quantity + p.unit + ' ' + p.name).join(', ');
      pantryContext = '\n\nThe user currently has these items in their pantry: ' + itemsList + '. When relevant, suggest recipes using what they have.';
    }
  } catch (e) {
    // Pantry lookup is best-effort - a failure here just means the assistant
    // doesn't have pantry context, not that the chat is broken.
  }
  return BASE_SYSTEM_PROMPT + pantryContext;
}

// In-memory conversation history. The durable log lives in Firestore
// (see logToFirestore below), satisfying the POE's NoSQL requirement.
const conversations = new Map();
const MAX_CONVERSATIONS = 500;

// POST /api/chat - takes a user message and optional conversationId,
// returns the assistant's reply.
router.post('/', async (req, res) => {
  const { message, conversationId } = req.body;
  if (!message || !message.trim()) {
    return res.status(400).json({ error: 'message is required.' });
  }
  if (!process.env.COHERE_API_KEY) {
    return res.status(503).json({ error: 'AI Chef Assistant is not configured. Set COHERE_API_KEY on the server.' });
  }

  const convId = conversationId || uuidv4();
  const history = conversations.get(convId) || [];

  try {
    // Build the full message list: system prompt + prior turns + new message
    const systemPrompt = await buildSystemPrompt(req.user.id);
    const messages = [
      { role: 'system', content: systemPrompt },
      ...history.map((h) => ({ role: h.role, content: h.text })),
      { role: 'user', content: message },
    ];

    const response = await fetch(COHERE_CHAT_URL, {
      method: 'POST',
      headers: {
        Authorization: 'Bearer ' + process.env.COHERE_API_KEY,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ model: COHERE_MODEL, messages }),
    });

    if (!response.ok) {
      const errText = await response.text();
      console.error('Cohere chat request failed:', response.status, errText);
      return res.status(502).json({ error: 'The AI Chef Assistant is unavailable right now. Please try again shortly.' });
    }

    const data = await response.json();

    // Cohere's response shape can differ slightly across models, so try
    // a couple of common paths before falling back.
    const reply = data?.message?.content?.find((c) => c.type === 'text')?.text
      || data?.message?.content?.[0]?.text
      || "Sorry, I couldn't come up with a reply to that — could you rephrase?";

    // Save this turn, keeping only the last 20 turns per conversation
    history.push({ role: 'user', text: message }, { role: 'assistant', text: reply });
    conversations.set(convId, history.slice(-20));

    // Cap the number of live conversations to avoid unbounded memory growth
    if (conversations.size > MAX_CONVERSATIONS) {
      const oldestKey = conversations.keys().next().value;
      conversations.delete(oldestKey);
    }

    // Fire-and-forget durable log; a failure here doesn't break the chat
    logToFirestore(req.user.id, convId, message, reply)
      .catch((e) => console.warn('Firestore chat log failed:', e.message));

    res.json({ conversationId: convId, reply });
  } catch (e) {
    console.error('Cohere request failed:', e.message);
    res.status(502).json({ error: 'The AI Chef Assistant is unavailable right now. Please try again shortly.' });
  }
});

// Writes a user/assistant message pair to Firestore's chatMessages
// collection. Skips silently if Firebase Admin isn't initialised.
async function logToFirestore(userId, conversationId, userMessage, assistantReply) {
  const admin = require('firebase-admin');
  if (!admin.apps || !admin.apps.length) return;
  const db = admin.firestore();
  const batch = db.batch();
  const col = db.collection('chatMessages');
  batch.set(col.doc(), { userId, conversationId, role: 'user', text: userMessage, createdAt: new Date() });
  batch.set(col.doc(), { userId, conversationId, role: 'assistant', text: assistantReply, createdAt: new Date() });
  await batch.commit();
}

module.exports = router;