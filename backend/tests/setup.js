// Jest runs without a real Firebase project. Stub the env so
// firebase-admin's initialisation is a harmless no-op instead of
// throwing when the file is required by the test suite.
process.env.FIREBASE_SERVICE_ACCOUNT_JSON =
  process.env.FIREBASE_SERVICE_ACCOUNT_JSON || '{}';

// Same for the DB — tests don't touch it, but the pool is constructed
// on import of db.js, and pg complains without a connection string.
process.env.DATABASE_URL =
  process.env.DATABASE_URL || 'postgres://test:test@localhost:5432/test';