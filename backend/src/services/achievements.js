// achievements.js
// This file holds the shared achievement checking logic.
// Other route files call checkAndUnlock after a user does something
// that could unlock one or more achievements (like cooking a recipe).

const { pool } = require('../db');

// checkAndUnlock(userId, conditionType, counterValue)
// - userId: the local user id whose achievements should be checked
// - conditionType: the kind of achievement to look at (e.g. 'recipes_cooked')
// - counterValue: the user's current count for that condition
// Returns an array of achievement names that were just unlocked by this call.
async function checkAndUnlock(userId, conditionType, counterValue) {
  // Get all achievement definitions that match this condition type.
  // Each definition has a threshold_value the counter must reach.
  const defs = await pool.query('SELECT * FROM achievement_definitions WHERE condition_type = $1', [conditionType]);
  const unlockedNames = [];

  for (const def of defs.rows) {
    // Check the user's current progress row for this achievement.
    const existing = await pool.query(
      'SELECT * FROM user_achievement_progress WHERE user_id = $1 AND achievement_id = $2',
      [userId, def.id],
    );
    // If it is already unlocked, skip it so we do not re-notify.
    if (existing.rows[0]?.unlocked) continue;

    // Unlock if the counter has reached or passed the threshold.
    const nowUnlocked = counterValue >= def.threshold_value;

    // Upsert the progress row with the latest count and unlock state.
    // unlocked_at is set only when it just unlocked, otherwise null.
    await pool.query(
      `INSERT INTO user_achievement_progress (user_id, achievement_id, progress, unlocked, unlocked_at)
       VALUES ($1,$2,$3,$4,$5)
       ON CONFLICT (user_id, achievement_id) DO UPDATE SET progress = $3, unlocked = $4, unlocked_at = $5`,
      [userId, def.id, counterValue, nowUnlocked, nowUnlocked ? new Date() : null],
    );

    // Track names so the caller can show a toast or send a notification.
    if (nowUnlocked) unlockedNames.push(def.name);
  }

  return unlockedNames;
}

module.exports = { checkAndUnlock };