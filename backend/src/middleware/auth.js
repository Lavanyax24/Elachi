// auth.js - Firebase Admin initialisation and the requireAuth middleware.
// Every protected route uses requireAuth, which verifies the Firebase ID
// token sent by the Android app and attaches the matching user row to
// the request.

const admin = require('firebase-admin');
const { pool } = require('../db');

// Initialise the Firebase Admin SDK once, from the service account JSON
// in the FIREBASE_SERVICE_ACCOUNT_JSON env var. admin.apps may be
// undefined (not just empty) in test environments, so guard both cases.
if (!admin.apps || !admin.apps.length) {
  try {
    const raw = process.env.FIREBASE_SERVICE_ACCOUNT_JSON;
    console.error('Firebase Admin init: FIREBASE_SERVICE_ACCOUNT_JSON length =', raw ? raw.length : 0);
    console.error('Firebase Admin init: first 60 chars =', raw ? raw.slice(0, 60) : '(null)');

    const serviceAccount = JSON.parse(raw || '{}');

    // Render env vars can't hold real newlines - swap the literal "\n"
    // characters in the private key back to real ones.
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

// Express middleware: verifies the request's Bearer token, looks up the
// matching user in Postgres, and attaches it as req.user.
async function requireAuth(req, res, next) {
  const authHeader = req.headers.authorization || '';
  const token = authHeader.startsWith('Bearer ') ? authHeader.slice(7) : null;

  if (!token) {
    return res.status(401).json({ error: 'Missing Authorization bearer token.' });
  }

  try {
    // Verify the Firebase ID token and find the local user row
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