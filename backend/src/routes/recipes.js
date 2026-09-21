const express = require('express');
const { pool } = require('../db');
const { requireAuth } = require('../middleware/auth');
const { sendPushToUser } = require('../services/notifications');
const { checkAndUnlock } = require('../services/achievements');

const router = express.Router();
router.use(requireAuth);

async function loadFullRecipe(recipeId) {
  const recipeResult = await pool.query('SELECT * FROM recipes WHERE id = $1', [recipeId]);
  if (recipeResult.rows.length === 0) return null;
  const r = recipeResult.rows[0];

  const ingredientsResult = await pool.query(
    'SELECT name, quantity, unit FROM ingredients WHERE recipe_id = $1 ORDER BY sort_order ASC',
    [recipeId],
  );
  const stepsResult = await pool.query(
    'SELECT "order", instruction, timer_seconds FROM steps WHERE recipe_id = $1 ORDER BY "order" ASC',
    [recipeId],
  );

  return {
    id: r.id, bookId: r.book_id, title: r.title, category: r.category, cuisine: r.cuisine,
    foodType: r.food_type, difficulty: r.difficulty, servings: r.servings,
    cookTimeMinutes: r.cook_time_minutes, method: r.method,
    ingredients: ingredientsResult.rows.map((i) => ({ name: i.name, quantity: Number(i.quantity), unit: i.unit })),
    steps: stepsResult.rows.map((s) => ({ order: s.order, instruction: s.instruction, timerSeconds: s.timer_seconds })),
    allergens: r.allergens, isPrivate: r.is_private,
    timesCooked: r.times_cooked,
    imageUrl: r.image_url,
  };
}

router.get('/', async (req, res) => {
  const { bookId, search } = req.query;
  let query = 'SELECT id FROM recipes WHERE owner_id = $1';
  const params = [req.user.id];

  if (bookId) { params.push(bookId); query += ' AND book_id = $' + params.length; }
  if (search) { params.push('%' + search + '%'); query += ' AND title ILIKE $' + params.length; }
  query += ' ORDER BY created_at DESC';

  const ids = await pool.query(query, params);
  const recipes = await Promise.all(ids.rows.map((row) => loadFullRecipe(row.id)));
  res.json(recipes);
});

// GET /api/recipes/discover?mode=global|personalised&search=&cuisine=
router.get('/discover', async (req, res) => {
  const { mode = 'global', search, cuisine } = req.query;
  const params = [];
  let whereClauses = ['r.is_private = false'];

  if (mode === 'personalised') {
    const interests = req.user.cooking_interests || [];
    if (interests.length > 0) {
      params.push(interests);
      whereClauses.push('(r.cuisine = ANY($' + params.length + ') OR r.category = ANY($' + params.length + '))');
    }
  }
  if (search) {
    params.push('%' + search + '%');
    whereClauses.push('r.title ILIKE $' + params.length);
  }
  if (cuisine) {
    params.push(cuisine);
    whereClauses.push('r.cuisine = $' + params.length);
  }

  const query = 'SELECT r.id, r.title, r.image_url, r.cook_time_minutes, r.difficulty, r.cuisine, '
    + 'r.times_cooked, u.id AS creator_id, u.display_name AS creator_name, '
    + 'COALESCE(AVG(c.rating), 0) AS avg_rating, COUNT(DISTINCT c.id) AS rating_count '
    + 'FROM recipes r '
    + 'JOIN users u ON u.id = r.owner_id '
    + 'LEFT JOIN comments c ON c.recipe_id = r.id AND c.rating IS NOT NULL '
    + 'WHERE ' + whereClauses.join(' AND ') + ' '
    + 'GROUP BY r.id, u.id '
    + 'ORDER BY r.created_at DESC '
    + 'LIMIT 50';

  const result = await pool.query(query, params);
  res.json(result.rows.map((row) => ({
    id: row.id, title: row.title, imageUrl: row.image_url, cookTimeMinutes: row.cook_time_minutes,
    difficulty: row.difficulty, cuisine: row.cuisine, timesCooked: row.times_cooked,
    creatorId: row.creator_id, creatorName: row.creator_name,
    avgRating: Number(row.avg_rating), ratingCount: Number(row.rating_count),
  })));
});

