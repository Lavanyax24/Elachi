<a id="top"></a>

# 🍳 Elachi
**Mouth Full Of Flavour**

**A modern recipe management Android application powered by a custom REST API, cloud storage, Firebase authentication, and intelligent cooking features.**

Built with **Kotlin, Jetpack Compose, Node.js, Express, PostgreSQL, Supabase and Firebase**.

> 🎥 **Project Demonstration:**
> **[INSERT YOUTUBE VIDEO LINK HERE]**

> 🌐 **Live API:** [https://elachi-backend.onrender.com](https://elachi-backend.onrender.com)
> **Health check:** [https://elachi-backend.onrender.com/health](https://elachi-backend.onrender.com/health)

---

<a id="developer-information"></a>
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

<a id="table-of-contents"></a>
# 📑 Table of Contents

- [Developer Information](#developer-information)
- [Project Overview](#project-overview)
- [Documentation](#documentation)
- [Technology Stack](#technology-stack)
- [Key Features](#key-features)
  - [Recipe Management](#recipe-management)
  - [Recipe Books](#recipe-books)
  - [Recipe Discovery](#recipe-discovery)
  - [Pantry Management](#pantry-management)
  - [Shopping List](#shopping-list)
  - [Pantry-Based Recipe Suggestions](#pantry-based-recipe-suggestions)
  - [Pantry Health](#pantry-health)
  - [AI Chef Assistant](#feature-ai-chef-assistant)
  - [Recipe Text Parsing](#recipe-text-parsing)
  - [Camera and OCR](#feature-camera-and-ocr)
  - [Cooking Tools](#cooking-tools)
  - [Achievements](#feature-achievements)
  - [Cooking Streaks](#feature-cooking-streaks)
  - [Notifications](#feature-notifications)
  - [User Profiles](#user-profiles)
- [System Architecture](#system-architecture)
  - [Request Flow](#request-flow)
- [Android Application](#android-application)
- [Backend REST API](#backend-rest-api)
  - [API Endpoint Reference](#api-endpoint-reference)
- [Database and Storage](#database-and-storage)
- [Authentication and Security](#authentication-and-security)
- [AI Chef Assistant (Architecture)](#ai-chef-architecture)
- [Camera and OCR (Architecture)](#camera-ocr-architecture)
- [Notifications (Architecture)](#notifications-architecture)
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
- [Version Control and Collaboration](#version-control-and-collaboration)
  - [Branching Strategy](#branching-strategy)
  - [Pull Requests](#pull-requests)
  - [Commit Conventions](#commit-conventions)
  - [Contributors and Commit History](#contributors-and-commit-history)
- [CI and GitHub Workflows](#ci-and-github-workflows)
  - [Android CI Workflow](#android-ci-workflow)
  - [Backend CI Workflow](#backend-ci-workflow)
  - [GitHub Actions Availability Note](#github-actions-availability-note)
  - [Running the CI Checks Locally](#running-ci-checks-locally)
  - [GitHub Actions Results](#github-actions-results)
- [Security Practices](#security-practices)
  - [Secure Demonstrations](#secure-demonstrations)
- [Troubleshooting](#troubleshooting)
- [Development Workflow](#development-workflow)
- [Repository Hygiene](#repository-hygiene)
- [Responsible Configuration](#responsible-configuration)
- [Future Improvements](#future-improvements)
- [Team](#team)
- [Code Attribution](#code-attribution)
- [License](#license)

---

<a id="project-overview"></a>
# 📖 Project Overview

Elachi is a full-stack recipe management platform designed to make cooking, organising recipes, managing pantry items, discovering meals, and tracking cooking progress easier and more engaging.

The project combines a native **Android application built with Kotlin and Jetpack Compose** with a **Node.js and Express REST API**, backed by **Supabase PostgreSQL**, **Supabase Storage**, and **Firebase Authentication**.

The application also includes intelligent features such as the **AI Chef Assistant**, recipe text parsing, pantry-based recipe suggestions, achievements, cooking streaks, notifications, offline/local persistence, and recipe management tools.

The system follows a client-server architecture.

The Android application acts as the primary user interface while the Node.js backend provides authenticated REST API functionality and communicates with the application's cloud services.

The backend is hosted on Render and is available at:

```text
https://elachi-backend.onrender.com
```

[⬆ Back to top](#top)

---

<a id="documentation"></a>
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

### CI Workflows

```text
.github/workflows/android-ci.yml
.github/workflows/backend-ci.yml
```

### Android Application

```text
andriod/
```

[⬆ Back to top](#top)

---

<a id="technology-stack"></a>
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

[⬆ Back to top](#top)

---

<a id="key-features"></a>
# ✨ Key Features

<a id="recipe-management"></a>
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

Users can also leave ratings and comments on recipes.

---

<a id="recipe-books"></a>
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

<a id="recipe-discovery"></a>
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

<a id="pantry-management"></a>
## 🥫 Pantry Management

Users can maintain a digital pantry containing:

- Ingredient names
- Quantities
- Units

Pantry items can be added and removed.

The pantry is used by the application to provide useful cooking functionality, including recipe suggestions based on ingredients the user already has.

Pantry changes made while offline are stored locally and synchronised with the backend once the device reconnects.

---

<a id="shopping-list"></a>
## 🛒 Shopping List

Users can maintain a shopping list of ingredients they need to buy.

The shopping list supports:

- Adding items manually
- Generating items from a recipe
- Marking items as bought
- Removing items

When a list is generated from a recipe, the backend only adds ingredients that are not already in the user's pantry.

---

<a id="pantry-based-recipe-suggestions"></a>
## 💡 Pantry-Based Recipe Suggestions

Elachi can compare recipes against pantry ingredients and calculate how closely each recipe matches the user's available ingredients.

This allows the application to surface recipes that can be prepared using ingredients already available to the user.

The backend calculates recipe-to-pantry matches rather than relying entirely on the Android client.

---

<a id="pantry-health"></a>
## 📊 Pantry Health

The application provides a pantry health calculation based on how well the user's available pantry ingredients match their recipes.

This provides users with a quick overview of how prepared they are to cook the recipes in their collection.

---

<a id="feature-ai-chef-assistant"></a>
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

<a id="recipe-text-parsing"></a>
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

<a id="feature-camera-and-ocr"></a>
## 📷 Camera and OCR

The Android application includes camera functionality for capturing recipe information.

The project uses:

- CameraX
- Google ML Kit Text Recognition

This allows text to be captured from physical or displayed recipe content and processed within the application.

---

<a id="cooking-tools"></a>
## 🧮 Cooking Tools

The application includes cooking utilities such as:

- Serving-size calculations
- Unit conversion
- Cooking timers
- Recipe text parsing
- Recipe PDF exporting

These tools are designed to make recipes more practical while users are cooking.

---

<a id="feature-achievements"></a>
## 🏆 Achievements

Elachi includes an achievement system that rewards users for cooking activity and progress.

Achievement functionality is supported by backend services that evaluate user activity and unlock relevant achievements.

---

<a id="feature-cooking-streaks"></a>
## 🔥 Cooking Streaks

Cooking activity can contribute to user streaks.

The application provides a streak calendar and tracks cooking sessions to help users maintain consistent cooking habits.

---

<a id="feature-notifications"></a>
## 🔔 Notifications

Elachi integrates Firebase Cloud Messaging for push notifications.

Notifications can be used to keep users informed about relevant application activity, such as new comments on a user's recipes.

The Android application registers its device token with the backend and handles notification events through its dedicated messaging service.

---

<a id="user-profiles"></a>
## 👤 User Profiles

Users can maintain application profiles containing information such as:

- Display name
- Profile information
- Cooking interests
- Dietary restrictions
- User preferences

The backend associates the application profile with the authenticated Firebase user.

[⬆ Back to top](#top)

---

<a id="system-architecture"></a>
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
└───────┬───────────────┬───────┘
        │               │
        ▼               ▼
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

<a id="request-flow"></a>
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

[⬆ Back to top](#top)

---

<a id="android-application"></a>
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

[⬆ Back to top](#top)

---

<a id="backend-rest-api"></a>
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

The live API is hosted at:

```text
https://elachi-backend.onrender.com
```

<a id="api-endpoint-reference"></a>
## API Endpoint Reference

All `/api` routes require a valid Firebase ID token. `POST /api/users/sync` verifies the token directly within the route; all other routes use the shared authentication middleware.

### Users

Handles user profile synchronisation, profile operations, account deletion and notification settings.

```text
POST   /api/users/sync
GET    /api/users/me
PATCH  /api/users/me
DELETE /api/users/me
GET    /api/users/:id
POST   /api/users/me/notification-token
DELETE /api/users/me/notification-token
GET    /api/users/me/notification-preferences
PATCH  /api/users/me/notification-preferences
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

Handles recipe creation, retrieval, discovery, modification and comments.

```text
GET    /api/recipes
POST   /api/recipes
GET    /api/recipes/discover
GET    /api/recipes/suggestions
GET    /api/recipes/pantry-health
GET    /api/recipes/:id
PATCH  /api/recipes/:id
DELETE /api/recipes/:id
GET    /api/recipes/:id/comments
POST   /api/recipes/:id/comments
```

### Pantry

Provides pantry management functionality.

```text
GET    /api/pantry
POST   /api/pantry
DELETE /api/pantry/:id
```

### Shopping List

Provides shopping list management, including generating items from a recipe.

```text
GET    /api/shopping-list
POST   /api/shopping-list
POST   /api/shopping-list/generate
PATCH  /api/shopping-list/:id
DELETE /api/shopping-list/:id
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

### Offline Sync

Receives changes made on the device while offline and returns server-side changes made since the last sync. Conflicts are resolved using a last-write-wins rule based on timestamps.

```text
POST /api/sync
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

[⬆ Back to top](#top)

---

<a id="database-and-storage"></a>
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
- Shopping list items
- Comments
- Ratings
- Cooking sessions
- Streak records
- Notification preferences
- Other application records

Database queries use parameterised PostgreSQL queries rather than directly concatenating user-provided values into SQL statements.

---

## Firestore

Firestore is used by the backend to store a log of AI Chef conversation messages.

---

## Supabase Storage

Supabase Storage is used for recipe-related image uploads.

The Android application communicates with the configured storage service for image uploads while the backend remains responsible for server-side API operations.

Storage access should always be configured according to the project's Supabase security policies and should not expose privileged service credentials.

[⬆ Back to top](#top)

---

<a id="authentication-and-security"></a>
# 🔐 Authentication and Security

Security is an important part of the Elachi architecture.

## Firebase Authentication

Firebase Authentication is used for user authentication.

Users can register and sign in with email and password or with Google Sign-In (single sign-on).

The Android application authenticates the user and receives a Firebase ID token.

Authenticated API requests use:

```text
Authorization: Bearer <Firebase ID Token>
```

The backend verifies the token using the Firebase Admin SDK.

---

## Protected API Routes

All application API routes require authentication.

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

[⬆ Back to top](#top)

---

<a id="ai-chef-architecture"></a>
# 🤖 AI Chef Assistant (Architecture)

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

[⬆ Back to top](#top)

---

<a id="camera-ocr-architecture"></a>
# 📷 Camera and OCR (Architecture)

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

[⬆ Back to top](#top)

---

<a id="notifications-architecture"></a>
# 🔔 Notifications (Architecture)

Firebase Cloud Messaging is integrated into the Android application.

The project includes a dedicated messaging service:

```text
ElachiMessagingService
```

The application registers its FCM device token with the backend through:

```text
POST /api/users/me/notification-token
```

The backend stores each user's notification preferences and uses the Firebase Admin SDK to send push notifications, for example when another user comments on a recipe.

Notification permissions are requested according to the Android platform requirements.

[⬆ Back to top](#top)

---

<a id="achievements-and-cooking-streaks"></a>
# 🏆 Achievements and Cooking Streaks

Elachi includes gamification features designed to encourage continued cooking activity.

## Achievements

Achievement logic is handled by backend services.

Achievements can be unlocked based on application activity such as creating recipes, adding pantry items or completing relevant milestones.

## Cooking Streaks

Cooking sessions are recorded by the backend and can contribute to a user's cooking streak.

The Android application includes a streak calendar for presenting this information to users.

[⬆ Back to top](#top)

---

<a id="project-structure"></a>
# 📦 Project Structure

The repository is divided into the Android application, backend API and CI workflows.

```text
Elachi/
│
├── .github/
│   └── workflows/
│       ├── android-ci.yml
│       └── backend-ci.yml
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
│   │   ├── wrapper/
│   │   └── libs.versions.toml
│   │
│   ├── build.gradle.kts
│   ├── gradle.properties
│   ├── gradlew
│   ├── gradlew.bat
│   └── settings.gradle.kts
│
├── backend/
│   ├── src/
│   │   ├── db.js
│   │   │
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
│   ├── README.md
│   ├── package.json
│   ├── package-lock.json
│   └── server.js
│
├── .gitignore
└── README.md
```

---

<a id="android-project-structure"></a>
## 📱 Android Project Structure

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

<a id="backend-project-structure"></a>
## 🌐 Backend Project Structure

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
├── README.md
├── package.json
└── package-lock.json
```

[⬆ Back to top](#top)

---

<a id="requirements"></a>
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

[⬆ Back to top](#top)

---

<a id="setup-and-run-instructions"></a>
# 🚀 Setup and Run Instructions

<a id="clone-the-repository"></a>
## Clone the Repository

Clone the GitHub repository:

```bash
git clone https://github.com/<ORGANISATION>/emkndn-prog7314-2026-prog7314-poe-st10439057.git
```

Enter the project directory:

```bash
cd emkndn-prog7314-2026-prog7314-poe-st10439057
```

---

<a id="backend-setup"></a>
## 🌐 Backend Setup

Navigate into the backend:

```bash
cd backend
```

Install dependencies:

```bash
npm install
```

### Configure Backend Environment Variables

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

<a id="database-setup"></a>
## 🗄️ Database Setup

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

<a id="running-the-backend"></a>
## ▶️ Running the Backend

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

### Production-Style Start

The backend can also be started using:

```bash
npm start
```

The server uses the `PORT` environment variable when supplied and otherwise defaults to port `3000`.

---

<a id="running-the-android-application"></a>
## 📱 Running the Android Application

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

### Android Backend Configuration

The Android application is configured to communicate with the deployed backend.

The backend base URL is set through the `API_BASE_URL` BuildConfig field in `andriod/app/build.gradle.kts`:

```text
https://elachi-backend.onrender.com/
```

For a local backend development environment, make sure the Android application can reach the development server from the emulator or physical device.

### Android Emulator

If your backend is running on the development computer, remember that Android emulator networking does not always treat `localhost` as the host computer.

For an Android emulator, the host machine is commonly accessed through:

```text
10.0.2.2
```

However, use the project's actual configured development environment and do not expose a development server publicly just to make the application work.

---

<a id="firebase-setup"></a>
## 🔥 Firebase Setup

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

<a id="supabase-setup"></a>
## 🗄️ Supabase Setup

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

<a id="ai-configuration"></a>
## 🤖 AI Configuration

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

[⬆ Back to top](#top)

---

<a id="testing"></a>
# 🧪 Testing

Elachi contains both backend and Android tests.

<a id="backend-tests"></a>
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
- Authentication being required on protected routes
- Pantry and shopping list routes
- Pantry matching
- Recipes and recipe books
- Streaks
- Users

The tests do not require a real database or Firebase project. `tests/setup.js` provides safe placeholder environment values so the suite can run locally and in CI without secrets.

### Backend Test Files

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

### Verified Backend Test Results

```text
Test Suites: 6 passed, 6 total
Tests:       43 passed, 43 total
```

---

<a id="android-unit-tests"></a>
## 📱 Android Unit Tests

Android unit tests are located under:

```text
andriod/app/src/test/
```

The unit tests cover:

- `RecipeTextParser` (recipe text, fractions, steps and empty input)
- `ServingScaler` (scaling quantities up and down, rounding and zero values)

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

*Figure 2: Android unit tests successfully passing in Android Studio.*

---

<a id="android-instrumented-tests"></a>
## 📱 Android Instrumented Tests

Instrumented tests are located under:

```text
andriod/app/src/androidTest/
```

These tests require an Android emulator or compatible physical Android device.

They can be run from Android Studio using the Android testing tools.

Instrumented tests are not run by the CI workflow because they require a device.

---

<a id="manual-api-testing"></a>
## 🔍 Manual API Testing

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

### Health Check

Once the backend is running locally:

```bash
curl http://localhost:3000/health
```

Or against the live deployment:

```bash
curl https://elachi-backend.onrender.com/health
```

A successful response should indicate that the backend is healthy.

### Authenticated Endpoints

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

[⬆ Back to top](#top)

---

<a id="deployment"></a>
# ☁️ Deployment

The backend is deployed on Render.

```text
Live API:      https://elachi-backend.onrender.com
Health check:  https://elachi-backend.onrender.com/health
```

The deployment uses Render's free tier, so the service may take up to a minute to respond to the first request after a period of inactivity.

The deployed environment provides the required backend environment variables through Render's Environment configuration.

Production configuration should include:

```text
DATABASE_URL
FIREBASE_SERVICE_ACCOUNT_JSON
COHERE_API_KEY
PORT
```

The exact production values must be configured securely through the hosting provider.

They should never be committed to the repository.

[⬆ Back to top](#top)

---

<a id="version-control-and-collaboration"></a>
# 🌿 Version Control and Collaboration

The project is managed with Git and hosted on GitHub. All four team members contributed to the repository throughout development.

<a id="branching-strategy"></a>
## Branching Strategy

- `main` holds the stable, submission-ready version of the application.
- New work is developed on feature branches named after the contributor and the work being done, for example `feature/jarrud-tests-ci`.
- Feature branches are merged into `main` once the work is complete and checked.

<a id="pull-requests"></a>
## Pull Requests

Changes are brought into `main` through GitHub pull requests. For example, the CI workflows and test changes were introduced through pull request #19 from `feature/jarrud-tests-ci`.

Pull requests also trigger both CI workflows, so the Android build, Android unit tests and backend tests are checked before the changes are merged.

<a id="commit-conventions"></a>
## Commit Conventions

Commits are made regularly and are kept small and focused on a single change.

Commit messages describe what changed, and a type prefix is used where it helps, for example:

```text
ci: add GitHub Actions workflows
chore: remove gitkeep file
```

<a id="contributors-and-commit-history"></a>
## Contributors and Commit History

- **Contributors:** 4 (see [Developer Information](#developer-information))
- **Total commits:** 117+

The full commit history and contributor breakdown can be viewed in the repository's **Commits** and **Insights → Contributors** pages.

[⬆ Back to top](#top)

---

<a id="ci-and-github-workflows"></a>
# 🔄 CI and GitHub Workflows

The repository uses GitHub Actions for continuous integration. Two workflows are defined:

```text
.github/workflows/android-ci.yml
.github/workflows/backend-ci.yml
```

Both workflows run automatically on every **push** and every **pull request**. Neither workflow requires secrets, and neither prints environment variables to the logs.

<a id="android-ci-workflow"></a>
## Android CI Workflow

File: `.github/workflows/android-ci.yml`

Runs on `ubuntu-latest` and performs the following steps:

1. Checks out the repository (`actions/checkout@v4`).
2. Sets up JDK 17 (Temurin) (`actions/setup-java@v4`).
3. Sets up the Android SDK (`android-actions/setup-android@v3`).
4. Installs `platform-tools`, `platforms;android-35` and `build-tools;35.0.0`.
5. Sets up Gradle (`gradle/actions/setup-gradle@v4`).
6. Makes the Gradle wrapper executable.
7. Runs the Android unit tests:

```bash
./gradlew :app:testDebugUnitTest --stacktrace
```

8. Builds the debug APK:

```bash
./gradlew :app:assembleDebug --stacktrace
```

All Gradle steps run from the `andriod/` directory.

<a id="backend-ci-workflow"></a>
## Backend CI Workflow

File: `.github/workflows/backend-ci.yml`

Runs on `ubuntu-latest` from the `backend/` directory and performs the following steps:

1. Checks out the repository (`actions/checkout@v4`).
2. Sets up Node.js 20 with npm caching (`actions/setup-node@v4`).
3. Installs dependencies from the lockfile:

```bash
npm ci
```

4. Runs the Jest and Supertest suite:

```bash
npm test
```

<a id="github-actions-availability-note"></a>
## GitHub Actions Availability Note

At the time of submission, GitHub Actions workflows were not running on the organisation's repositories because of an organisation-level issue communicated by the lecturer.

The workflow files are committed in `.github/workflows/` so they can be run when marking. The runs shown in Figure 3 were completed successfully before this issue, and the same tests were also run locally as described below.

<a id="running-ci-checks-locally"></a>
## Running the CI Checks Locally

The same checks performed by both workflows can be run locally.

Backend (from `backend/`):

```bash
npm ci
npm test
```

Android (from `andriod/`):

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Local results are shown in the [Testing](#testing) section.

<a id="github-actions-results"></a>
## GitHub Actions Results

The GitHub Actions workflow runs can be viewed from the repository's **Actions** tab.

<img width="1600" height="661" alt="image" src="https://github.com/user-attachments/assets/fbdbf239-a5ea-45b1-87ad-b260cb9e5943" />

*Figure 3: GitHub Actions showing successful Android CI and Backend CI workflow runs on the `feature/jarrud-tests-ci` branch, including pull request #19.*

[⬆ Back to top](#top)

---

<a id="security-practices"></a>
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

<a id="secure-demonstrations"></a>
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

[⬆ Back to top](#top)

---

<a id="troubleshooting"></a>
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

If the live backend has been inactive, open `https://elachi-backend.onrender.com/health` and wait for it to respond before using the app.

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

[⬆ Back to top](#top)

---

<a id="development-workflow"></a>
# 🧑‍💻 Development Workflow

A typical development workflow for the project is:

```text
1. Create a feature branch from main
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
9. Commit and push the feature branch
        │
        ▼
10. Open a pull request and merge into main
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

[⬆ Back to top](#top)

---

<a id="repository-hygiene"></a>
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

[⬆ Back to top](#top)

---

<a id="responsible-configuration"></a>
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

[⬆ Back to top](#top)

---

<a id="future-improvements"></a>
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

[⬆ Back to top](#top)

---

<a id="team"></a>
# 🤝 Team

Elachi was designed and developed by the team listed in the [Developer Information](#developer-information) section at the top of this document.

[⬆ Back to top](#top)

---

<a id="code-attribution"></a>
# 📚 Code Attribution

- Android Developers, [s.a.]. CameraX overview. [online] Available at: <https://developer.android.com/training/camerax> [Accessed 21 September 2026].

- Android Developers, [s.a.]. Create and manage documents. [online] Available at: <https://developer.android.com/training/data-storage/shared/documents-files> [Accessed 21 September 2026].

- Android Developers, [s.a.]. DataStore. [online] Available at: <https://developer.android.com/topic/libraries/architecture/datastore> [Accessed 21 September 2026].

- Android Developers, [s.a.]. Save data in a local database using Room. [online] Available at: <https://developer.android.com/training/data-storage/room> [Accessed 21 September 2026].

- Axios, [s.a.]. Axios documentation. [online] Available at: <https://axios-http.com/docs/intro> [Accessed 21 September 2026].

- Cohere, [s.a.]. Chat API. [online] Available at: <https://docs.cohere.com/reference/chat> [Accessed 21 September 2026].

- Express.js, [s.a.]. Express.js routing. [online] Available at: <https://expressjs.com/en/guide/routing/> [Accessed 21 September 2026].

- Firebase, [s.a.]. Add the Firebase Admin SDK to your server. [online] Available at: <https://firebase.google.com/docs/admin/setup> [Accessed 21 September 2026].

- Firebase, [s.a.]. Get started with Firebase Cloud Messaging in Android apps. [online] Available at: <https://firebase.google.com/docs/cloud-messaging/android/get-started> [Accessed 21 September 2026].

- Google Developers, [s.a.]. Recognize text in images with ML Kit on Android. [online] Available at: <https://developers.google.com/ml-kit/vision/text-recognition/v2/android> [Accessed 21 September 2026].

- Microsoft, [s.a.]. Playwright documentation. [online] Available at: <https://playwright.dev/docs/intro> [Accessed 21 September 2026].

- Node.js, [s.a.]. Environment variables. [online] Available at: <https://nodejs.org/api/environment_variables.html> [Accessed 21 September 2026].

- React, [s.a.]. React documentation. [online] Available at: <https://react.dev/learn> [Accessed 21 September 2026].

- Render, [s.a.]. Deploy a Node Express app on Render. [online] Available at: <https://render.com/docs/deploy-node-express-app> [Accessed 21 September 2026].

- Supabase, [s.a.]. JavaScript client library reference. [online] Available at: <https://supabase.com/docs/reference/javascript/introduction> [Accessed 21 September 2026].

- Testing Library, [s.a.]. React Testing Library. [online] Available at: <https://testing-library.com/docs/react-testing-library/intro/> [Accessed 21 September 2026].

- Vite, [s.a.]. Getting Started. [online] Available at: <https://vite.dev/guide/> [Accessed 21 September 2026].

- Vitest, [s.a.]. Getting Started. [online] Available at: <https://vitest.dev/guide/> [Accessed 21 September 2026].

[⬆ Back to top](#top)

---

<a id="license"></a>
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
