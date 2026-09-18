const admin = require('firebase-admin');
const { pool } = require('../db');

async function sendPushToUser(userId, notification, preferenceColumn) {
  if (!admin.apps || !admin.apps.length) return;

  const prefsResult = await pool.query(
    'SELECT ' + preferenceColumn + ' AS enabled FROM notification_preferences WHERE user_id = $1',
    [userId],
  );
  if (prefsResult.rows.length > 0 && prefsResult.rows[0].enabled === false) return;

  const tokensResult = await pool.query('SELECT fcm_token FROM notification_tokens WHERE user_id = $1', [userId]);
  if (tokensResult.rows.length === 0) return;

  const tokens = tokensResult.rows.map((r) => r.fcm_token);
  try {
    const response = await admin.messaging().sendEachForMulticast({
      tokens,
      notification,
    });
    const staleTokens = [];
    response.responses.forEach((r, i) => {
      if (!r.success && ['messaging/registration-token-not-registered', 'messaging/invalid-registration-token'].includes(r.error?.code)) {
        staleTokens.push(tokens[i]);
      }
    });
    if (staleTokens.length > 0) {
      await pool.query('DELETE FROM notification_tokens WHERE fcm_token = ANY($1)', [staleTokens]);
    }
  } catch (e) {
    console.warn('Push send failed:', e.message);
  }
}

module.exports = { sendPushToUser };