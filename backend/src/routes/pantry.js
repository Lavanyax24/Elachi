// pantry.js - Pantry and Shopping List endpoints.
// All routes require a valid Firebase token via requireAuth; every
// query is scoped to the authenticated user.

const express = require('express');
const { pool } = require('../db');
const { requireAuth } = require('../middleware/auth');
const { checkAndUnlock } = require('../services/achievements');

const router = express.Router();

// GET /api/pantry - all pantry items for the signed-in user, sorted by name.
router.get('/pantry', requireAuth, async (req, res) => {
  const result = await pool.query('SELECT * FROM pantry_items WHERE user_id = $1 ORDER BY name ASC', [req.user.id]);
  res.json(result.rows.map((i) => ({ id: i.id, name: i.name, quantity: Number(i.quantity), unit: i.unit })));
});

// POST /api/pantry - add a pantry item.
// Accepts an optional client-generated id so offline adds keep their UUID.
router.post('/pantry', requireAuth, async (req, res) => {
  const { id, name, quantity, unit } = req.body;
  const result = await pool.query(
    `INSERT INTO pantry_items (id, user_id, name, quantity, unit)
     VALUES (COALESCE($1::uuid, uuid_generate_v4()), $2, $3, $4, $5) RETURNING *`,
    [id || null, req.user.id, name, quantity, unit],
  );
  const i = result.rows[0];

  // Check whether this pushes the user past a "pantry items" badge threshold
  const totalItems = (await pool.query('SELECT COUNT(*) FROM pantry_items WHERE user_id = $1', [req.user.id])).rows[0].count;
  await checkAndUnlock(req.user.id, 'pantryItemsAdded', Number(totalItems));

  res.status(201).json({ id: i.id, name: i.name, quantity: Number(i.quantity), unit: i.unit });
});

// DELETE /api/pantry/:id - remove a pantry item owned by the signed-in user.
router.delete('/pantry/:id', requireAuth, async (req, res) => {
  await pool.query('DELETE FROM pantry_items WHERE id = $1 AND user_id = $2', [req.params.id, req.user.id]);
  res.status(204).send();
});

// GET /api/shopping-list - all shopping list items for the signed-in user.
router.get('/shopping-list', requireAuth, async (req, res) => {
  const result = await pool.query('SELECT * FROM shopping_list_items WHERE user_id = $1 ORDER BY created_at DESC', [req.user.id]);
  res.json(result.rows.map((i) => ({ id: i.id, name: i.name, quantity: Number(i.quantity), unit: i.unit, isBought: i.is_bought })));
});

// POST /api/shopping-list - add a single item manually.
router.post('/shopping-list', requireAuth, async (req, res) => {
  const { name, quantity, unit } = req.body;
  const result = await pool.query(
    'INSERT INTO shopping_list_items (user_id, name, quantity, unit) VALUES ($1,$2,$3,$4) RETURNING *',
    [req.user.id, name, quantity, unit],
  );
  const i = result.rows[0];
  res.status(201).json({ id: i.id, name: i.name, quantity: Number(i.quantity), unit: i.unit, isBought: false });
});

// POST /api/shopping-list/generate - takes a recipeId and adds any of its
// ingredients that are NOT already in the user's pantry to the shopping list.
// Matching is a simple case-insensitive name comparison.
router.post('/shopping-list/generate', requireAuth, async (req, res) => {
  const { recipeId } = req.body;
  const ingredients = await pool.query('SELECT name, quantity, unit FROM ingredients WHERE recipe_id = $1', [recipeId]);
  const pantry = await pool.query('SELECT LOWER(TRIM(name)) AS name FROM pantry_items WHERE user_id = $1', [req.user.id]);
  const pantryNames = new Set(pantry.rows.map((r) => r.name));

  const added = [];
  for (const ing of ingredients.rows) {
    // Skip if the ingredient is already in the pantry
    if (pantryNames.has(ing.name.trim().toLowerCase())) continue;

    const result = await pool.query(
      'INSERT INTO shopping_list_items (user_id, name, quantity, unit) VALUES ($1,$2,$3,$4) RETURNING *',
      [req.user.id, ing.name, ing.quantity, ing.unit],
    );
    const i = result.rows[0];
    added.push({ id: i.id, name: i.name, quantity: Number(i.quantity), unit: i.unit, isBought: false });
  }
  res.json(added);
});

// PATCH /api/shopping-list/:id - toggle an item's "bought" flag.
router.patch('/shopping-list/:id', requireAuth, async (req, res) => {
  const { isBought } = req.body;
  const result = await pool.query(
    'UPDATE shopping_list_items SET is_bought = $1 WHERE id = $2 AND user_id = $3 RETURNING *',
    [isBought, req.params.id, req.user.id],
  );
  if (result.rows.length === 0) return res.status(404).json({ error: 'Item not found.' });
  const i = result.rows[0];
  res.json({ id: i.id, name: i.name, quantity: Number(i.quantity), unit: i.unit, isBought: i.is_bought });
});

// DELETE /api/shopping-list/:id - remove a shopping list item.
router.delete('/shopping-list/:id', requireAuth, async (req, res) => {
  await pool.query('DELETE FROM shopping_list_items WHERE id = $1 AND user_id = $2', [req.params.id, req.user.id]);
  res.status(204).send();
});

module.exports = router;