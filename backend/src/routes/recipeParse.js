const express = require('express');
const { requireAuth } = require('../middleware/auth');

const router = express.Router();
router.use(requireAuth);

const COHERE_CHAT_URL = 'https://api.cohere.com/v2/chat';
const COHERE_MODEL = 'command-r-plus-08-2024';

const PARSE_SYSTEM_PROMPT = 'You extract structured recipe data from raw, '
  + 'messy OCR text scanned from a recipe card, cookbook page, or website '
  + 'screenshot. The text may have broken lines, missing punctuation, or OCR '
  + 'errors — do your best to interpret it sensibly the way a human cook would.\n\n'
  + 'Generate a JSON object with EXACTLY this shape and nothing else:\n'
  + '{\n'
  + '  "title": "string",\n'
  + '  "ingredients": [ { "name": "string", "quantity": number, "unit": "string" } ],\n'
  + '  "steps": [ "string", "string" ],\n'
  + '  "method": "one of: Stovetop, Oven, Grill, Air Fryer, Slow Cooker, Pressure Cooker, Microwave, No-Cook, or your best guess",\n'
  + '  "servings": number,\n'
  + '  "cookTimeMinutes": number\n'
  + '}\n\n'
  + 'Rules:\n'
  + '- "quantity" must be a plain number (convert fractions like 1/2 to 0.5, and unicode fractions like ½ to 0.5). Use 0 if genuinely no quantity is given.\n'
  + '- "unit" should be a short lowercase unit like "g", "cup", "tbsp", "clove" — use "" if the ingredient has no unit (e.g. "2 eggs").\n'
  + '- "steps" should be the cooking instructions in order, one step per array entry, with any leading numbers removed.\n'
  + '- If "servings" or "cookTimeMinutes" cannot be determined, use 0.\n'
  + '- Never include markdown, explanation, or text outside the JSON object.';

router.post('/parse-text', async (req, res) => {
  const { rawText } = req.body;
  if (!rawText || !rawText.trim()) {
    return res.status(400).json({ error: 'rawText is required.' });
  }
  if (!process.env.COHERE_API_KEY) {
    return res.status(503).json({ error: 'Recipe parsing is not configured. Set COHERE_API_KEY on the server.' });
  }

  try {
    const response = await fetch(COHERE_CHAT_URL, {
      method: 'POST',
      headers: {
        Authorization: 'Bearer ' + process.env.COHERE_API_KEY,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        model: COHERE_MODEL,
        response_format: { type: 'json_object' },
        messages: [
          { role: 'system', content: PARSE_SYSTEM_PROMPT },
          { role: 'user', content: 'Generate a JSON object for this recipe text:\n\n' + rawText },
        ],
      }),
    });

    if (!response.ok) {
      const errText = await response.text();
      console.error('Cohere recipe-parse request failed:', response.status, errText);
      return res.status(502).json({ error: 'AI recipe parsing is unavailable right now.' });
    }

    const data = await response.json();
    const rawJsonText = data?.message?.content?.find((c) => c.type === 'text')?.text
      || data?.message?.content?.[0]?.text
      || '{}';

    let parsed;
    try {
      parsed = JSON.parse(rawJsonText);
    } catch (parseErr) {
      console.error('Cohere returned non-JSON for recipe parse:', rawJsonText);
      return res.status(502).json({ error: 'AI recipe parsing returned an unexpected format.' });
    }

    const normalised = {
      title: typeof parsed.title === 'string' ? parsed.title : '',
      ingredients: Array.isArray(parsed.ingredients)
        ? parsed.ingredients.map((i) => ({
          name: String(i?.name ?? ''),
          quantity: Number(i?.quantity) || 0,
          unit: String(i?.unit ?? ''),
        })).filter((i) => i.name.trim() !== '')
        : [],
      steps: Array.isArray(parsed.steps) ? parsed.steps.map((s) => String(s)).filter((s) => s.trim() !== '') : [],
      method: typeof parsed.method === 'string' ? parsed.method : '',
      servings: Number(parsed.servings) || 0,
      cookTimeMinutes: Number(parsed.cookTimeMinutes) || 0,
    };

    res.json(normalised);
  } catch (e) {
    console.error('Recipe parse request failed:', e.message);
    res.status(502).json({ error: 'AI recipe parsing is unavailable right now.' });
  }
});

module.exports = router;