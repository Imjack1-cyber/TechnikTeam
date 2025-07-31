# TechnikTeam - School Event & Crew Management System v2.0

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![React](https://img.shields.io/badge/React-18+-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-5+-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![Java](https://img.shields.io/badge/Java-21+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

A comprehensive web application designed to manage a school's event technology crew ("Technik-Team"). The platform provides tools for event planning, user management, inventory tracking, and skill development, all within a granular, permission-based access control system. This version is a complete rewrite using a modern Spring Boot REST API and a React single-page application frontend.

## Key Features

### 👑 Admin-Specific Features
*   **Central Dashboard**: At-a-glance view of key metrics like upcoming events, low-stock items, and event trends.
*   **User Management**: Full CRUD functionality for user accounts with a flexible permission-based system.
*   **Event Management**: Create events with skill requirements, reserve inventory, upload files, and manage the event lifecycle.
*   **Training & Qualification**: Define course templates, schedule meetings, and track user qualifications with an interactive matrix.
*   **Inventory Management**: Full CRUD for all inventory items, including location details, quantity, images, and maintenance logs.
*   **Kit Management**: Create reusable "kits" or "cases" of equipment with printable packing lists via QR codes.
*   **File & Document Hub**: Manage file categories and upload documents with role-based access control.
*   **Feedback Kanban Board**: Manage user feedback and suggestions through a visual board.
*   **Audit Trail**: A detailed log of all administrative actions for accountability.
*   **System Monitoring**: View live server statistics, including CPU, memory, and disk usage.

### 👥 User-Facing Features
*   **Personalized Homepage**: A dashboard showing upcoming events, assigned tasks, and training meetings.
*   **Event System**: View and sign up for upcoming events, and access real-time tools like chat and task lists for "running" events.
*   **Training Hub**: View and sign up for upcoming course meetings to gain new qualifications.
*   **Inventory Browser**: Browse the entire equipment inventory and view item details.
*   **Profile Management**: Update personal details, change passwords, and manage passwordless login with Passkeys/WebAuthn.
*   **Calendar**: View all upcoming events and meetings in a list or calendar view, with an option to subscribe via an iCal feed.

## Technology Stack

*   **Backend**: Spring Boot 3.3, Java 21
    *   **Security**: Spring Security 6 with JWT Authentication
    *   **Database**: Spring Data JDBC, MariaDB/MySQL
    *   **Migrations**: Flyway
    *   **Real-time**: Spring WebSocket
*   **Frontend**: React 18, Vite 5
    *   **Routing**: React Router
    *   **State Management**: Zustand
    *   **Styling**: Plain CSS with custom properties for theming
*   **API Documentation**: Springdoc OpenAPI (Swagger UI)
*   **Build Tool**: Apache Maven

## Setup and Installation

Follow these steps to get a local instance of the application running for development.

### 1. Prerequisites
*   Java Development Kit (JDK) 21 or higher
*   Apache Maven 3.8+
*   Node.js 20+ (with npm)
*   MySQL Server 8.0+ or MariaDB 10.6+

### 2. Database Setup
1.  Create a new database in your database server:
    ```sql
    CREATE DATABASE technik_team_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ```
2.  Create a dedicated database user with privileges on this database.

### 3. Backend Configuration & Setup
1.  Navigate to the project's root directory.
2.  Open `src/main/resources/application.properties`.
3.  Update the `spring.datasource.*` properties to match your database connection details.
4.  Set a strong, unique secret for `jwt.secret`. This is critical for security.
5.  Set the `upload.directory` to an absolute path on your local machine. This directory must exist and be writable by the application.
6.  The application uses Flyway for database migrations. The necessary tables will be created automatically when the application starts for the first time.

### 4. Backend Launch
1.  From the project's root directory, run the Spring Boot application using Maven:
    ```shell
    mvn spring-boot:run
    ```
2.  The backend server will start on `http://localhost:8080`.

### 5. Frontend Setup & Launch
1.  In a separate terminal, navigate to the `frontend` directory:
    ```shell
    cd frontend
    ```
2.  Install the required Node.js dependencies:
    ```shell
    npm install
    ```
3.  Start the Vite development server:
    ```shell
    npm run dev
    ```
4.  The frontend will be available at `http://localhost:3000`. The Vite server is configured to proxy all API (`/api`) and WebSocket (`/ws`) requests to the Spring Boot backend running on port 8080.

## Usage

Once both servers are running, open `http://localhost:3000` in your browser.

*   **Default Admin**: If you used the provided Flyway migration scripts, a default admin user will be created.
    *   **Username**: `admin`
    *   **Password**: `admin123`
*   **API Documentation**: Full, interactive API documentation is available via Swagger UI at `http://localhost:8080/TechnikTeam/swagger-ui.html` once the backend is running.