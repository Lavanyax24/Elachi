const express = require('express');
const { pool } = require('../db');
const { requireAuth } = require('../middleware/auth');

const router = express.Router();
router.use(requireAuth);

function toBookDto(b) {
  return {
    id: b.id,
    name: b.name,
    description: b.description,
    coverImageUrl: b.cover_image_url,
    icon: b.icon,
    colour: b.colour,
    recipeCount: b.recipe_count !== undefined ? Number(b.recipe_count) : undefined,
  };
}

router.get('/', async (req, res) => {
  const result = await pool.query(
    `SELECT b.*, COUNT(r.id) AS recipe_count
     FROM recipe_books b LEFT JOIN recipes r ON r.book_id = b.id
     WHERE b.owner_id = $1 GROUP BY b.id ORDER BY b.created_at DESC`,
    [req.user.id],
  );
  res.json(result.rows.map(toBookDto));
});

// Accepts an optional client-generated id so the Android app can create a
// book offline with a local UUID and have the server keep that same id.
router.post('/', async (req, res) => {
  const { id, name, description, coverImageUrl, icon, colour } = req.body;
  const result = await pool.query(
    `INSERT INTO recipe_books (id, owner_id, name, description, cover_image_url, icon, colour)
     VALUES (COALESCE($1::uuid, uuid_generate_v4()), $2, $3, $4, $5, $6, $7) RETURNING *`,
    [id || null, req.user.id, name, description, coverImageUrl, icon, colour],
  );
  res.status(201).json({ ...toBookDto(result.rows[0]), recipeCount: 0 });
});

router.patch('/:id', async (req, res) => {
  const { name, description, coverImageUrl, icon, colour } = req.body;
  const result = await pool.query(
    `UPDATE recipe_books SET
       name = COALESCE($1, name), description = COALESCE($2, description),
       cover_image_url = COALESCE($3, cover_image_url), icon = COALESCE($4, icon),
       colour = COALESCE($5, colour)
     WHERE id = $6 AND owner_id = $7 RETURNING *`,
    [name, description, coverImageUrl, icon, colour, req.params.id, req.user.id],
  );
  if (result.rows.length === 0) return res.status(404).json({ error: 'Book not found.' });
  res.json(toBookDto(result.rows[0])); // camelCase, matches every other endpoint
});

router.delete('/:id', async (req, res) => {
  await pool.query('DELETE FROM recipe_books WHERE id = $1 AND owner_id = $2', [req.params.id, req.user.id]);
  res.status(204).send();
});

module.exports = router;