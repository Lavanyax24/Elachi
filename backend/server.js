require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');

const usersRoutes = require('./src/routes/users');
const booksRoutes = require('./src/routes/books');
const recipesRoutes = require('./src/routes/recipes');
const pantryRoutes = require('./src/routes/pantry'); // also handles /shopping-list
const cookSessionsRoutes = require('./src/routes/cookSessions'); // also handles /achievements, /streaks
const chatRoutes = require('./src/routes/chat');
const recipeParseRoutes = require('./src/routes/recipeParse');
const syncRoutes = require('./src/routes/sync');

const app = express();
app.use(helmet());
app.use(cors());
app.use(express.json());

// General API rate limit — generous enough for normal use, tight enough to
// stop one account or a bug in the client from hammering the database.
app.use('/api', rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 300,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Too many requests, please try again shortly.' },
}));

// A tighter limit specifically on auth-adjacent endpoints.
app.use('/api/users/sync', rateLimit({ windowMs: 15 * 60 * 1000, max: 30 }));

app.get('/', (req, res) => res.json({ status: 'ok', service: 'elachi-api' }));
app.get('/health', (req, res) => res.json({ status: 'ok' }));

app.use('/api/users', usersRoutes);
app.use('/api/books', booksRoutes);
app.use('/api/recipes', recipesRoutes);
app.use('/api', pantryRoutes);        // exposes /api/pantry and /api/shopping-list
app.use('/api', cookSessionsRoutes);  // exposes /api/cook-sessions, /api/achievements, /api/streaks
app.use('/api/chat', chatRoutes);
app.use('/api/recipes', recipeParseRoutes); // adds POST /api/recipes/parse-text
app.use('/api/sync', syncRoutes);

app.use((req, res) => res.status(404).json({ error: 'Not found.' }));

app.use((err, req, res, next) => {
  console.error(err);
  res.status(500).json({ error: 'Internal server error.' });
});

const PORT = process.env.PORT || 3000;
if (require.main === module) {
  app.listen(PORT, () => console.log(Elachi API listening on port ${PORT}));
}

module.exports = app;