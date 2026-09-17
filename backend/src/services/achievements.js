const { pool } = require('../db');

async function checkAndUnlock(userId, conditionType, counterValue) {
  const defs = await pool.query('SELECT * FROM achievement_definitions WHERE condition_type = $1', [conditionType]);
  const unlockedNames = [];
  for (const def of defs.rows) {
    const existing = await pool.query(
      'SELECT * FROM user_achievement_progress WHERE user_id = $1 AND achievement_id = $2',
      [userId, def.id],
    );
    if (existing.rows[0]?.unlocked) continue;
    const nowUnlocked = counterValue >= def.threshold_value;
    await pool.query(
      `INSERT INTO user_achievement_progress (user_id, achievement_id, progress, unlocked, unlocked_at)
       VALUES ($1,$2,$3,$4,$5)
       ON CONFLICT (user_id, achievement_id) DO UPDATE SET progress = $3, unlocked = $4, unlocked_at = $5`,
      [userId, def.id, counterValue, nowUnlocked, nowUnlocked ? new Date() : null],
    );
    if (nowUnlocked) unlockedNames.push(def.name);
  }
  return unlockedNames;
}

module.exports = { checkAndUnlock };