# Elachi Backend (Node.js / Express, deployed on Render)

REST API for the Elachi recipe app — recipe/book/pantry CRUD, cook-session
logging, achievement/streak calculation, and the AI Chef Assistant's LLM proxy.

## Local setup

1. npm install
2. Copy .env.example to .env and fill in the values (see below).
3. npm run db:init — applies src/db/schema.sql to your database.
4. npm run dev — starts the server on http://localhost:3000.

## Environment variables

| Variable | Where to get it |
|---|---|
| DATABASE_URL | Supabase dashboard → Settings → Database → Connection String ("Session" pooler string). |
| FIREBASE_SERVICE_ACCOUNT_JSON | Firebase Console → Project Settings → Service Accounts → "Generate new private key". Paste the whole JSON as one line. |
| COHERE_API_KEY | https://dashboard.cohere.com — free Trial key, 1,000 calls/month. |

## Project structure