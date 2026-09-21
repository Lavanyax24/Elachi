// sync.js - Offline sync endpoint.
// Used when the Android app has been offline and the user is coming
// back online. The app sends any local changes made while offline,
// and receives any server-side changes made since its last sync.
// Conflict rule is last-write-wins: whichever side has the newer
// timestamp overwrites the other.

const express = require('express');
const { pool } = require('../db');
const { requireAuth } = require('../middleware/auth');

const router = express.Router();
router.use(requireAuth);

// POST /api/sync - main sync handler.
// Body: { lastSyncedAt, recipes: [], pantryItems: [] }
// Returns: { syncedAt, conflicts, serverChanges }
router.post('/', async (req, res) => {
  const { lastSyncedAt, recipes = [], pantryItems = [] } = req.body;
  const userId = req.user.id;
  const syncStartedAt = new Date();
  const conflicts = [];

  // Apply client-side recipe changes first, last-write-wins
  for (const clientRecipe of recipes) {
    // Look up the server's timestamp for this recipe
    const existing = await pool.query('SELECT updated_at FROM recipes WHERE id = $1 AND owner_id = $2', [clientRecipe.id, userId]);
    const serverUpdatedAt = existing.rows[0]?.updated_at ? new Date(existing.rows[0].updated_at).getTime() : 0;

    // If the server's copy is newer or equal, discard the client's change
    if (clientRecipe.updatedAt <= serverUpdatedAt) {
      conflicts.push({ type: 'recipe', id: clientRecipe.id, reason: 'Server copy is newer, client change was discarded.' });
      continue;
    }

    // Otherwise, apply the client's change
    await pool.query(
      `UPDATE recipes SET title = COALESCE($1, title), servings = COALESCE($2, servings),
         cook_time_minutes = COALESCE($3, cook_time_minutes), is_favourite = COALESCE($4, is_favourite),
         updated_at = now()
       WHERE id = $5 AND owner_id = $6`,
      [clientRecipe.title, clientRecipe.servings, clientRecipe.cookTimeMinutes, clientRecipe.isFavourite, clientRecipe.id, userId],
    );
  }

  // Apply client-side pantry changes, same conflict rule
  for (const clientItem of pantryItems) {
    const existing = await pool.query('SELECT updated_at FROM pantry_items WHERE id = $1 AND user_id = $2', [clientItem.id, userId]);
    const serverUpdatedAt = existing.rows[0]?.updated_at ? new Date(existing.rows[0].updated_at).getTime() : 0;

    if (clientItem.updatedAt <= serverUpdatedAt) {
      conflicts.push({ type: 'pantryItem', id: clientItem.id, reason: 'Server copy is newer, client change was discarded.' });
      continue;
    }

    // Upsert: insert if the id is new, otherwise update
    await pool.query(
      `INSERT INTO pantry_items (id, user_id, name, quantity, unit, updated_at)
       VALUES ($1, $2, $3, $4, $5, now())
       ON CONFLICT (id) DO UPDATE SET name = $3, quantity = $4, unit = $5, updated_at = now()`,
      [clientItem.id, userId, clientItem.name, clientItem.quantity, clientItem.unit],
    );
  }

  // Anything changed server-side since the client's last sync goes back
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