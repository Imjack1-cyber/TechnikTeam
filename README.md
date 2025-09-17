# TechnikTeam - School Event & Crew Management System v2.0

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![React](https://img.shields.io/badge/React-18+-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-5+-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![Java](https://img.shields.io/badge/Java-21+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

A comprehensive web application designed to manage a school's event technology crew ("Technik-Team"). The platform provides tools for event planning, user management, inventory tracking, and skill development. This version is a complete rewrite using a modern Spring Boot REST API and a React single-page application frontend, now with support for native mobile clients.

## Key Features

### 👑 Admin-Specific Features
*   **Central Dashboard**: At-a-glance view of key metrics like upcoming events, low-stock items, and event trends.
*   **User Management**: Full CRUD functionality for user accounts, including temporary or permanent user suspension.
*   **Event Management**: Create events with skill requirements, reserve inventory from pre-made checklist templates, upload files, and manage the event lifecycle.
*   **Training & Qualification**: Define course templates, schedule meetings (including repeats with waitlists), and track user qualifications with an interactive matrix.
*   **Inventory Management**: Full CRUD for all inventory items, including location details, quantity, images, and maintenance logs.
*   **Kit Management**: Create reusable "kits" or "cases" of equipment with printable packing lists via QR codes.
*   **Content Management**: Manage sitewide content like announcements, changelogs, and the help documentation system.
*   **Feedback Kanban Board**: Manage user feedback and suggestions through a visual board.
*   **Audit Trail**: A detailed log of all administrative actions for accountability.
*   **System Monitoring**: View live server statistics, including CPU, memory, and disk usage.

### 👥 User-Facing Features
*   **Personalized Homepage**: A dashboard showing upcoming events, assigned tasks, and training meetings.
*   **Notifications**: View a history of all received notifications, separated into seen and unseen categories.
*   **Event System**: View and sign up for upcoming events, and access real-time tools like chat, checklists, and photo galleries for "running" events.
*   **Training Hub**: View and sign up for upcoming course meetings. If a meeting is a repeat of one you've already completed, you'll be added to a waitlist.
*   **Inventory Browser**: Browse the entire equipment inventory and view item details and availability.
*   **Profile Management**: Update personal details and change passwords.
*   **Calendar**: View all upcoming events and meetings in a list or calendar view, with an option to subscribe via an iCal feed.

## Technology Stack

*   **Backend**: Spring Boot 3.3, Java 21
    *   **Security**: Spring Security 6 with JWT Authentication (supports both HttpOnly Cookies for web and Bearer Tokens for mobile clients).
    *   **Database**: Spring Data JDBC, MariaDB/MySQL
    *   **Migrations**: Flyway
    *   **Real-time**: Spring WebSocket for chats, Server-Sent Events (SSE) for UI updates.
    *   **Push Notifications**: Firebase Admin SDK (for future mobile integration).
*   **Frontend**: React 18, Vite 5
    *   **Routing**: React Router
    *   **State Management**: Zustand
    *   **Styling**: Plain CSS with custom properties for theming
*   **API Documentation**: Springdoc OpenAPI (Swagger UI)
*   **Build Tool**: Apache Maven

## Application Behavior

### Session Management
*   User sessions are managed by a JSON Web Token (JWT).
*   **Web Sessions:** For security in a browser environment, web session tokens have a lifetime of **8 hours**.
*   **Mobile App Sessions:** To provide a better user experience on native apps, mobile session tokens have an extended lifetime of **336 hours (14 days)**.
*   **Server Restarts:** If the backend server is restarted (e.g., during a deployment), all active sessions will be invalidated for security reasons, and all users will need to log in again. This is expected behavior.

## Setup and Installation

Follow these steps to get a local instance of the application running for development.

### 1. Prerequisites
*   Java Development Kit (JDK) 21 or higher
*   Apache Maven 3.8+
*   Node.js 20+ (with npm)
*   MySQL Server 8.0+ or MariaDB 10.6+
*   [Google Cloud CLI](https://cloud.google.com/sdk/docs/install) (optional, for local development push notifications)

### 2. Database Setup
1.  Create a new database in your database server:
    ```sql
    CREATE DATABASE technik_team_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ```
2.  Create a dedicated database user with privileges on this database.

### 3. Backend Configuration & Setup
1.  Navigate to the project's root directory.
2.  **CRITICAL SECURITY STEP:** The JWT secret key must be provided as an environment variable. Create or edit your system's environment variables to add a new variable:
    *   **Variable name:** `JWT_SECRET`
    *   **Variable value:** A strong, unique, randomly-generated string of at least 32 characters.
3.  **Firebase Setup (for Push Notifications):**
    1.  Create a new project on the [Firebase Console](https://console.firebase.google.com/).
    2.  Go to **Project Settings > Service accounts**.
    3.  Click **"Generate new private key"** to download a service account JSON file.
    4.  **Choose ONE of the following methods for backend authentication:**
        *   **A) Production / Server Deployment (Recommended):**
            1.  Place the downloaded JSON file in a secure, non-public directory on your server.
            2.  Create an environment variable named `GOOGLE_APPLICATION_CREDENTIALS` and set its value to the full, absolute path of the JSON file. The Spring Boot application will automatically detect this.
        *   **B) Local Development (Convenient):**
            1.  Install the [Google Cloud CLI](https://cloud.google.com/sdk/docs/install).
            2.  Run the following command in your terminal: `gcloud auth application-default login`
            3.  This will open a browser window for you to log in with your Google account. After authenticating, the Spring Boot application will automatically find these credentials when running on your local machine, without needing the environment variable.
4.  Open `src/main/resources/application.properties`.
5.  Update the `spring.datasource.*` properties to match your database connection details.
6.  Set the `upload.directory` to an absolute path on your local machine. This directory must exist and be writable by the application.
7.  Set the `app.base-url` to the full public URL where your application will be hosted (e.g., `https://technikteam.qs0.de/TechnikTeam` for production).
8.  The application uses Flyway for database migrations. The necessary tables will be created automatically when the application starts for the first time.

**First-time Setup Note:** The application includes a component (`InitialAdminCreator.java`) that checks if an 'admin' user exists on first startup. If not, it creates a default `admin` user with full permissions and a strong, random password. This password is printed to the console **only once** on the very first startup. Please copy this password immediately and store it securely.

### 4. Backend Launch
1.  From the project's root directory, run the Spring Boot application using Maven:
    ```shell
    mvn spring-boot:run
    ```
2.  The backend server will start on the port defined in your `pom.xml`'s `spring-boot-maven-plugin` configuration (default `8080`). You can change this in the pom or override it in `application.properties`.

### 5. Frontend Setup & Launch
1.  In a separate terminal, navigate to the `frontend` directory:
    ```shell
    cd frontend
    ```
2.  **Environment Configuration:** Create a file named `.env.local` by copying `frontend/.env.local.example`. This file tells the Vite development server where your backend is running.
    ```    # frontend/.env.local
    VITE_API_TARGET_URL=http://localhost:8081
    ```
    Change the port if your backend runs on a different one.
3.  **Firebase Setup (for Push Notifications on Native):**
    -   **Android:** In your Firebase project, add an Android app with the package name `de.technikteam`. Download the `google-services.json` file and place it in the `frontend` directory.
    -   **iOS:** Add an iOS app with the bundle identifier `de.technikteam`. Download the `GoogleService-Info.plist` file and place it in the `frontend` directory.
4.  Install the required Node.js dependencies:
    ```shell
    npm install
    ```
5.  Start the Vite development server:
    ```shell
    npm run dev
    ```
6.  The frontend will be available at `http://localhost:3000`. The Vite server is configured to proxy all API (`/api`) and WebSocket (`/ws`) requests to the Spring Boot backend defined in your `.env.local` file.

## Troubleshooting

### Failed Flyway Migration
If the backend fails to start with an error message like `Migrations have failed validation. Detected failed migration to version X`, it means your local database is in a failed migration state. To fix this:

1.  Open a terminal in the project's root directory.
2.  Run the Flyway repair command for your active profile. For development:
    ```shell
    mvn flyway:repair -Pdev
    ```
3.  After the command completes successfully, you can start the backend again.

## Production Deployment

After running `npm run build` in the `frontend` directory, a production-ready version of the site is available in `frontend/dist`. For deployment, it is recommended to use a reverse proxy like Nginx.

1.  Serve the static files from `frontend/dist`.
2.  Configure the reverse proxy to forward all requests starting with `/TechnikTeam/` to the running Spring Boot backend. The primary domains are `technikteam.qs0.de` (production) and `technikteamdev.qs0.de` (development).
3.  Ensure the `app.base-url` in `application.properties` is set to your final public domain.

A detailed guide and example Nginx configuration can be found by asking the maintaining AI for instructions on setting up a reverse proxy.

## Usage

Once both servers are running, open `http://localhost:3000` in your browser.

*   **API Documentation**: Full, interactive API documentation is available via Swagger UI at `http://localhost:8080/TechnikTeam/swagger-ui.html` once the backend is running.