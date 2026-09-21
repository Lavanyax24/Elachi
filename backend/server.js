// Load environment variables from .env
require('dotenv').config();

// Framework and security middleware
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');

// Route modules
const usersRoutes = require('./src/routes/users');
const booksRoutes = require('./src/routes/books');
const recipesRoutes = require('./src/routes/recipes');
const pantryRoutes = require('./src/routes/pantry');
const cookSessionsRoutes = require('./src/routes/cookSessions');
const chatRoutes = require('./src/routes/chat');
const recipeParseRoutes = require('./src/routes/recipeParse');
const syncRoutes = require('./src/routes/sync');

// Create the Express app
const app = express();

// Security headers, CORS, and JSON body parsing
app.use(helmet());
app.use(cors());
app.use(express.json());

// General rate limit for all API routes
app.use('/api', rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 300,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Too many requests, please try again shortly.' },
}));

// Tighter rate limit on user sync (auth-adjacent)
app.use('/api/users/sync', rateLimit({ windowMs: 15 * 60 * 1000, max: 30 }));

// Health check endpoints
app.get('/', (req, res) => res.json({ status: 'ok', service: 'elachi-api' }));
app.get('/health', (req, res) => res.json({ status: 'ok' }));

// Mount all route modules
app.use('/api/users', usersRoutes);
app.use('/api/books', booksRoutes);
app.use('/api/recipes', recipesRoutes);
app.use('/api', pantryRoutes);
app.use('/api', cookSessionsRoutes);
app.use('/api/chat', chatRoutes);
app.use('/api/recipes', recipeParseRoutes);
app.use('/api/sync', syncRoutes);

// 404 handler for unknown routes
app.use((req, res) => res.status(404).json({ error: 'Not found.' }));

// Central error handler
app.use((err, req, res, next) => {
  console.error(err);
  res.status(500).json({ error: 'Internal server error.' });
});

// Start the server (unless imported by tests)
const PORT = process.env.PORT || 3000;
if (require.main === module) {
  app.listen(PORT, () => console.log('Elachi API listening on port ' + PORT));
}

module.exports = app;