router.get('/suggestions', async (req, res) => {
  const recipesResult = await pool.query('SELECT id FROM recipes WHERE owner_id = $1', [req.user.id]);
  const pantryResult = await pool.query('SELECT LOWER(TRIM(name)) AS name FROM pantry_items WHERE user_id = $1', [req.user.id]);
  const pantryNames = new Set(pantryResult.rows.map((r) => r.name));

  const suggestions = [];
  for (const row of recipesResult.rows) {
    const recipe = await loadFullRecipe(row.id);
    if (!recipe.ingredients.length) continue;
    const haveCount = recipe.ingredients.filter((i) => pantryNames.has(i.name.trim().toLowerCase())).length;
    suggestions.push({ recipe, matchPercent: (haveCount / recipe.ingredients.length) * 100 });
  }
  suggestions.sort((a, b) => b.matchPercent - a.matchPercent);
  res.json(suggestions);
});

router.get('/pantry-health', async (req, res) => {
  const recipesResult = await pool.query('SELECT id FROM recipes WHERE owner_id = $1', [req.user.id]);
  if (recipesResult.rows.length === 0) return res.json({ pantryHealthPercent: 0 });

  const pantryResult = await pool.query('SELECT LOWER(TRIM(name)) AS name FROM pantry_items WHERE user_id = $1', [req.user.id]);
  const pantryNames = new Set(pantryResult.rows.map((r) => r.name));

  const percents = [];
  for (const row of recipesResult.rows) {
    const recipe = await loadFullRecipe(row.id);
    if (!recipe.ingredients.length) continue;
    const haveCount = recipe.ingredients.filter((i) => pantryNames.has(i.name.trim().toLowerCase())).length;
    percents.push((haveCount / recipe.ingredients.length) * 100);
  }
  const pantryHealthPercent = percents.length === 0
    ? 0
    : Math.round(percents.reduce((a, b) => a + b, 0) / percents.length);

  res.json({ pantryHealthPercent });
});

router.get('/:id', async (req, res) => {
  const recipe = await loadFullRecipe(req.params.id);
  if (!recipe) return res.status(404).json({ error: 'Recipe not found.' });
  res.json(recipe);
});

router.post('/', async (req, res) => {
  const {
    id, bookId, title, category, cuisine, foodType, difficulty, servings,
    cookTimeMinutes, method, ingredients, steps, allergens, isPrivate, imageUrl,
  } = req.body;

  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const recipeResult = await client.query(
      'INSERT INTO recipes (id, owner_id, book_id, title, category, cuisine, food_type, difficulty, '
      + 'servings, cook_time_minutes, method, allergens, is_private, image_url) '
      + 'VALUES (COALESCE($1::uuid, uuid_generate_v4()),$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14) RETURNING id',
      [id || null, req.user.id, bookId, title, category, cuisine, foodType, difficulty, servings, cookTimeMinutes, method, allergens, isPrivate, imageUrl || null],
    );
    const recipeId = recipeResult.rows[0].id;

    for (let i = 0; i < (ingredients || []).length; i++) {
      const ing = ingredients[i];
      await client.query(
        'INSERT INTO ingredients (recipe_id, name, quantity, unit, sort_order) VALUES ($1,$2,$3,$4,$5)',
        [recipeId, ing.name, ing.quantity, ing.unit, i],
      );
    }
    for (const step of steps || []) {
      await client.query(
        'INSERT INTO steps (recipe_id, "order", instruction, timer_seconds) VALUES ($1,$2,$3,$4)',
        [recipeId, step.order, step.instruction, step.timerSeconds || null],
      );
    }

    await client.query('COMMIT');

    const totalRecipes = (await pool.query('SELECT COUNT(*) FROM recipes WHERE owner_id = $1', [req.user.id])).rows[0].count;
    await checkAndUnlock(req.user.id, 'recipesAdded', Number(totalRecipes));

    res.status(201).json(await loadFullRecipe(recipeId));
  } catch (e) {
    await client.query('ROLLBACK');
    console.error(e);
    res.status(500).json({ error: 'Failed to save recipe.' });
  } finally {
    client.release();
  }
});

