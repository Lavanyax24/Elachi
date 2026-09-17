const express = require('express');
const { pool } = require('../db');
const { requireAuth } = require('../middleware/auth');
const { checkAndUnlock } = require('../services/achievements');

const router = express.Router();

router.post('/cook-sessions', requireAuth, async (req, res) => {
  const { recipeId, completedAt } = req.body;
  const userId = req.user.id;

  const sessionResult = await pool.query(
    'INSERT INTO cook_sessions (user_id, recipe_id, completed_at) VALUES ($1,$2,$3) RETURNING id',
    [userId, recipeId, completedAt || new Date()],
  );
  await pool.query('UPDATE recipes SET times_cooked = times_cooked + 1 WHERE id = $1', [recipeId]);

  const streakRow = (await pool.query('SELECT * FROM streak_records WHERE user_id = $1', [userId])).rows[0];
  const today = new Date().toISOString().slice(0, 10);
  const yesterday = new Date(Date.now() - 86400000).toISOString().slice(0, 10);
  const lastDate = streakRow?.last_cooked_date ? new Date(streakRow.last_cooked_date).toISOString().slice(0, 10) : null;

  let newStreak;
  if (lastDate === today) newStreak = streakRow.current_streak;
  else if (lastDate === yesterday) newStreak = (streakRow?.current_streak || 0) + 1;
  else newStreak = 1;

  const longest = Math.max(newStreak, streakRow?.longest_streak || 0);
  await pool.query(
    `INSERT INTO streak_records (user_id, current_streak, longest_streak, last_cooked_date)
     VALUES ($1,$2,$3,$4)
     ON CONFLICT (user_id) DO UPDATE SET current_streak = $2, longest_streak = $3, last_cooked_date = $4`,
    [userId, newStreak, longest, today],
  );

  const totalCooked = (await pool.query('SELECT COUNT(*) FROM cook_sessions WHERE user_id = $1', [userId])).rows[0].count;
  const uniqueCooked = (await pool.query('SELECT COUNT(DISTINCT recipe_id) FROM cook_sessions WHERE user_id = $1', [userId])).rows[0].count;

  const unlocked = [];
  unlocked.push(...(await checkAndUnlock(userId, 'recipesCooked', Number(totalCooked))));
  unlocked.push(...(await checkAndUnlock(userId, 'streakDays', newStreak)));
  unlocked.push(...(await checkAndUnlock(userId, 'uniqueRecipesCooked', Number(uniqueCooked))));

  res.status(201).json({ cookSessionId: sessionResult.rows[0].id, updatedStreak: newStreak, unlockedAchievements: unlocked });
});

router.get('/achievements', requireAuth, async (req, res) => {
  const defs = await pool.query('SELECT * FROM achievement_definitions');
  const progress = await pool.query('SELECT * FROM user_achievement_progress WHERE user_id = $1', [req.user.id]);
  const progressByAchievement = Object.fromEntries(progress.rows.map((p) => [p.achievement_id, p]));

  res.json(defs.rows.map((def) => {
    const p = progressByAchievement[def.id];
    return {
      achievement: {
        id: def.id, name: def.name, description: def.description, category: def.category,
        conditionType: def.condition_type, thresholdValue: def.threshold_value, iconUrl: def.icon_url,
      },
      progress: p?.progress || 0,
      unlocked: p?.unlocked || false,
      unlockedAt: p?.unlocked_at || null,
    };
  }));
});

router.get('/streaks', requireAuth, async (req, res) => {
  const streak = (await pool.query('SELECT * FROM streak_records WHERE user_id = $1', [req.user.id])).rows[0];
  const sessions = await pool.query(
    "SELECT DATE(completed_at) AS date FROM cook_sessions WHERE user_id = $1 ORDER BY completed_at DESC LIMIT 60",
    [req.user.id],
  );
  res.json({
    currentStreak: streak?.current_streak || 0,
    longestStreak: streak?.longest_streak || 0,
    calendar: sessions.rows.map((r) => ({ date: r.date, activityType: 'cooking' })),
  });
});

module.exports = router;