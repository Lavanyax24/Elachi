const express = require('express');
const { v4: uuidv4 } = require('uuid');
const { pool } = require('../db');
const { requireAuth } = require('../middleware/auth');

const router = express.Router();
router.use(requireAuth);

const COHERE_CHAT_URL = 'https://api.cohere.com/v2/chat';
const COHERE_MODEL = 'command-r-plus-08-2024';

const BASE_SYSTEM_PROMPT = 'You are the Elachi AI Chef Assistant, a friendly cooking '
  + 'expert embedded in a recipe app. Only answer questions about recipes, '
  + 'ingredient substitutions, cooking techniques, nutrition, and meal ideas from '
  + 'available ingredients. If asked about anything unrelated to cooking, politely '
  + 'redirect the conversation back to cooking. Keep responses concise and '
  + 'practical — a home cook reading this on a phone screen while cooking.';

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
    // Pantry lookup is best-effort — a failure here just means the assistant
    // doesn't have pantry context, not that the chat is broken.
  }
  return BASE_SYSTEM_PROMPT + pantryContext;
}

// In-memory conversation store. Fine for a prototype; the durable log lives
// in Firestore (see logToFirestore below), satisfying the POE's NoSQL
// requirement.
const conversations = new Map();
const MAX_CONVERSATIONS = 500;

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
    const reply = data?.message?.content?.find((c) => c.type === 'text')?.text
      || data?.message?.content?.[0]?.text
      || "Sorry, I couldn't come up with a reply to that — could you rephrase?";

    history.push({ role: 'user', text: message }, { role: 'assistant', text: reply });
    conversations.set(convId, history.slice(-20));

    if (conversations.size > MAX_CONVERSATIONS) {
      const oldestKey = conversations.keys().next().value;
      conversations.delete(oldestKey);
    }

    logToFirestore(req.user.id, convId, message, reply)
      .catch((e) => console.warn('Firestore chat log failed:', e.message));

    res.json({ conversationId: convId, reply });
  } catch (e) {
    console.error('Cohere request failed:', e.message);
    res.status(502).json({ error: 'The AI Chef Assistant is unavailable right now. Please try again shortly.' });
  }
});

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