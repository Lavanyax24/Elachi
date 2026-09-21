<a id="top"></a>

# 🍳 Elachi Backend
 **Mouth Full Of Flavour**

> **The engine behind Elachi.**
>
> A Node.js and Express REST API powering recipes, pantry management, cooking progress, achievements, notifications, and AI-powered cooking features.

---

<a id="table-of-contents"></a>
## 📑 Table of Contents

- [About](#about)
- [Team](#team)
- [Technology Stack](#technology-stack)
- [Features](#features)
- [Architecture](#architecture)
- [API Endpoints](#api-endpoints)
  - [Health](#endpoints-health)
  - [Users](#endpoints-users)
  - [Books](#endpoints-books)
  - [Recipes](#endpoints-recipes)
  - [Pantry](#endpoints-pantry)
  - [AI and Parsing](#endpoints-ai-and-parsing)
  - [Progress](#endpoints-progress)
- [Project Structure](#project-structure)
- [Setup](#setup)
  - [1. Clone the Repository](#1-clone-the-repository)
  - [2. Install Dependencies](#2-install-dependencies)
  - [3. Configure Environment Variables](#3-configure-environment-variables)
- [Database Setup](#database-setup)
- [Running the Backend](#running-the-backend)
- [Testing](#testing)
- [Database](#database)
- [Security](#security)
- [Deployment](#deployment)
- [Development Workflow](#development-workflow)
- [Code Attribution](#code-attribution)

---

<a id="about"></a>
## 📖 About

The Elachi Backend provides the server-side functionality for the Elachi Android recipe management application.

It handles authentication, recipes, recipe books, pantry items, shopping lists, cooking sessions, achievements, streaks, notifications, and AI-powered features.

The backend is built with **Node.js and Express**, uses **PostgreSQL through Supabase**, and is deployed using **Render**.

[⬆ Back to top](#top)

---

<a id="team"></a>
## 👥 Team

| Team Member | Student Number |
| :--- | :--- |
| **Diya Lakha** | **ST10439176** |
| **Lavanya Pillay** | **ST10438009** |
| **Saa'diyah Mansoor** | **ST10439057** |
| **Jarrud Frederick Cochrane** | **ST10266083** |

[⬆ Back to top](#top)

---

<a id="technology-stack"></a>
## 🛠️ Technology Stack

| Technology | Purpose |
| :--- | :--- |
| 🟢 **Node.js** | Backend runtime |
| 🚀 **Express.js** | REST API |
| 🐘 **PostgreSQL** | Database |
| ☁️ **Supabase** | Database hosting |
| 🔐 **Firebase Admin SDK** | Authentication verification |
| 🤖 **Cohere API** | AI Chef and recipe parsing |
| 🛡️ **Helmet** | HTTP security |
| 🚦 **Express Rate Limit** | Request rate limiting |
| 🧪 **Jest + Supertest** | Backend testing |
| ☁️ **Render** | Backend hosting |

[⬆ Back to top](#top)

---

<a id="features"></a>
## ✨ Features

- 🔐 Firebase authentication and user synchronisation
- 👤 User profile management
- 🍳 Recipe creation and management
- 📚 Recipe books
- 🥫 Pantry management
- 🛒 Shopping lists
- 👨‍🍳 Cooking sessions
- 🏆 Achievements
- 🔥 Cooking streaks
- ⭐ Recipe comments and ratings
- 🔎 Recipe discovery and suggestions
- 🤖 AI Chef Assistant
- 📝 Recipe text parsing
- 🔔 Notification management
- ❤️ API health checks

[⬆ Back to top](#top)

---

<a id="architecture"></a>
## 🏗️ Architecture

```text
┌─────────────────────┐
│     Android App     │
│ Kotlin + Compose    │
└──────────┬──────────┘
           │
           │ HTTPS / REST
           │ Firebase Token
           ▼
┌─────────────────────┐
│   Elachi Backend    │
│  Node.js + Express  │
├─────────────────────┤
│ Authentication      │
│ API Routes          │
│ Business Logic      │
│ Security            │
└──────────┬──────────┘
           │
      ┌────┴─────┐
      ▼          ▼
┌───────────┐ ┌────────────┐
│ Supabase  │ │  Firebase  │
│ PostgreSQL│ │    Auth    │
└───────────┘ └────────────┘
      │
      ▼
┌─────────────────┐
│   Cohere API    │
│ AI Functionality│
└─────────────────┘
```

[⬆ Back to top](#top)

---

<a id="api-endpoints"></a>
## 🌐 API Endpoints

<a id="endpoints-health"></a>
### ❤️ Health

| Method | Endpoint | Auth |
| :--- | :--- | :---: |
| `GET` | `/` | No |
| `GET` | `/health` | No |

<a id="endpoints-users"></a>
### 👤 Users

| Method | Endpoint | Auth |
| :--- | :--- | :---: |
| `POST` | `/api/users/sync` | No |
| `GET` | `/api/users/me` | Yes |
| `PATCH` | `/api/users/me` | Yes |
| `DELETE` | `/api/users/me` | Yes |

<a id="endpoints-books"></a>
### 📚 Books

| Method | Endpoint | Auth |
| :--- | :--- | :---: |
| `GET` | `/api/books` | Yes |
| `POST` | `/api/books` | Yes |
| `PATCH` | `/api/books/:id` | Yes |
| `DELETE` | `/api/books/:id` | Yes |

<a id="endpoints-recipes"></a>
### 🍳 Recipes

| Method | Endpoint | Auth |
| :--- | :--- | :---: |
| `GET` | `/api/recipes` | Yes |
| `GET` | `/api/recipes/discover` | Yes |
| `GET` | `/api/recipes/suggestions` | Yes |
| `GET` | `/api/recipes/:id` | Yes |
| `POST` | `/api/recipes` | Yes |
| `PATCH` | `/api/recipes/:id` | Yes |
| `DELETE` | `/api/recipes/:id` | Yes |

<a id="endpoints-pantry"></a>
### 🥫 Pantry

| Method | Endpoint | Auth |
| :--- | :--- | :---: |
| `GET` | `/api/pantry` | Yes |
| `POST` | `/api/pantry` | Yes |
| `DELETE` | `/api/pantry/:id` | Yes |

<a id="endpoints-ai-and-parsing"></a>
### 🤖 AI and Parsing

| Method | Endpoint | Auth |
| :--- | :--- | :---: |
| `POST` | `/api/chat` | Yes |
| `POST` | `/api/recipes/parse-text` | Yes |

<a id="endpoints-progress"></a>
### 🏆 Progress

| Method | Endpoint | Auth |
| :--- | :--- | :---: |
| `POST` | `/api/cook-sessions` | Yes |
| `GET` | `/api/achievements` | Yes |
| `GET` | `/api/streaks` | Yes |

[⬆ Back to top](#top)

---

<a id="project-structure"></a>
## 📁 Project Structure

```text
backend/
│
├── src/
│   ├── db/
│   │   ├── init.js
│   │   └── schema.sql
│   │
│   ├── middleware/
│   │   └── auth.js
│   │
│   ├── routes/
│   │   ├── books.js
│   │   ├── chat.js
│   │   ├── cookSessions.js
│   │   ├── pantry.js
│   │   ├── recipeParse.js
│   │   ├── recipes.js
│   │   ├── sync.js
│   │   └── users.js
│   │
│   ├── services/
│   │   ├── achievements.js
│   │   └── notifications.js
│   │
│   └── db.js
│
├── tests/
│   ├── endpoints.md
│   ├── health.test.js
│   ├── pantry-match.test.js
│   ├── pantry.test.js
│   ├── recipes.test.js
│   ├── setup.js
│   ├── streaks.test.js
│   └── users.test.js
│
├── .env.example
├── .gitignore
├── package.json
├── package-lock.json
├── server.js
└── README.md
```

[⬆ Back to top](#top)

---

<a id="setup"></a>
## ⚙️ Setup

<a id="1-clone-the-repository"></a>
### 1. Clone the Repository

```bash
git clone <YOUR-GITHUB-REPOSITORY-URL>
cd <YOUR-REPOSITORY>/backend
```

<a id="2-install-dependencies"></a>
### 2. Install Dependencies

```bash
npm install
```

<a id="3-configure-environment-variables"></a>
### 3. Configure Environment Variables

Create a `.env` file using `.env.example` as a template.

```env
DATABASE_URL=your_database_url
FIREBASE_SERVICE_ACCOUNT_JSON=your_firebase_service_account
COHERE_API_KEY=your_cohere_api_key
PORT=3000
```

> 🔒 **Never commit `.env` or real credentials to GitHub.**

[⬆ Back to top](#top)

---

<a id="database-setup"></a>
## 🗄️ Database Setup

Initialise the PostgreSQL database using:

```bash
npm run db:init
```

The database schema is located at:

```text
src/db/schema.sql
```

[⬆ Back to top](#top)

---

<a id="running-the-backend"></a>
## ▶️ Running the Backend

Development mode:

```bash
npm run dev
```

Standard start:

```bash
npm start
```

The API runs locally on:

```text
http://localhost:3000
```

Check that it is running:

```text
http://localhost:3000/health
```

[⬆ Back to top](#top)

---

<a id="testing"></a>
## 🧪 Testing

Run the automated backend tests:

```bash
npm test
```

Testing uses:

- **Jest**
- **Supertest**

Current tests cover areas including:

- Health checks
- Users
- Recipes
- Pantry
- Pantry matching
- Cooking streaks

Manual endpoint testing documentation is available in:

```text
tests/endpoints.md
```

[⬆ Back to top](#top)

---

<a id="database"></a>
## 🗄️ Database

Elachi uses **PostgreSQL through Supabase**.

Main database areas include:

- Users
- Recipes
- Recipe books
- Ingredients
- Recipe steps
- Pantry items
- Shopping-list items
- Cooking sessions
- Achievements
- Streaks
- Comments
- Notification tokens
- Notification preferences
- Friend relationships

Database configuration is provided through:

```text
DATABASE_URL
```

[⬆ Back to top](#top)

---

<a id="security"></a>
## 🔐 Security

The backend includes several security measures:

- Firebase ID token verification
- Protected API routes
- Parameterised PostgreSQL queries
- Helmet security headers
- CORS configuration
- Express rate limiting
- Environment-based secret management
- Controlled error responses

### 🔒 Never commit

```text
.env
```

Never commit or expose:

- Database passwords
- Firebase private keys
- Firebase service-account credentials
- Cohere API keys
- Access tokens
- User passwords

The Cohere API key remains on the backend and is not embedded in the Android application.

[⬆ Back to top](#top)

---

<a id="deployment"></a>
## ☁️ Deployment

The backend is deployed using **Render**.

Production environment variables should be configured through Render's environment-variable settings.

Required configuration includes:

```text
DATABASE_URL
FIREBASE_SERVICE_ACCOUNT_JSON
COHERE_API_KEY
PORT
```

Production credentials should never be stored directly in the GitHub repository.

[⬆ Back to top](#top)

---

<a id="development-workflow"></a>
## 👨‍💻 Development Workflow

```text
Feature
  ↓
Backend Changes
  ↓
Run Tests
  ↓
Test API
  ↓
Test Android Integration
  ↓
Security Review
  ↓
Commit
  ↓
Push
```

Before pushing changes:

```bash
git status
```

Make sure no `.env` files, credentials, API keys, or tokens are staged.

[⬆ Back to top](#top)

---

<a id="code-attribution"></a>
## 📚 Code Attribution

This backend was developed collaboratively by the Elachi team.

Third-party libraries, frameworks, APIs, documentation, and other external resources remain subject to their respective licences and terms of use.

### Additional Attribution

- Cohere, [s.a.]. Chat API. [online] Available at: <https://docs.cohere.com/reference/chat> [Accessed 21 September 2026].

- Express.js, [s.a.]. Express.js routing. [online] Available at: <https://expressjs.com/en/guide/routing/> [Accessed 21 September 2026].

- Firebase, [s.a.]. Add the Firebase Admin SDK to your server. [online] Available at: <https://firebase.google.com/docs/admin/setup> [Accessed 21 September 2026].

- Node.js, [s.a.]. Environment variables. [online] Available at: <https://nodejs.org/api/environment_variables.html> [Accessed 21 September 2026].

- Render, [s.a.]. Deploy a Node Express app on Render. [online] Available at: <https://render.com/docs/deploy-node-express-app> [Accessed 21 September 2026].

- Supabase, [s.a.]. JavaScript client library reference. [online] Available at: <https://supabase.com/docs/reference/javascript/introduction> [Accessed 21 September 2026].

[⬆ Back to top](#top)

---

<p align="center">

## 🍳 Elachi Backend

**Recipes in. Data handled. AI served.**

**Node.js • Express • PostgreSQL • Supabase • Firebase • Cohere • Render**

**Built by Team Elachi ❤️**

</p>
