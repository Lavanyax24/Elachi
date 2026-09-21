// db.js
// This file creates and exports a single shared PostgreSQL connection pool.
// Every route and helper that needs the database imports { pool } from here,
// so the whole app reuses the same pool instead of opening new connections.

const { Pool } = require('pg');
// Load environment variables from a .env file into process.env.
require('dotenv').config();

// Create the connection pool using the DATABASE_URL from the environment.
const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  // Enable SSL for remote databases (like Supabase, etc.),
  // but disable it for localhost so local development works without certs.
  // rejectUnauthorized: false is needed for most hosted Postgres providers
  // because their certificates are not in the local trust store.
  ssl: process.env.DATABASE_URL?.includes('localhost') ? false : { rejectUnauthorized: false },
});

// Export the pool so other files can run queries with pool.query(...).
module.exports = { pool };