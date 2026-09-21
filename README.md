# 🍳 Elachi

**A modern recipe management Android application powered by a custom REST API, cloud storage, Firebase authentication, and intelligent cooking features.**

Built with **Kotlin, Jetpack Compose, Node.js, Express, PostgreSQL, Supabase and Firebase**.

> 🎥 **Project Demonstration:**  
> **[INSERT YOUTUBE VIDEO LINK HERE]**

---

## 👥 Developer Information

**Project:** Elachi Recipe Management Application  
**Academic Year:** 2026

| TEAM MEMBER | STUDENT NUMBER |
| :--- | :--- |
| DIYA LAKHA | ST10439176 |
| LAVANYA PILLAY | ST10438009 |
| SAA'DIYAH MANSOOR | ST10439057 |
| JARRUD FREDERICK COCHRANE | ST10266083 |

---

# 📑 Table of Contents

- [Project Overview](#project-overview)
- [Documentation](#documentation)
- [Technology Stack](#technology-stack)
- [Key Features](#key-features)
  - [Recipe Management](#recipe-management)
  - [Recipe Books](#recipe-books)
  - [Recipe Discovery](#recipe-discovery)
  - [Pantry Management](#pantry-management)
  - [Pantry-Based Recipe Suggestions](#pantry-based-recipe-suggestions)
  - [Pantry Health](#pantry-health)
  - [AI Chef Assistant](#ai-chef-assistant)
  - [Recipe Text Parsing](#recipe-text-parsing)
  - [Camera and OCR](#camera-and-ocr)
  - [Cooking Tools](#cooking-tools)
  - [Achievements](#achievements)
  - [Cooking Streaks](#cooking-streaks)
  - [Notifications](#notifications)
  - [User Profiles](#user-profiles)
- [System Architecture](#system-architecture)
- [Android Application](#android-application)
- [Backend REST API](#backend-rest-api)
- [Database and Storage](#database-and-storage)
- [Authentication and Security](#authentication-and-security)
- [AI Chef Assistant](#ai-chef-assistant-1)
- [Camera and OCR](#camera-and-ocr-1)
- [Notifications](#notifications-1)
- [Achievements and Cooking Streaks](#achievements-and-cooking-streaks)
- [Project Structure](#project-structure)
  - [Android Project Structure](#android-project-structure)
  - [Backend Project Structure](#backend-project-structure)
- [Requirements](#requirements)
- [Setup and Run Instructions](#setup-and-run-instructions)
  - [Clone the Repository](#clone-the-repository)
  - [Backend Setup](#backend-setup)
  - [Database Setup](#database-setup)
  - [Running the Backend](#running-the-backend)
  - [Running the Android Application](#running-the-android-application)
  - [Firebase Setup](#firebase-setup)
  - [Supabase Setup](#supabase-setup)
  - [AI Configuration](#ai-configuration)
- [Testing](#testing)
  - [Backend Tests](#backend-tests)
  - [Android Unit Tests](#android-unit-tests)
  - [Android Instrumented Tests](#android-instrumented-tests)
  - [Manual API Testing](#manual-api-testing)
- [Deployment](#deployment)
- [CI and GitHub Workflows](#ci-and-github-workflows)
- [Security Practices](#security-practices)
- [Troubleshooting](#troubleshooting)
- [Development Workflow](#development-workflow)
- [Repository Hygiene](#repository-hygiene)
- [Responsible Configuration](#responsible-configuration)
- [Future Improvements](#future-improvements)
- [Team](#team)
- [Code Attribution](#code-attribution)
- [Final Demonstration](#final-demonstration)
- [License](#license)

---

# 📖 Project Overview

Elachi is a full-stack recipe management platform designed to make cooking, organising recipes, managing pantry items, discovering meals, and tracking cooking progress easier and more engaging.

The project combines a native **Android application built with Kotlin and Jetpack Compose** with a **Node.js and Express REST API**, backed by **Supabase PostgreSQL**, **Supabase Storage**, and **Firebase Authentication**.

The application also includes intelligent features such as the **AI Chef Assistant**, recipe text parsing, pantry-based recipe suggestions, achievements, cooking streaks, notifications, offline/local persistence, and recipe management tools.

The system follows a client-server architecture.

The Android application acts as the primary user interface while the Node.js backend provides authenticated REST API functionality and communicates with the application's cloud services.

---

# 📚 Documentation

Additional documentation is contained within the repository.

### Backend Documentation

```text
backend/README.md
```

### API Testing Documentation

```text
backend/tests/endpoints.md
```

### Database Schema

```text
backend/src/db/schema.sql
```

### Backend Environment Template

```text
backend/.env.example
```

### Android Application

```text
andriod/
```

---

# 🛠️ Technology Stack

| Area | Technology |
| :--- | :--- |
| Mobile Platform | Android |
| Programming Language | Kotlin |
| UI Framework | Jetpack Compose |
| Architecture | MVVM-style architecture with repositories and ViewModels |
| Local Database | Room |
| Local Preferences | Android DataStore |
| Networking | Retrofit + OkHttp |
| Image Loading | Coil |
| Camera | CameraX |
| OCR | Google ML Kit |
| Authentication | Firebase Authentication |
| Push Notifications | Firebase Cloud Messaging |
| Backend | Node.js |
| API Framework | Express |
| Database | PostgreSQL |
| Database Platform | Supabase |
| Image Storage | Supabase Storage |
| AI Services | Cohere API through backend |
| Backend Authentication | Firebase Admin SDK |
| Backend Hosting | Render |
| Backend Testing | Jest + Supertest |
| Android Testing | JUnit + AndroidX/Compose testing |
| Build System | Gradle |
| Source Control | Git + GitHub |
| CI | GitHub Actions |

---

# ✨ Key Features

## 📚 Recipe Management

Users can create and manage detailed recipes containing:

- Recipe titles
- Categories
- Cuisine types
- Food types
- Difficulty levels
- Serving sizes
- Cooking times
- Cooking methods
- Ingredients
- Ingredient quantities
- Ingredient units
- Ordered cooking instructions
- Optional cooking timers
- Allergens
- Recipe visibility
- Recipe images

Recipes can be updated and organised into recipe books.

---

## 📖 Recipe Books

Users can organise recipes into custom recipe books.

Recipe books support:

- Custom names
- Descriptions
- Icons
- Colours
- Recipe organisation
- Book-specific recipe views

This allows users to separate recipes into categories such as:

- Weeknight Meals
- Desserts
- Family Recipes
- Breakfast
- Meal Prep
- Special Occasions

---

## 🔎 Recipe Discovery

Elachi includes a recipe discovery experience that allows users to explore public recipes.

Discovery functionality supports:

- Global recipe discovery
- Personalised discovery
- Recipe search
- Cuisine filtering
- Recipe ratings
- Recipe creator information
- Cooking statistics

Private recipes remain separate from publicly discoverable recipes.

---

## 🥫 Pantry Management

Users can maintain a digital pantry containing:

- Ingredient names
- Quantities
- Units

The pantry is used by the application to provide useful cooking functionality, including recipe suggestions based on ingredients the user already has.

---

## 💡 Pantry-Based Recipe Suggestions

Elachi can compare recipes against pantry ingredients and calculate how closely each recipe matches the user's available ingredients.

This allows the application to surface recipes that can be prepared using ingredients already available to the user.

The backend calculates recipe-to-pantry matches rather than relying entirely on the Android client.

---

## 📊 Pantry Health

The application provides a pantry health calculation based on how well the user's available pantry ingredients match their recipes.

This provides users with a quick overview of how prepared they are to cook the recipes in their collection.

---

## 👨‍🍳 AI Chef Assistant

Elachi includes an AI Chef Assistant that allows users to ask cooking-related questions.

The assistant can use relevant pantry information as context when generating responses.

Example use cases include:

- "What can I make with rice and eggs?"
- Ingredient-based recipe ideas
- Cooking guidance
- Recipe-related questions
- Ingredient substitutions
- General cooking assistance

The AI API key is kept on the backend and is **not embedded in the Android application**.

---

## 📝 Recipe Text Parsing

Elachi includes backend functionality for processing raw recipe text and converting it into structured recipe information.

This can be used with text captured from recipes and can assist with:

- Recipe titles
- Ingredients
- Quantities
- Units
- Cooking instructions

The AI service is accessed through the backend rather than directly from the Android client.

---

## 📷 Camera and OCR

The Android application includes camera functionality for capturing recipe information.

The project uses:

- CameraX
- Google ML Kit Text Recognition

This allows text to be captured from physical or displayed recipe content and processed within the application.

---

## 🧮 Cooking Tools

The application includes cooking utilities such as:

- Serving-size calculations
- Unit conversion
- Cooking timers
- Recipe text parsing
- Recipe PDF exporting

These tools are designed to make recipes more practical while users are cooking.

---

## 🏆 Achievements

Elachi includes an achievement system that rewards users for cooking activity and progress.

Achievement functionality is supported by backend services that evaluate user activity and unlock relevant achievements.

---

## 🔥 Cooking Streaks

Cooking activity can contribute to user streaks.

The application provides a streak calendar and tracks cooking sessions to help users maintain consistent cooking habits.

---

## 🔔 Notifications

Elachi integrates Firebase Cloud Messaging for push notifications.

Notifications can be used to keep users informed about relevant application activity.

The Android application registers for Firebase messaging and handles notification events through its dedicated messaging service.

---

## 👤 User Profiles

Users can maintain application profiles containing information such as:

- Display name
- Profile information
- Cooking interests
- User preferences

The backend associates the application profile with the authenticated Firebase user.

---

# 🏗️ System Architecture

Elachi follows a client-server architecture.

```text
┌───────────────────────────────┐
│       Android Application     │
│                               │
│ Kotlin + Jetpack Compose      │
│ ViewModels + Repositories     │
│ Room + DataStore              │
│ Retrofit + OkHttp             │
│ Firebase Authentication       │
│ CameraX + ML Kit              │
└───────────────┬───────────────┘
                │
                │ HTTPS / REST API
                │ Firebase Bearer Token
                ▼
┌───────────────────────────────┐
│       Node.js / Express       │
│                               │
│ Authentication Middleware     │
│ REST API Routes               │
│ Business Logic                │
│ AI Service Proxy              │
│ Achievement Services          │
│ Notification Services         │
└───────┬───────────┬───────────┘
        │           │
        │           │
        ▼           ▼
┌─────────────┐  ┌────────────────┐
│  Supabase   │  │ Firebase       │
│ PostgreSQL  │  │ Authentication │
│             │  │ Firestore      │
│ Storage     │  │ FCM            │
└─────────────┘  └────────────────┘
        │
        ▼
┌───────────────────────────────┐
│          Cohere API           │
│                               │
│ AI Chef Assistant             │
│ Recipe Text Processing        │
└───────────────────────────────┘
```

## Request Flow

A typical authenticated request follows this flow:

1. The user signs into the Android application.
2. Firebase Authentication authenticates the user.
3. The Android client obtains a Firebase ID token.
4. Retrofit attaches the token as a Bearer token.
5. The request is sent to the Express backend.
6. Firebase Admin verifies the token.
7. The backend identifies the corresponding application user.
8. The requested route performs its database or service operation.
9. The backend returns a controlled JSON response.
10. The Android application updates its UI through its repository/ViewModel layer.

---

# 📱 Android Application

The Android application is built using Kotlin and Jetpack Compose.

The UI is separated into multiple feature areas, with repositories handling application data and ViewModels managing screen-level state and operations.

## Android Technologies

### Kotlin

The primary programming language used throughout the Android application.

### Jetpack Compose

The application UI is built using modern declarative Android UI development.

### Room

Room provides local persistence for application data.

The project contains DAOs and entities for areas such as:

- Recipes
- Recipe books
- Pantry items
- Achievements

### DataStore

Android DataStore is used for local preference-style data.

### Retrofit

Retrofit provides the Android client with a structured interface to the backend REST API.

### OkHttp

OkHttp supports the application's HTTP communication.

### Coil

Coil is used for loading images.

### CameraX

CameraX provides camera functionality.

### ML Kit

Google ML Kit provides text recognition functionality.

### Firebase

Firebase provides:

- Authentication
- Firestore integration
- Cloud Messaging
- Google authentication support

---

# 🌐 Backend REST API

The backend is built using:

- Node.js
- Express
- PostgreSQL
- Firebase Admin SDK
- Helmet
- CORS
- Express Rate Limit
- Jest
- Supertest

The backend provides the application's server-side business logic and data access.

## Main API Areas

### Users

Handles user profile synchronisation and profile operations.

```text
POST   /api/users/sync
GET    /api/users/me
PATCH  /api/users/me
```

### Recipe Books

Handles recipe-book management.

```text
GET    /api/books
POST   /api/books
PATCH  /api/books/:id
DELETE /api/books/:id
```

### Recipes

Handles recipe creation, retrieval, discovery and modification.

```text
GET    /api/recipes
POST   /api/recipes
GET    /api/recipes/:id
PATCH  /api/recipes/:id
DELETE /api/recipes/:id
GET    /api/recipes/discover
GET    /api/recipes/suggestions
GET    /api/recipes/pantry-health
```

### Pantry

Provides pantry management functionality.

```text
GET    /api/pantry
POST   /api/pantry
PATCH  /api/pantry/:id
DELETE /api/pantry/:id
```

### Cook Sessions

Records cooking activity.

```text
POST /api/cook-sessions
```

### Achievements

Provides achievement information and progression.

```text
GET /api/achievements
```

### Streaks

Provides cooking streak information.

```text
GET /api/streaks
```

### AI Chef

Provides the AI Chef Assistant through the backend.

```text
POST /api/chat
```

### Recipe Parsing

Processes raw recipe text.

```text
POST /api/recipes/parse-text
```

### Health Checks

The backend exposes public health endpoints:

```text
GET /
GET /health
```

These can be used to verify that the server is running.

---

# 🗄️ Database and Storage

## PostgreSQL

The primary relational database is PostgreSQL hosted through Supabase.

The database stores application data such as:

- Users
- Recipes
- Ingredients
- Recipe steps
- Recipe books
- Pantry items
- Comments
- Ratings
- Cooking sessions
- Other application records

Database queries use parameterised PostgreSQL queries rather than directly concatenating user-provided values into SQL statements.

---

## Supabase Storage

Supabase Storage is used for recipe-related image uploads.

The Android application communicates with the configured storage service for image uploads while the backend remains responsible for server-side API operations.

Storage access should always be configured according to the project's Supabase security policies and should not expose privileged service credentials.

---

# 🔐 Authentication and Security

Security is an important part of the Elachi architecture.

## Firebase Authentication

Firebase Authentication is used for user authentication.

The Android application authenticates the user and receives a Firebase ID token.

Authenticated API requests use:

```text
Authorization: Bearer <Firebase ID Token>
```

The backend verifies the token using the Firebase Admin SDK.

---

## Protected API Routes

Most application API routes require authentication.

The backend authentication middleware:

1. Reads the Authorization header.
2. Extracts the Bearer token.
3. Verifies the Firebase ID token.
4. Finds the corresponding local user.
5. Attaches the authenticated user to the request.
6. Allows the protected route to continue.

Requests without valid authentication are rejected.

---

## Parameterised Database Queries

The backend uses parameterised PostgreSQL queries.

This helps prevent SQL injection by ensuring user-controlled values are passed separately from SQL statements.

---

## Security Headers

The backend uses Helmet to provide HTTP security headers.

---

## Rate Limiting

API requests are rate limited using `express-rate-limit`.

A general rate limit is applied to API routes, while user synchronisation has an additional tighter limit.

This helps reduce abuse and excessive automated requests.

---

## Error Handling

The backend uses centralised error handling and avoids returning internal implementation details as API responses.

Client-facing errors are returned using controlled JSON responses.

---

## Secret Management

Sensitive credentials must **never** be committed to GitHub.

This includes:

- Database passwords
- Supabase service-role keys
- Firebase Admin service-account private keys
- Cohere API keys
- Other private API credentials
- Production secrets
- Passwords
- Access tokens

Local backend secrets should be stored in:

```text
backend/.env
```

The `.env` file is excluded from Git through `.gitignore`.

Production secrets should be configured through the hosting provider's environment-variable system.

### Important

Do not paste real secrets into:

- `README.md`
- GitHub issues
- GitHub commits
- Screenshots
- YouTube demonstrations
- Source-code comments
- Public documentation

If a secret is accidentally committed, removing it from the latest commit is not sufficient. The credential should be treated as compromised and rotated or revoked through the relevant provider.

---

# 🤖 AI Chef Assistant

The AI Chef Assistant is intentionally accessed through the backend.

The Android application sends the user's request to the Elachi API.

The backend then communicates with the configured AI provider.

This architecture prevents the AI provider's private API credential from being embedded in the Android application.

The backend can also provide relevant pantry context to the AI assistant when available.

## AI Request Flow

```text
Android App
     │
     ▼
POST /api/chat
     │
     ▼
Firebase Authentication
     │
     ▼
Express Backend
     │
     ├── User information
     ├── Pantry context
     └── Conversation context
     │
     ▼
Cohere API
     │
     ▼
AI response
     │
     ▼
Android App
```

---

# 📷 Camera and OCR

Elachi uses CameraX for camera functionality and Google ML Kit Text Recognition for OCR.

A typical recipe capture flow is:

```text
Camera
  │
  ▼
Image Capture
  │
  ▼
Text Recognition
  │
  ▼
Extracted Recipe Text
  │
  ▼
Recipe Parsing
  │
  ▼
Structured Recipe
  │
  ▼
User Review
  │
  ▼
Recipe Saved
```

Users should always review automatically extracted recipe information before saving it, particularly when OCR is used with low-quality images, unusual fonts, handwriting, or complex layouts.

---

# 🔔 Notifications

Firebase Cloud Messaging is integrated into the Android application.

The project includes a dedicated messaging service:

```text
ElachiMessagingService
```

The application can receive Firebase Cloud Messaging events and handle notification-related functionality.

Notification permissions are requested according to the Android platform requirements.

---

# 🏆 Achievements and Cooking Streaks

Elachi includes gamification features designed to encourage continued cooking activity.

## Achievements

Achievement logic is handled by backend services.

Achievements can be unlocked based on application activity such as creating recipes or completing relevant milestones.

## Cooking Streaks

Cooking sessions are recorded by the backend and can contribute to a user's cooking streak.

The Android application includes a streak calendar for presenting this information to users.

---

# 📦 Project Structure

The repository is divided into the Android application, backend API, CI workflows and supporting documentation.

```text
Elachi/
│
├── .github/
│   └── workflows/
│       └── .gitkeep
│
├── andriod/
│   ├── app/
│   │   ├── src/
│   │   │   ├── androidTest/
│   │   │   ├── main/
│   │   │   └── test/
│   │   │
│   │   ├── build.gradle.kts
│   │   └── google-services.json
│   │
│   ├── gradle/
│   │   └── wrapper/
│   │
│   ├── build.gradle.kts
│   ├── gradle.properties
│   ├── gradlew
│   ├── gradlew.bat
│   └── settings.gradle.kts
│
├── backend/
│   ├── src/
│   │   ├── db/
│   │   │   ├── init.js
│   │   │   └── schema.sql
│   │   │
│   │   ├── middleware/
│   │   │   └── auth.js
│   │   │
│   │   ├── routes/
│   │   │   ├── books.js
│   │   │   ├── chat.js
│   │   │   ├── cookSessions.js
│   │   │   ├── pantry.js
│   │   │   ├── recipeParse.js
│   │   │   ├── recipes.js
│   │   │   ├── sync.js
│   │   │   └── users.js
│   │   │
│   │   └── services/
│   │       ├── achievements.js
│   │       └── notifications.js
│   │
│   ├── tests/
│   │   ├── endpoints.md
│   │   ├── health.test.js
│   │   ├── pantry-match.test.js
│   │   ├── pantry.test.js
│   │   ├── recipes.test.js
│   │   ├── setup.js
│   │   ├── streaks.test.js
│   │   └── users.test.js
│   │
│   ├── .env.example
│   ├── .gitignore
│   ├── package.json
│   ├── package-lock.json
│   └── server.js
│
├── docs/
│   └── images/
│       ├── android-tests.png
│       └── github-actions.png
│
├── .gitignore
└── README.md
```

---

# 📱 Android Project Structure

The Android application is organised into data, navigation, UI, notification, utility and theme components.

```text
andriod/app/src/main/java/com/elachi/app/
│
├── ElachiApp.kt
├── MainActivity.kt
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   └── entities/
│   │
│   ├── remote/
│   │   ├── ApiService.kt
│   │   ├── RetrofitClient.kt
│   │   ├── SupabaseStorageClient.kt
│   │   └── dto/
│   │
│   └── repository/
│       ├── AchievementRepository.kt
│       ├── AuthRepository.kt
│       ├── ChatRepository.kt
│       ├── PantryRepository.kt
│       ├── ProfileRepository.kt
│       ├── RecipeRepository.kt
│       └── UserSession.kt
│
├── navigation/
│   ├── NavGraph.kt
│   ├── SimpleViewModelFactory.kt
│   └── screen.kt
│
├── notifications/
│   └── ElachiMessagingService.kt
│
├── ui/
│   ├── achievements/
│   ├── aichef/
│   ├── auth/
│   ├── common/
│   ├── cookbook/
│   ├── discover/
│   ├── help/
│   ├── home/
│   ├── onboarding/
│   ├── pantry/
│   ├── profile/
│   ├── recipe/
│   ├── settings/
│   ├── splash/
│   ├── streak/
│   ├── theme/
│   └── tools/
│
└── util/
    ├── AlarmPlayer.kt
    ├── RecipePdfExporter.kt
    ├── RecipeTextParser.kt
    └── ServingScaler.kt
```

---

# 🌐 Backend Project Structure

```text
backend/
│
├── server.js
│
├── src/
│   ├── db.js
│   │
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
│   └── services/
│       ├── achievements.js
│       └── notifications.js
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
└── package-lock.json
```

---

# ⚙️ Requirements

## Android Development

Recommended:

- Android Studio
- JDK 17
- Android SDK
- Android SDK Platform 35
- Android SDK Build Tools
- Android emulator or physical Android device

The project is configured for:

```text
Compile SDK: 35
Target SDK: 35
Minimum SDK: 24
Java: 17
Kotlin: 2.0.21
Android Gradle Plugin: 8.7.3
```

---

## Backend Development

Required:

- Node.js 20 or newer
- npm
- PostgreSQL-compatible database
- Firebase project
- Supabase project
- Cohere API access if AI features are being tested

The backend declares Node.js 20+ as its supported runtime.

---

# 🚀 Setup and Run Instructions

## Clone the Repository

Clone the GitHub repository:

```bash
git clone <YOUR-GITHUB-REPOSITORY-URL>
```

Enter the project directory:

```bash
cd <YOUR-REPOSITORY-DIRECTORY>
```

---

# 🌐 Backend Setup

Navigate into the backend:

```bash
cd backend
```

Install dependencies:

```bash
npm install
```

---

## Configure Backend Environment Variables

Create a local environment file from the example:

```text
backend/.env.example
```

Create:

```text
backend/.env
```

The backend requires configuration for:

```text
DATABASE_URL=
FIREBASE_SERVICE_ACCOUNT_JSON=
COHERE_API_KEY=
PORT=3000
```

### Important Security Rule

**Never commit `backend/.env`.**

The repository already excludes `.env` through `.gitignore`.

The Firebase service-account value contains sensitive credentials and must only exist in secure environment configuration.

---

# 🗄️ Database Setup

The backend includes database initialisation support.

After configuring `DATABASE_URL`, run:

```bash
npm run db:init
```

This applies the database schema required by the backend.

The schema is located at:

```text
backend/src/db/schema.sql
```

---

# ▶️ Running the Backend

For development:

```bash
npm run dev
```

The backend should start on:

```text
http://localhost:3000
```

The backend also exposes:

```text
http://localhost:3000/
```

and:

```text
http://localhost:3000/health
```

A successful health response should indicate that the service is running.

---

## Production-Style Start

The backend can also be started using:

```bash
npm start
```

The server uses the `PORT` environment variable when supplied and otherwise defaults to port `3000`.

---

# 📱 Running the Android Application

Open the `andriod/` directory in Android Studio.

```text
Elachi/
└── andriod/
```

Allow Android Studio to:

1. Import the Gradle project.
2. Download required dependencies.
3. Sync the Gradle configuration.
4. Index the project.
5. Build the application.

---

## Android Backend Configuration

The Android application is configured to communicate with the deployed backend.

The current project configuration contains the backend base URL through the Android BuildConfig setup.

For a local backend development environment, make sure the Android application can reach the development server from the emulator or physical device.

### Android Emulator

If your backend is running on the development computer, remember that Android emulator networking does not always treat `localhost` as the host computer.

For an Android emulator, the host machine is commonly accessed through:

```text
10.0.2.2
```

However, use the project's actual configured development environment and do not expose a development server publicly just to make the application work.

---

# 🔥 Firebase Setup

Firebase is used for authentication and application services.

The Android application uses Firebase configuration through the Google Services Gradle integration.

A Firebase project should be configured with the required Android application details.

The backend separately requires Firebase Admin credentials for verifying Firebase ID tokens.

### Never commit

Do not commit:

- Firebase Admin private keys
- Service-account JSON containing private credentials
- Server-side Firebase credentials
- Production secrets

Firebase Android configuration files may contain client-side project identifiers, but **server-side credentials must never be placed in the Android application**.

---

# 🗄️ Supabase Setup

Supabase provides:

- PostgreSQL
- Storage

The database connection is configured on the backend through:

```text
DATABASE_URL
```

The Android application uses the configured Supabase storage integration for image uploads.

Supabase access policies should be configured so that users cannot access or modify data outside the permissions intended by the application.

Do not place a Supabase **service-role key** in the Android application.

---

# 🤖 AI Configuration

The AI features require:

```text
COHERE_API_KEY
```

This key belongs on the backend only.

The Android application should communicate with:

```text
POST /api/chat
```

and:

```text
POST /api/recipes/parse-text
```

rather than directly calling the AI provider with a private API key.

---

# 🧪 Testing

Elachi contains both backend and Android tests.

## Backend Tests

From the `backend/` directory:

```bash
npm test
```

The backend uses:

- Jest
- Supertest

The test suite includes coverage for areas such as:

- Health endpoints
- Pantry functionality
- Pantry matching
- Recipes
- Streaks
- Users

---

## Backend Test Files

```text
backend/tests/
├── health.test.js
├── pantry-match.test.js
├── pantry.test.js
├── recipes.test.js
├── setup.js
├── streaks.test.js
└── users.test.js
```

---

# 📱 Android Unit Tests

Android unit tests are located under:

```text
andriod/app/src/test/
```

Android Studio can run these tests through the standard Gradle test tools.

From the `andriod/` directory, the Gradle wrapper can also be used.

### Windows

```powershell
.\gradlew.bat test
```

### macOS / Linux

```bash
./gradlew test
```

### Verified Test Results

The Android utility tests were successfully executed in Android Studio.

**11 tests passed, 11 tests total.**

<img width="1600" height="761" alt="image" src="https://github.com/user-attachments/assets/4f9b511d-fbe0-4961-9387-d42c52d9a869" />

*Figure 1: Android unit tests successfully passing in Android Studio.*

---

# 📱 Android Instrumented Tests

Instrumented tests are located under:

```text
andriod/app/src/androidTest/
```

These tests require an Android emulator or compatible physical Android device.

They can be run from Android Studio using the Android testing tools.

---

# 🔍 Manual API Testing

The repository contains a manual API testing reference at:

```text
backend/tests/endpoints.md
```

It documents example requests for:

- Health checks
- User synchronisation
- User profiles
- Recipe books
- Recipes
- Pantry
- Shopping lists
- Cooking sessions
- Achievements
- Streaks
- AI Chef
- Recipe text parsing

---

## Health Check

Once the backend is running:

```bash
curl http://localhost:3000/health
```

A successful response should indicate that the backend is healthy.

---

## Authenticated Endpoints

Protected endpoints require a valid Firebase ID token.

Requests use:

```text
Authorization: Bearer <FIREBASE_ID_TOKEN>
```

Do not publish real Firebase ID tokens in:

- GitHub
- README files
- Screenshots
- Documentation
- Videos
- Issue trackers

Use test accounts and temporary credentials when demonstrating authenticated API requests.

---

# ☁️ Deployment

The backend is designed to run on Render.

The deployed environment should provide the required backend environment variables through Render's Environment configuration.

Production configuration should include:

```text
DATABASE_URL
FIREBASE_SERVICE_ACCOUNT_JSON
COHERE_API_KEY
PORT
```

The exact production values must be configured securely through the hosting provider.

They should never be committed to the repository.

---

# 🔄 CI and GitHub Workflows

The project reserves the following directory for continuous integration workflows:

```text
.github/workflows/
```

GitHub Actions workflows can be placed here for automated processes such as:

- Backend tests
- Android builds
- Static checks
- Automated validation
- Release workflows

Any workflow added to this directory should avoid printing secrets or exposing environment variables in logs.

## GitHub Actions Results

The repository includes CI workflows for both the Android and backend components.

The GitHub Actions workflow runs can be viewed from the repository's **Actions** tab.

<img width="1600" height="661" alt="image" src="https://github.com/user-attachments/assets/fbdbf239-a5ea-45b1-87ad-b260cb9e5943" />

*Figure 2: GitHub Actions showing successful Android CI and Backend CI workflow runs.*

---

# 🛡️ Security Practices

The project follows several security-focused practices.

## Authentication

Firebase Authentication is used for user authentication.

Firebase Admin verifies tokens on the backend.

---

## Authorisation

Authenticated user information is used by backend routes to associate data with the correct account.

Database operations should always verify ownership where required.

---

## SQL Injection Protection

PostgreSQL queries use parameterised values rather than directly inserting user input into SQL statements.

---

## HTTP Security

Helmet is enabled on the Express server.

---

## Rate Limiting

Express rate limiting is enabled for API routes.

User synchronisation has additional rate limiting because it is closely associated with authentication.

---

## Secret Protection

Secrets are provided through environment variables.

Never commit:

```text
.env
```

or other private credential files.

---

## AI Key Protection

The AI provider API key is stored on the backend.

The Android application does not need access to the private AI API credential.

---

## Firebase Admin Protection

Firebase Admin credentials must remain server-side.

They must never be bundled into the Android application.

---

## Secure Demonstrations

When recording the YouTube demonstration:

- Do not show `.env`
- Do not show API keys
- Do not show Firebase service-account credentials
- Do not show private database passwords
- Do not show access tokens
- Do not show personal test-account credentials
- Do not display secret values in terminal output
- Do not include secrets in screenshots

---

# 🐛 Troubleshooting

## Backend Does Not Start

Check:

```bash
node --version
```

The project requires Node.js 20 or newer.

Then reinstall dependencies:

```bash
npm install
```

Check that the backend `.env` file exists and contains the required configuration.

---

## Database Connection Fails

Check:

```text
DATABASE_URL
```

Make sure the database is accessible and the connection string is correct.

Then try:

```bash
npm run db:init
```

---

## Firebase Authentication Fails

Check that:

- Firebase Authentication is configured.
- The Android Firebase configuration belongs to the correct Firebase project.
- The backend has valid Firebase Admin credentials.
- The Firebase ID token has not expired.
- The user has been synchronised with the backend.

---

## AI Chef Does Not Respond

Check that:

```text
COHERE_API_KEY
```

is configured on the backend.

The Android application should never contain the private Cohere API key.

---

## Android Application Cannot Connect to Backend

Check:

1. The backend is running.
2. The configured API URL is correct.
3. The Android device has network access.
4. The emulator can reach the backend host.
5. Firebase authentication is working.
6. The backend is reachable from the device.

---

## Images Do Not Upload

Check:

- Supabase Storage configuration.
- Storage bucket configuration.
- Storage policies.
- Android network connectivity.
- Image permissions.
- The configured storage URL.

Do not solve storage issues by adding privileged Supabase service credentials to the Android application.

---

# 🧑‍💻 Development Workflow

A typical development workflow for the project is:

```text
1. Create or update a feature
          │
          ▼
2. Implement Android UI / ViewModel / Repository changes
          │
          ▼
3. Implement required backend API changes
          │
          ▼
4. Update database functionality if required
          │
          ▼
5. Run backend tests
          │
          ▼
6. Build and test Android application
          │
          ▼
7. Test the feature end-to-end
          │
          ▼
8. Review changes for security
          │
          ▼
9. Commit changes
          │
          ▼
10. Push to GitHub
```

Before pushing changes, verify that:

- No secrets were added.
- `.env` files are not staged.
- Firebase private credentials are not committed.
- API keys are not present in source code.
- Tests pass.
- The Android application builds successfully.
- Backend functionality still works.
- Existing functionality has not been unintentionally removed.

---

# 🧹 Repository Hygiene

The repository uses `.gitignore` rules to prevent common generated files and local configuration from being committed.

Examples include:

```text
node_modules/
.env
*.log
build/
.gradle/
.idea/
local.properties
```

Generated build files and local development credentials should remain outside source control.

---

# 🔐 Responsible Configuration

Elachi is designed so that sensitive server-side credentials remain outside the Android client.

The following separation should be maintained:

| Credential / Configuration | Location |
| :--- | :--- |
| Firebase user authentication | Android + Firebase |
| Firebase Admin credentials | Backend environment only |
| Database connection string | Backend environment only |
| Cohere API key | Backend environment only |
| Supabase service-role credentials | Backend only, if required |
| Supabase public/client configuration | Android only where appropriate |
| Application API URL | Android configuration |
| Local development secrets | `.env` / local environment |

**Never move a server-side private credential into the Android application simply to make a feature easier to configure.**

---

# 📈 Future Improvements

Potential future improvements include:

- Expanded automated Android UI testing
- More comprehensive backend integration tests
- Improved offline synchronisation
- Enhanced recipe discovery
- Additional achievement types
- Improved AI Chef personalisation
- More advanced pantry matching
- Expanded recipe import capabilities
- Improved accessibility
- More notification types
- Production monitoring and logging
- Additional CI validation
- Automated release builds

---

# 🙏 Code Attribution

> **Code Attribution**

> `[INSERT ATTRIBUTION DETAILS HERE]`

---

# 📄 License

This project was developed as an academic/team software project.

Unless otherwise stated, the project's source code and original materials remain subject to the rights and requirements of the project team and the applicable academic institution.

Third-party libraries and services remain subject to their respective licences and terms of use.

---

<p align="center">

🍳 **Elachi**

**Mouth Full Of Flavour**

Built with Kotlin, Jetpack Compose, Node.js, PostgreSQL, Firebase and Supabase.

</p>
