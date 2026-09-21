// pushNotifications.js
// This file contains the shared helper for sending push notifications
// to a single user through Firebase Cloud Messaging (FCM).
// It respects the user's notification preferences and cleans up
// any stale device tokens that FCM reports as no longer valid.

const admin = require('firebase-admin');
const { pool } = require('../db');

// sendPushToUser(userId, notification, preferenceColumn)
// - userId: the local user id to send the push to
// - notification: the FCM notification payload (title, body, etc.)
// - preferenceColumn: which notification_preferences column to check
//   (e.g. 'comment_notifications') so we can skip if the user opted out.
// Returns nothing. Failures are logged but not thrown.
async function sendPushToUser(userId, notification, preferenceColumn) {
  // If Firebase Admin was never initialized, do nothing.
  if (!admin.apps || !admin.apps.length) return;

  // Check the user's preference for this notification type.
  // If the column is explicitly false, skip sending.
  const prefsResult = await pool.query(
    'SELECT ' + preferenceColumn + ' AS enabled FROM notification_preferences WHERE user_id = $1',
    [userId],
  );
  if (prefsResult.rows.length > 0 && prefsResult.rows[0].enabled === false) return;

  // Load all FCM tokens registered for this user.
  const tokensResult = await pool.query('SELECT fcm_token FROM notification_tokens WHERE user_id = $1', [userId]);
  if (tokensResult.rows.length === 0) return;

  const tokens = tokensResult.rows.map((r) => r.fcm_token);
  try {
    // Send the notification to all of the user's devices at once.
    const response = await admin.messaging().sendEachForMulticast({
      tokens,
      notification,
    });

    // Collect any tokens FCM says are no longer valid.
    // These should be deleted so we stop trying to use them.
    const staleTokens = [];
    response.responses.forEach((r, i) => {
      if (!r.success && ['messaging/registration-token-not-registered', 'messaging/invalid-registration-token'].includes(r.error?.code)) {
        staleTokens.push(tokens[i]);
      }
    });

    // Remove the stale tokens from the database in one query.
    if (staleTokens.length > 0) {
      await pool.query('DELETE FROM notification_tokens WHERE fcm_token = ANY($1)', [staleTokens]);
    }
  } catch (e) {
    // Do not crash the caller if the push fails, just log a warning.
    console.warn('Push send failed:', e.message);
  }
}

module.exports = { sendPushToUser };