// Node filesystem and path helpers
const fs = require('fs');
const path = require('path');

// Shared Postgres connection pool
const { pool } = require('../db');

// Reads schema.sql and executes it against the database
async function init() {
  const schema = fs.readFileSync(path.join(__dirname, 'schema.sql'), 'utf8');
  await pool.query(schema);
  console.log('Schema applied successfully.');

  // Close the pool so the script can exit
  await pool.end();
}

// Run, and exit with an error code if anything fails
init().catch((err) => {
  console.error('Failed to apply schema:', err);
  process.exit(1);
});