// PATCH /api/recipes/:id — partial update. Any subset of fields may be
// supplied; only the fields present in the body are changed. If `ingredients`
// or `steps` are supplied, they replace the entire existing list for that
// recipe (simplest, matches how the Add Recipe form submits a complete list).
router.patch('/:id', async (req, res) => {
  const {
    title, category, cuisine, foodType, difficulty, servings,
    cookTimeMinutes, method, allergens, isPrivate, imageUrl, bookId,
    ingredients, steps,
  } = req.body;

  const client = await pool.connect();
  try {
    await client.query('BEGIN');

    // Verify the recipe exists and belongs to the caller before touching it.
    const existing = await client.query(
      'SELECT id FROM recipes WHERE id = $1 AND owner_id = $2',
      [req.params.id, req.user.id],
    );
    if (existing.rows.length === 0) {
      await client.query('ROLLBACK');
      return res.status(404).json({ error: 'Recipe not found.' });
    }

    // Update the recipe row. COALESCE leaves untouched fields at their
    // current values, so a request that only sends { "isPrivate": false }
    // flips visibility without wiping out title/cuisine/etc.
    await client.query(
      'UPDATE recipes SET '
      + 'title = COALESCE($1, title), '
      + 'category = COALESCE($2, category), '
      + 'cuisine = COALESCE($3, cuisine), '
      + 'food_type = COALESCE($4, food_type), '
      + 'difficulty = COALESCE($5, difficulty), '
      + 'servings = COALESCE($6, servings), '
      + 'cook_time_minutes = COALESCE($7, cook_time_minutes), '
      + 'method = COALESCE($8, method), '
      + 'allergens = COALESCE($9, allergens), '
      + 'is_private = COALESCE($10, is_private), '
      + 'image_url = COALESCE($11, image_url), '
      + 'book_id = COALESCE($12, book_id), '
      + 'updated_at = now() '
      + 'WHERE id = $13 AND owner_id = $14',
      [
        title, category, cuisine, foodType, difficulty, servings,
        cookTimeMinutes, method, allergens, isPrivate, imageUrl, bookId,
        req.params.id, req.user.id,
      ],
    );

    // If the caller supplied a full ingredient list, replace the old one.
    if (Array.isArray(ingredients)) {
      await client.query('DELETE FROM ingredients WHERE recipe_id = $1', [req.params.id]);
      for (let i = 0; i < ingredients.length; i++) {
        const ing = ingredients[i];
        await client.query(
          'INSERT INTO ingredients (recipe_id, name, quantity, unit, sort_order) VALUES ($1,$2,$3,$4,$5)',
          [req.params.id, ing.name, ing.quantity, ing.unit, i],
        );
      }
    }

    // Same for steps.
    if (Array.isArray(steps)) {
      await client.query('DELETE FROM steps WHERE recipe_id = $1', [req.params.id]);
      for (const step of steps) {
        await client.query(
          'INSERT INTO steps (recipe_id, "order", instruction, timer_seconds) VALUES ($1,$2,$3,$4)',
          [req.params.id, step.order, step.instruction, step.timerSeconds || null],
        );
      }
    }

    await client.query('COMMIT');
    res.json(await loadFullRecipe(req.params.id));
  } catch (e) {
    await client.query('ROLLBACK');
    console.error(e);
    res.status(500).json({ error: 'Failed to update recipe.' });
  } finally {
    client.release();
  }
});

router.delete('/:id', async (req, res) => {
  await pool.query('DELETE FROM recipes WHERE id = $1 AND owner_id = $2', [req.params.id, req.user.id]);
  res.status(204).send();
});

router.get('/:id/comments', async (req, res) => {
  const result = await pool.query(
    'SELECT c.id, c.rating, c.text, c.created_at, u.display_name FROM comments c '
    + 'JOIN users u ON u.id = c.user_id WHERE c.recipe_id = $1 ORDER BY c.created_at DESC',
    [req.params.id],
  );
  res.json(result.rows.map((row) => ({
    id: row.id, rating: row.rating, text: row.text,
    createdAt: row.created_at, displayName: row.display_name,
  })));
});

router.post('/:id/comments', async (req, res) => {
  const { rating, text } = req.body;
  const result = await pool.query(
    'INSERT INTO comments (recipe_id, user_id, rating, text) VALUES ($1,$2,$3,$4) RETURNING *',
    [req.params.id, req.user.id, rating, text],
  );

  const recipeOwner = await pool.query('SELECT owner_id, title FROM recipes WHERE id = $1', [req.params.id]);
  if (recipeOwner.rows.length > 0 && recipeOwner.rows[0].owner_id !== req.user.id) {
    sendPushToUser(recipeOwner.rows[0].owner_id, {
      title: 'New Comment',
      body: req.user.display_name + ' commented on ' + recipeOwner.rows[0].title + '.',
    }, 'comment_notifications').catch((e) => console.warn('Push notification failed:', e.message));
  }

  res.status(201).json({
    id: result.rows[0].id, rating: result.rows[0].rating, text: result.rows[0].text,
    createdAt: result.rows[0].created_at, displayName: req.user.display_name,
  });
});

module.exports = router;