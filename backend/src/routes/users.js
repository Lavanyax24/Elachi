const express = require('express');
const { pool } = require('../db');
const { requireAuth } = require('../middleware/auth');

const router = express.Router();

function generateFriendCode() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  return Array.from({ length: 6 }, () => chars[Math.floor(Math.random() * chars.length)]).join('');
}

router.post('/sync', async (req, res) => {
  const authHeader = req.headers.authorization || '';
  const token = authHeader.startsWith('Bearer ') ? authHeader.slice(7) : null;
  if (!token) return res.status(401).json({ error: 'Missing Authorization bearer token.' });

  const admin = require('firebase-admin');
  try {
    const decoded = await admin.auth().verifyIdToken(token);
    const { firebaseUid, email, firstName, surname } = req.body;

    if (decoded.uid !== firebaseUid) {
      return res.status(403).json({ error: 'Token does not match the supplied firebaseUid.' });
    }

    const existing = await pool.query('SELECT * FROM users WHERE firebase_uid = $1', [firebaseUid]);
    if (existing.rows.length > 0) {
      return res.json({
        userId: existing.rows[0].id,
        friendCode: existing.rows[0].friend_code,
        createdAt: existing.rows[0].created_at,
      });
    }

    const friendCode = generateFriendCode();
    const displayName = (firstName + ' ' + surname).trim();
    const result = await pool.query(
      'INSERT INTO users (firebase_uid, email, first_name, surname, display_name, friend_code) '
      + 'VALUES ($1, $2, $3, $4, $5, $6) RETURNING id, friend_code, created_at',
      [firebaseUid, email, firstName, surname, displayName, friendCode],
    );
    await pool.query('INSERT INTO streak_records (user_id) VALUES ($1) ON CONFLICT DO NOTHING', [result.rows[0].id]);
    await pool.query('INSERT INTO notification_preferences (user_id) VALUES ($1) ON CONFLICT DO NOTHING', [result.rows[0].id]);

    res.status(201).json({
      userId: result.rows[0].id,
      friendCode: result.rows[0].friend_code,
      createdAt: result.rows[0].created_at,
    });
  } catch (e) {
    console.error(e);
    res.status(401).json({ error: 'Invalid or expired token.' });
  }
});

router.get('/me', requireAuth, async (req, res) => {
  const u = req.user;
  res.json({
    id: u.id, displayName: u.display_name, bio: u.bio, avatarUrl: u.avatar_url,
    friendCode: u.friend_code, cookingInterests: u.cooking_interests, dietaryRestrictions: u.dietary_restrictions,
  });
});

router.patch('/me', requireAuth, async (req, res) => {
  const { displayName, bio, avatarUrl, cookingInterests, dietaryRestrictions } = req.body;
  const result = await pool.query(
    'UPDATE users SET '
    + 'display_name = COALESCE($1, display_name), '
    + 'bio = COALESCE($2, bio), '
    + 'avatar_url = COALESCE($3, avatar_url), '
    + 'cooking_interests = COALESCE($4, cooking_interests), '
    + 'dietary_restrictions = COALESCE($5, dietary_restrictions), '
    + 'updated_at = now() '
    + 'WHERE id = $6 RETURNING *',
    [displayName, bio, avatarUrl, cookingInterests, dietaryRestrictions, req.user.id],
  );
  const u = result.rows[0];
  res.json({
    id: u.id, displayName: u.display_name, bio: u.bio, avatarUrl: u.avatar_url,
    friendCode: u.friend_code, cookingInterests: u.cooking_interests, dietaryRestrictions: u.dietary_restrictions,
  });
});

router.get('/:id', requireAuth, async (req, res) => {
  const result = await pool.query('SELECT id, display_name, bio, avatar_url FROM users WHERE id = $1', [req.params.id]);
  if (result.rows.length === 0) return res.status(404).json({ error: 'User not found.' });
  const u = result.rows[0];
  res.json({ id: u.id, displayName: u.display_name, bio: u.bio, avatarUrl: u.avatar_url });
});

router.delete('/me', requireAuth, async (req, res) => {
  await pool.query('DELETE FROM users WHERE id = $1', [req.user.id]);
  res.status(204).send();
});

router.post('/me/notification-token', requireAuth, async (req, res) => {
  const { fcmToken } = req.body;
  if (!fcmToken) return res.status(400).json({ error: 'fcmToken is required.' });
  await pool.query(
    'INSERT INTO notification_tokens (user_id, fcm_token) VALUES ($1, $2) '
    + 'ON CONFLICT (fcm_token) DO UPDATE SET user_id = $1, updated_at = now()',
    [req.user.id, fcmToken],
  );
  res.status(204).send();
});

router.delete('/me/notification-token', requireAuth, async (req, res) => {
  const { fcmToken } = req.body;
  if (fcmToken) {
    await pool.query('DELETE FROM notification_tokens WHERE user_id = $1 AND fcm_token = $2', [req.user.id, fcmToken]);
  } else {
    await pool.query('DELETE FROM notification_tokens WHERE user_id = $1', [req.user.id]);
  }
  res.status(204).send();
});

router.get('/me/notification-preferences', requireAuth, async (req, res) => {
  const result = await pool.query('SELECT * FROM notification_preferences WHERE user_id = $1', [req.user.id]);
  const row = result.rows[0];
  res.json({
    commentNotifications: row?.comment_notifications ?? true,
    friendRequestNotifications: row?.friend_request_notifications ?? true,
    recipeShareNotifications: row?.recipe_share_notifications ?? true,
  });
});

router.patch('/me/notification-preferences', requireAuth, async (req, res) => {
  const { commentNotifications, friendRequestNotifications, recipeShareNotifications } = req.body;
  await pool.query(
    'INSERT INTO notification_preferences (user_id, comment_notifications, friend_request_notifications, recipe_share_notifications) '
    + 'VALUES ($1, $2, $3, $4) '
    + 'ON CONFLICT (user_id) DO UPDATE SET '
    + 'comment_notifications = COALESCE($2, notification_preferences.comment_notifications), '
    + 'friend_request_notifications = COALESCE($3, notification_preferences.friend_request_notifications), '
    + 'recipe_share_notifications = COALESCE($4, notification_preferences.recipe_share_notifications)',
    [req.user.id, commentNotifications, friendRequestNotifications, recipeShareNotifications],
  );
  res.status(204).send();
});

module.exports = router;