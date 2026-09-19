const fs = require('fs');
const path = require('path');
const { pool } = require('../db');

async function init() {
  const schema = fs.readFileSync(path.join(__dirname, 'schema.sql'), 'utf8');
  await pool.query(schema);
  console.log('Schema applied successfully.');
  await pool.end();
}

init().catch((err) => {
  console.error('Failed to apply schema:', err);
  process.exit(1);
});