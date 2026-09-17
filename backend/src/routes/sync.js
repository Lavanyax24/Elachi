const express = require('express');
const { pool } = require('../db');
const { requireAuth } = require('../middleware/auth');

const router = express.Router();
router.use(requireAuth);

router.post('/', async (req, res) => {
  const { lastSyncedAt, recipes = [], pantryItems = [] } = req.body;
  const userId = req.user.id;
  const syncStartedAt = new Date();
  const conflicts = [];

  for (const clientRecipe of recipes) {
    const existing = await pool.query('SELECT updated_at FROM recipes WHERE id = $1 AND owner_id = $2', [clientRecipe.id, userId]);
    const serverUpdatedAt = existing.rows[0]?.updated_at ? new Date(existing.rows[0].updated_at).getTime() : 0;
    if (clientRecipe.updatedAt <= serverUpdatedAt) {
      conflicts.push({ type: 'recipe', id: clientRecipe.id, reason: 'Server copy is newer, client change was discarded.' });
      continue;
    }
    await pool.query(
      `UPDATE recipes SET title = COALESCE($1, title), servings = COALESCE($2, servings),
         cook_time_minutes = COALESCE($3, cook_time_minutes), is_favourite = COALESCE($4, is_favourite),
         updated_at = now()
       WHERE id = $5 AND owner_id = $6`,
      [clientRecipe.title, clientRecipe.servings, clientRecipe.cookTimeMinutes, clientRecipe.isFavourite, clientRecipe.id, userId],
    );
  }

  for (const clientItem of pantryItems) {
    const existing = await pool.query('SELECT updated_at FROM pantry_items WHERE id = $1 AND user_id = $2', [clientItem.id, userId]);
    const serverUpdatedAt = existing.rows[0]?.updated_at ? new Date(existing.rows[0].updated_at).getTime() : 0;
    if (clientItem.updatedAt <= serverUpdatedAt) {
      conflicts.push({ type: 'pantryItem', id: clientItem.id, reason: 'Server copy is newer, client change was discarded.' });
      continue;
    }
    await pool.query(
      `INSERT INTO pantry_items (id, user_id, name, quantity, unit, updated_at)
       VALUES ($1, $2, $3, $4, $5, now())
       ON CONFLICT (id) DO UPDATE SET name = $3, quantity = $4, unit = $5, updated_at = now()`,
      [clientItem.id, userId, clientItem.name, clientItem.quantity, clientItem.unit],
    );
  }

  const since = lastSyncedAt ? new Date(lastSyncedAt) : new Date(0);
  const serverRecipeChanges = await pool.query('SELECT * FROM recipes WHERE owner_id = $1 AND updated_at > $2', [userId, since]);
  const serverPantryChanges = await pool.query('SELECT * FROM pantry_items WHERE user_id = $1 AND updated_at > $2', [userId, since]);

  res.json({
    syncedAt: syncStartedAt.toISOString(),
    conflicts,
    serverChanges: { recipes: serverRecipeChanges.rows, pantryItems: serverPantryChanges.rows },
  });
});

module.exports = router;