const admin = require('firebase-admin');
const { pool } = require('../db');

// admin.apps can be undefined (not just empty) when firebase-admin fails to
// load, or in Jest's test environment where the SDK isn't initialised.
// Guard both cases so requiring this file never throws.
if (!admin.apps || !admin.apps.length) {
  try {
    const raw = process.env.FIREBASE_SERVICE_ACCOUNT_JSON;
    console.error('Firebase Admin init: FIREBASE_SERVICE_ACCOUNT_JSON length =', raw ? raw.length : 0);
    console.error('Firebase Admin init: first 60 chars =', raw ? raw.slice(0, 60) : '(null)');

    const serviceAccount = JSON.parse(raw || '{}');

    // Render's env var box can't hold real newlines, so the private key
    // arrives with literal "\n" characters — swap them back to real newlines.
    if (serviceAccount.private_key) {
      serviceAccount.private_key = serviceAccount.private_key.replace(/\\n/g, '\n');
    }
    admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
    console.error('Firebase Admin init: SUCCESS');
  } catch (e) {
    console.error('Firebase Admin init: FAILED with error:', e.message);
    console.error('Firebase Admin init: error name:', e.name);
    console.error('Firebase Admin not initialised — set FIREBASE_SERVICE_ACCOUNT_JSON to enable auth verification.');
  }
}

async function requireAuth(req, res, next) {
  const authHeader = req.headers.authorization || '';
  const token = authHeader.startsWith('Bearer ') ? authHeader.slice(7) : null;

  if (!token) {
    return res.status(401).json({ error: 'Missing Authorization bearer token.' });
  }

  try {
    const decoded = await admin.auth().verifyIdToken(token);
    const result = await pool.query('SELECT * FROM users WHERE firebase_uid = $1', [decoded.uid]);

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'User profile not found. Call POST /api/users/sync first.' });
    }

    req.user = result.rows[0];
    req.firebaseUid = decoded.uid;
    next();
  } catch (e) {
    console.error('Token verification failed:', e.message);
    return res.status(401).json({ error: 'Invalid or expired token.' });
  }
}

module.exports = { requireAuth };