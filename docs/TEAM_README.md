# HydroGrid

HydroGrid is a full-stack Smart-X Internet of Things (IoT) monitoring application designed to support the management of a hydroponic growing environment. It allows an operator to register sensors, submit typed telemetry, monitor system health, investigate anomalies and sensor disconnections, and manage encrypted sensor-related files through one responsive dashboard.

This repository contains the completed implementation for **PROG7312 Part 1**. The enabled architectural pillar is **Sensor Data Ingestion and Telemetry**. The remaining two pillars are displayed in the application as planned future functionality for later stages of the PoE.

> **Video demonstration:** In progress - the final YouTube link will be added here.

## Developer Information

| Item | Details |
|---|---|
| Developer | Lavanya Pillay |
| Student number | ST10438009 |
| Module | PROG7312 |
| Assessment | Portfolio of Evidence - Part 1 |
| Academic year | 2026 |
| Development type | Individual project |
| Repository | [HydroGrid GitHub Repository](https://github.com/EMKNDN/emkndn-prog7312-g2-2026-prog7312-poe-lavan24) |
| Main IDE | Visual Studio 2026 |

## Table of Contents

- [Project Overview](#project-overview)
- [Part 1 Scope](#part-1-scope)
- [Engagement Strategy](#engagement-strategy)
- [Technology Stack](#technology-stack)
- [Completed Features](#completed-features)
- [Seeded Demonstration Data](#seeded-demonstration-data)
- [Solution Structure](#solution-structure)
- [Advanced C# Concepts](#advanced-c-concepts)
- [API Endpoints](#api-endpoints)
- [System Requirements](#system-requirements)
- [Setup Instructions](#setup-instructions)
- [Running the Application](#running-the-application)
- [Testing and Verification](#testing-and-verification)
- [Security Controls](#security-controls)
- [Troubleshooting](#troubleshooting)
- [Rubric Evidence](#rubric-evidence)
- [Future Development](#future-development)

## Project Overview

HydroGrid provides a central interface for monitoring and controlling sensors deployed across a hydroponic growth environment. These sensors collect information such as root-zone moisture, nutrient electrical conductivity (EC), pH, water temperature, electricity consumption and actuator states.

The system converts incoming telemetry into useful operational information. Instead of requiring the operator to inspect large amounts of raw sensor data, HydroGrid highlights warning, critical and disconnected states and allows the operator to explore the evidence behind each event. This supports faster fault identification and helps protect crops and equipment from avoidable damage.

The application follows a client-server architecture:

1. The React client collects input and displays dashboard information.
2. The ASP.NET Core API validates requests and coordinates system operations.
3. The Core project processes typed telemetry, anomaly rules, arrays and deployment trees.
4. Entity Framework Core stores sensor, telemetry and attachment metadata in SQLite.
5. Encrypted attachment bytes are stored outside the database in a protected local uploads directory.

## Part 1 Scope

The Smart-X solution is organised around three architectural pillars.

| Pillar | Part 1 status | Description |
|---|---|---|
| Sensor Data Ingestion and Telemetry | **Completed** | Sensor registration, typed telemetry submission, anomaly detection, sensor status monitoring, file attachments and dashboard exploration. |
| Real-Time Command Stream and History | Planned for Part 2 | Will introduce device command execution and command-history functionality. |
| Network Topology and Mesh Routing | Planned for final PoE | Will visualise and manage the relationships between connected Smart-X devices. |

The two future pillars appear as disabled cards on the landing page so that the complete system direction is visible without presenting unfinished functionality as part of Part 1.

## Engagement Strategy

The selected user engagement strategy is **Interactive Anomaly Exploration**.

This strategy encourages the operator to investigate live and historical telemetry directly instead of passively reading a static report. The completed dashboard supports the following interaction flow:

1. **Overview:** Summary cards show healthy, warning, critical and disconnected sensor counts.
2. **Filter:** Sensors can be narrowed by status, category and search text.
3. **Select:** The operator can open a flagged sensor or recent anomaly.
4. **Inspect:** The sensor profile presents current readings, historical charts and highlighted anomaly points.
5. **Explore:** Time ranges can be changed to compare live and earlier telemetry.
6. **Understand:** Diagnostic information explains the triggering rule, previous value, change in value, last-seen time and connection state.
7. **Act:** The interface presents relevant next steps, such as checking the sensor location, reviewing an attachment, reconnecting a device or investigating the affected hydroponic zone.

The dashboard refreshes through five-second polling to provide simulated near-real-time updates. It retains the last successful data during temporary request failures and shows the most recent update time.

## Technology Stack

| Technology | Purpose |
|---|---|
| Visual Studio 2026 | Main development environment for the .NET solution and integrated terminals. |
| .NET 10 | Runtime and framework version used by the backend projects. |
| ASP.NET Core Web API | Controller-based REST API for sensors, telemetry, dashboard data, attachments and validation. |
| C# | Backend, domain-processing and automated-test language. |
| React | Component-based frontend user interface. |
| Vite | React development server and production build tool. |
| JavaScript | Client-side application logic. |
| Axios | Centralised asynchronous communication between React and the API. |
| React Router | Client-side page navigation. |
| Recharts | Historical telemetry and anomaly visualisation. |
| Entity Framework Core | Data access, entity configuration and migrations. |
| SQLite | Local relational database for sensor, telemetry and attachment metadata. |
| AES-GCM | Authenticated encryption for uploaded configuration, image and log files. |
| xUnit | Backend unit and integration testing. |
| Vitest and Testing Library | React component and interaction testing. |
| OpenAPI | Development-time API discovery and endpoint documentation. |
| Git and GitHub | Version control, source hosting and development history. |

## Completed Features

### Sensor Registration and Management

- Registers environmental, power-consumption and actuator sensors.
- Captures a unique MAC address, sensor name, deployment location and category.
- Normalises MAC addresses to uppercase.
- Rejects missing, invalid or duplicate sensor information.
- Supports sensor listing, searching, filtering, editing and profile viewing.
- Uses client-side feedback and authoritative server-side validation.

### Typed Telemetry Ingestion

- Accepts floating-point telemetry for environmental readings.
- Accepts integer telemetry for power consumption and counters.
- Accepts Boolean telemetry for actuator states.
- Preserves the original data category throughout the generic ingestion pipeline.
- Validates sensor identity, timestamps, values and units before persistence.
- Uses asynchronous API and Entity Framework Core operations.

### Dashboard and Anomaly Detection

- Displays total, healthy, warning, critical and disconnected sensor counts.
- Shows the latest reading, unit, last-seen time and anomaly count for each sensor.
- Applies deterministic rules for sudden spikes and out-of-range values.
- Calculates disconnected status from the sensor's last successful contact time.
- Records Boolean actuator transitions with contextual messages.
- Stores the anomaly severity and a human-readable diagnostic reason.
- Provides status, category and text filters.
- Supports one-hour, 24-hour and seven-day history views.
- Distinguishes anomaly points on the chart and provides an accessible table alternative.

### Secure Attachments

- Uploads sensor configuration files, photographs and diagnostic logs.
- Supports `.json`, `.txt`, `.log`, `.jpg`, `.jpeg` and `.png` files.
- Enforces a maximum file size of 5 MB in both the client and API.
- Generates random stored filenames rather than trusting supplied paths.
- Encrypts attachment bytes with AES-GCM before writing them to disk.
- Stores only attachment metadata, nonce and authentication tag in SQLite.
- Decrypts approved files during download and restores the safe original filename.

### Deployment Validation

- Validates nested Facility, Zone, Sub-Zone and Device structures recursively.
- Checks required names and permitted parent-child relationships.
- Returns path-aware validation messages.
- Handles leaf nodes as the recursion base case.
- Protects the API from malformed, cyclic or excessively deep structures.

### User Experience

- Responsive dashboard for desktop, tablet and smaller screens.
- Clear loading, empty, success and error states.
- Consistent status colours and accessible labels.
- Visible keyboard focus and accessible chart alternatives.
- Controlled API errors without exposed stack traces or internal paths.

## Seeded Demonstration Data

Development mode automatically creates repeatable demonstration data only when the database contains no sensors.

| Sensor | Category | Demonstration purpose |
|---|---|---|
| ESP32 Greenhouse A1 | Environmental | Moisture and temperature history containing a warning and a noticeable moisture spike. |
| Meter Office B2 | Power Consumption | Integer wattage readings containing a critical load spike. |
| Valve Hydroponics C3 | Actuator | Boolean open/closed history containing a state transition and contextual message. |
| ESP32 Remote D4 | Environmental | Historical readings ending far enough in the past for the sensor to appear disconnected. |

Numeric sensors contain sufficient historical points to produce useful charts. Stable random seeds ensure that demonstrations and automated checks remain repeatable.

## Solution Structure

```text
HydroGrid/
├── src/
│   ├── SmartX.Core/
│   │   ├── Domain/
│   │   └── Services/
│   ├── SmartX.Api/
│   │   ├── Controllers/
│   │   ├── Data/
│   │   ├── Dtos/
│   │   ├── Entities/
│   │   ├── Migrations/
│   │   ├── Services/
│   │   └── Program.cs
│   └── smartx-client/
│       ├── src/
│       │   ├── api/
│       │   ├── components/
│       │   ├── pages/
│       │   └── App.jsx
│       ├── package.json
│       └── vite.config.js
├── tests/
│   └── SmartX.Tests/
├── SmartX.sln
├── .config/
│   └── dotnet-tools.json
├── .gitignore
└── README.md
```

| Location | Responsibility |
|---|---|
| `SmartX.Core/Domain` | Domain types, categories, generic packets, numeric readings and deployment nodes. |
| `SmartX.Core/Services` | Telemetry processing, anomaly evaluation, jagged-array processing and recursive validation. |
| `SmartX.Api/Controllers` | HTTP endpoints and request coordination. |
| `SmartX.Api/Data` | Database context, configurations, migrations and deterministic seeding. |
| `SmartX.Api/Dtos` | Validated request and response contracts. |
| `SmartX.Api/Entities` | Persistent sensor, telemetry and attachment entities. |
| `SmartX.Api/Services` | Database operations, dashboard queries and encrypted attachment handling. |
| `smartx-client/src/api` | Configured Axios client and endpoint functions. |
| `smartx-client/src/components` | Reusable forms, status badges, charts and feedback components. |
| `smartx-client/src/pages` | Landing, ingestion, sensor list, registration, detail and not-found pages. |
| `SmartX.Tests` | Tests for the advanced C# concepts, anomaly rules, validation and file security. |

## Advanced C# Concepts

### Generics

`TelemetryPacket<T>` carries a strongly typed sensor value without storing it as `object`. The application constructs and processes closed packet types for `double`, `int` and `bool` telemetry.

### Operator Overloading

`PowerReading` overloads arithmetic and comparison operators. The dashboard uses these operators to aggregate compatible power readings, calculate changes and compare consumption levels.

### Jagged Arrays

Unequal historical telemetry batches are represented using jagged arrays. Valid values are processed and converted into a typed `List<T>` before filtering and persistence.

### Collections

Generic lists are used throughout telemetry processing and API responses. A bounded recent-anomaly collection maintains a predictable newest-first set of events for the dashboard.

### Recursion

The deployment validator recursively traverses a Facility → Zone → Sub-Zone → Device tree. It validates the current node, stops at a leaf node and accumulates readable errors containing the full deployment path.

## API Endpoints

### Health and Sensors

| Method | Route | Purpose |
|---|---|---|
| GET | `/api/health` | Returns application, environment and database health. |
| GET | `/api/sensors` | Lists sensors with optional status, category and search filters. |
| GET | `/api/sensors/{id}` | Returns a sensor profile, recent telemetry and attachments. |
| POST | `/api/sensors` | Registers a validated sensor. |
| PUT | `/api/sensors/{id}` | Updates the sensor name, location or category. |

### Telemetry and Deployment

| Method | Route | Purpose |
|---|---|---|
| POST | `/api/telemetry/floating-point` | Submits environmental or other decimal telemetry. |
| POST | `/api/telemetry/integer` | Submits whole-number telemetry such as wattage. |
| POST | `/api/telemetry/boolean` | Submits an actuator state. |
| POST | `/api/deployments/validate` | Recursively validates a deployment hierarchy. |

### Dashboard

| Method | Route | Purpose |
|---|---|---|
| GET | `/api/dashboard/summary` | Returns status totals, update time and recent anomalies. |
| GET | `/api/dashboard/sensors` | Returns filterable dashboard sensor cards. |
| GET | `/api/dashboard/sensors/{id}/history?from=&to=` | Returns ranged chart data and diagnostics. |

### Attachments and Development

| Method | Route | Purpose |
|---|---|---|
| POST | `/api/sensors/{sensorId}/attachments` | Uploads and encrypts an approved file. |
| GET | `/api/sensors/{sensorId}/attachments` | Lists attachment metadata for a sensor. |
| GET | `/api/attachments/{attachmentId}/download` | Decrypts and downloads an attachment. |
| POST | `/api/development/simulate` | Creates a demonstration reading or spike in Development only. |

### Sample Sensor Request

```json
{
  "macAddress": "AA:BB:CC:DD:EE:FF",
  "name": "ESP32 Greenhouse A2",
  "deploymentLocation": "Greenhouse A - Zone 2",
  "category": "Environmental"
}
```

### Sample Floating-Point Telemetry Request

```json
{
  "sensorId": "00000000-0000-0000-0000-000000000000",
  "value": 6.4,
  "recordedAtUtc": "2026-09-13T12:00:00Z",
  "unit": "pH",
  "sequenceNumber": 101
}
```

Replace the sample GUID with the ID returned when a sensor is registered.

## System Requirements

### Hardware

- Windows 10 or Windows 11 computer
- 2 GHz dual-core processor or better
- Minimum 4 GB RAM; 8 GB recommended
- At least 2 GB of available storage
- Internet connection for the initial dependency restore

### Software

- Visual Studio 2026 with the **ASP.NET and web development** workload
- .NET 10 SDK
- Node.js 20.19+ or 22.12+
- npm
- Git for Windows
- A modern browser such as Microsoft Edge, Google Chrome or Firefox

Confirm the installed tools:

```powershell
dotnet --version
node --version
npm --version
git --version
```

## Setup Instructions

### 1. Clone the Repository

```powershell
git clone https://github.com/EMKNDN/emkndn-prog7312-g2-2026-prog7312-poe-lavan24.git
cd emkndn-prog7312-g2-2026-prog7312-poe-lavan24
```

### 2. Trust the ASP.NET Development Certificate

```powershell
dotnet dev-certs https --trust
```

### 3. Restore .NET Tools and Packages

```powershell
dotnet tool restore
dotnet restore
```

### 4. Create the Local Encryption Key

Run these commands once from the repository root:

```powershell
dotnet user-secrets init --project src/SmartX.Api
$key = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
dotnet user-secrets set "FileEncryption:Key" $key --project src/SmartX.Api
```

The generated key is stored in local user secrets and must never be committed to GitHub.

### 5. Create or Update the SQLite Database

```powershell
dotnet ef database update --project src/SmartX.Api --startup-project src/SmartX.Api
```

The development seeder automatically adds the demonstration sensors and telemetry when the database is empty.

### 6. Install React Dependencies

```powershell
cd src/smartx-client
npm install
cd ../..
```

### 7. Configure the API Address

Open `src/SmartX.Api/Properties/launchSettings.json` and identify the API HTTPS address. Create or update `src/smartx-client/.env.development`:

```env
VITE_API_BASE_URL=https://localhost:YOUR_API_HTTPS_PORT
```

Replace `YOUR_API_HTTPS_PORT` with the port from `launchSettings.json`.

## Running the Application

The API and React client run in separate terminals.

### Terminal 1 - ASP.NET Core API

```powershell
dotnet run --project src/SmartX.Api
```

Alternatively, open `SmartX.sln` in Visual Studio 2026, set `SmartX.Api` as the startup project and select the green Start button.

### Terminal 2 - React Client

```powershell
cd src/smartx-client
npm run dev
```

Open [http://localhost:5173](http://localhost:5173) in a browser.

No login credentials are required for Part 1. The application opens directly on the Smart-X landing page.

## Testing and Verification

### Backend

```powershell
dotnet format --verify-no-changes
dotnet build --configuration Release
dotnet test --configuration Release
```

The backend tests verify:

- Generic floating-point, integer and Boolean telemetry packets
- Power-reading arithmetic and comparisons
- Jagged-array processing and collection ordering
- Valid and invalid recursive deployment structures
- Normal, warning, critical and disconnected anomaly states
- File validation and AES-GCM encryption round trips

### Frontend

```powershell
cd src/smartx-client
npm test -- --run
npm run build
```

The frontend tests verify the three-pillar landing page, input validation, dashboard states, status filtering and anomaly-selection workflow.

### Clean Verification Sequence

```powershell
dotnet clean
dotnet restore
dotnet tool restore
dotnet ef database update --project src/SmartX.Api --startup-project src/SmartX.Api
dotnet build --configuration Release
dotnet test --configuration Release
cd src/smartx-client
npm ci
npm test -- --run
npm run build
cd ../..
git status
```

## Security Controls

| Control | Purpose |
|---|---|
| HTTPS redirection | Protects traffic between the React client and API during local development. |
| Restricted CORS policy | Allows requests from the configured React development origin only. |
| Client and server validation | Rejects incomplete, invalid and unexpected input before processing. |
| ProblemDetails responses | Returns consistent errors without exposing stack traces or internal file paths. |
| Database uniqueness | Prevents duplicate sensor MAC addresses even if application checks are bypassed. |
| File allow-list and size limit | Rejects unsupported or oversized uploads. |
| Safe generated filenames | Prevents supplied filenames from controlling storage paths. |
| AES-GCM encryption | Protects attachment confidentiality and verifies integrity. |
| User secrets | Keeps the encryption key outside source control. |
| Development-only simulator | Prevents the demonstration endpoint from being exposed in production. |

The SQLite database, uploaded files, local environment files, secrets, `bin`, `obj` and `node_modules` are excluded from Git.

## Troubleshooting

| Problem | Resolution |
|---|---|
| Browser reports a CORS error | Confirm the React client is running at `http://localhost:5173` and that the API policy contains this exact origin without a trailing slash. |
| React displays a network or fetch error | Confirm the API is running, the HTTPS port matches `VITE_API_BASE_URL` and the development certificate is trusted. |
| `dotnet ef` is not recognised | Run `dotnet tool restore` from the repository root. |
| SQLite reports that a table does not exist | Apply migrations using both the `--project` and `--startup-project` arguments shown above. |
| SQLite database is locked | Stop duplicate API processes and close tools holding an active database transaction. |
| Dashboard data appears stale | Check the API response, five-second polling and the displayed last-updated time. |
| Upload returns HTTP 415 | Submit the file using browser `FormData`; do not manually force a JSON `Content-Type`. |
| Upload succeeds but download fails | Confirm the same local encryption key is configured and the encrypted upload file still exists. |
| No demonstration data appears | Confirm the application is running in Development and that seeding completed against the selected database. |

## Rubric Evidence

| Rubric requirement | Implementation evidence |
|---|---|
| Startup and API integration | Three-pillar landing page, central Axios client and `/api/health`. |
| Sensor ingestion and UI | Sensor registration page, sensor controllers, DTO validation and typed telemetry forms. |
| Generics and operator overloading | `TelemetryPacket<T>`, generic ingestion service and `PowerReading` operators. |
| Arrays, collections and recursion | Jagged batch processor, typed lists, recent-anomaly collection and recursive deployment validator. |
| Media and log upload | Sensor attachment interface, multipart controller and AES-GCM attachment service. |
| Dynamic engagement | Dashboard overview, filters, selectable anomalies, historical charts and diagnostic drill-down. |
| UI design | Responsive layout, reusable components, state feedback and accessible chart alternative. |
| README | Complete fresh-clone, configuration, migration, run, test and troubleshooting instructions. |

## Future Development

Part 1 establishes the complete sensor-ingestion and telemetry foundation. The following features are intentionally reserved for later PoE submissions:

- **Part 2:** Real-time device command streaming and command history.
- **Final PoE:** Network topology visualisation and mesh-routing functionality.

These items are represented as disabled planned features in the current user interface and are not required for the completed Part 1 implementation.

## Repository

Source code and commit history are available at:

[https://github.com/EMKNDN/emkndn-prog7312-g2-2026-prog7312-poe-lavan24](https://github.com/EMKNDN/emkndn-prog7312-g2-2026-prog7312-poe-lavan24)

---

**HydroGrid - Explore anomalies, understand sensor behaviour and respond faster.**
