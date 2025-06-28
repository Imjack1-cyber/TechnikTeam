# TechnikTeam - School Event & Crew Management System

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![Tomcat](https://img.shields.io/badge/Tomcat-10+-F8DC75?style=for-the-badge&logo=apache-tomcat&logoColor=black)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![JSP/Servlets](https://img.shields.io/badge/Jakarta_EE-Servlets_&_JSP-7A43B6?style=for-the-badge&logo=eclipse-jakarta-ee&logoColor=white)

A comprehensive web application designed to manage a school's event technology crew ("Technik-Team"). The platform provides tools for event planning, user management, inventory tracking, and skill development, all within a role-based access control system.

## Key Features

### 👑 Admin-Specific Features
*   **Central Dashboard**: At-a-glance view of key statistics like total users and active events.
*   **User Management**: Full CRUD (Create, Read, Update, Delete) functionality for user accounts, with support for `ADMIN` and `NUTZER` roles. Includes a secure password reset feature.
*   **Event Management**:
    *   Create and edit events with details like name, date, time, location, and description.
    *   Define skill requirements for each event (e.g., "2 Tontechniker", "1 Lichttechniker").
    *   Reserve specific items from the inventory for an event.
    *   Upload event-specific files with role-based visibility.
    *   Assign a final team from the list of users who signed up.
    *   Control the event lifecycle (`GEPLANT` -> `LAUFEND` -> `ABGESCHLOSSEN`).
*   **Training & Qualification**:
    *   Create course templates (e.g., "Grundlehrgang Tontechnik").
    *   Schedule individual meetings for each course, complete with location, leader, and file attachments.
    *   **Interactive Qualification Matrix**: A powerful grid view to track and update which users have attended specific course meetings.
*   **Inventory Management**: Full CRUD for all inventory items, including location details, quantity, and images.
*   **File & Document Hub**:
    *   Create and manage file categories.
    *   Upload files to specific categories with role-based access (`ADMIN` or `NUTZER`).
*   **Audit Trail**: A detailed log of all administrative actions for accountability.

### 👥 User-Facing Features
*   **Personalized Homepage**: A dashboard showing upcoming events and training meetings relevant to the user.
*   **Event System**:
    *   View a list of upcoming events for which the user is qualified.
    *   Sign up for or sign off from events.
    *   View detailed event pages with descriptions, requirements, and assigned team members.
    *   **Live Event Tools**: For assigned members of a "running" event, access a real-time chat and a task list.
*   **Training Hub**: View and sign up for upcoming course meetings to gain new qualifications.
*   **Inventory Overview**: Browse the entire equipment inventory, grouped by location, and view item details.
*   **Document Access**: View and download all files and documents they are authorized to see.
*   **Collaborative Editor**: A shared, real-time notepad for quick collaboration.

## Technology Stack

*   **Backend**: Java 17+, Jakarta Servlets, JSP
*   **Database**: MySQL
*   **Connection Pooling**: HikariCP
*   **Logging**: Log4j2
*   **JSON Processing**: Google Gson
*   **Frontend**: HTML5, CSS3, Vanilla JavaScript (ES6+)
*   **Build Tool**: Apache Maven
*   **Web Server**: Apache Tomcat 10+

## Setup and Installation

Follow these steps to get a local instance of the application running.

### 1. Prerequisites
*   Java Development Kit (JDK) 17 or higher
*   Apache Maven
*   Apache Tomcat 10 or higher
*   MySQL Server

### 2. Database Setup
1.  Create a new database in MySQL:
    ```sql
    CREATE DATABASE technik_team_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ```
2.  Create a dedicated database user with privileges on this database:
    ```sql
    CREATE USER 'technik_user'@'localhost' IDENTIFIED BY 'ein_sicheres_passwort';
    GRANT ALL PRIVILEGES ON technik_team_db.* TO 'technik_user'@'localhost';
    FLUSH PRIVILEGES;
    ```
3.  Import the provided SQL dump file (`-- phpMyAdmin SQL Dump ...`) into the `technik_team_db` database to create the tables and seed initial data.

### 3. Application Configuration
1.  **Database Connection**: Open `src/main/java/de/technikteam/dao/DatabaseManager.java` and update the database credentials to match your setup:
    ```java
    // ...
    config.setUsername("technik_user"); // <-- SET YOUR DB USERNAME
    config.setPassword("ein_sicheres_passwort"); // <-- SET YOUR DB PASSWORD
    // ...
    ```
2.  **File Upload Directory**: Open `src/main/java/de/technikteam/config/AppConfig.java` and change the `UPLOAD_DIRECTORY` constant to an absolute path on your local machine. **This directory must be created manually and be writable by the Tomcat server process.**
    ```java
    // ...
    public static final String UPLOAD_DIRECTORY = "C:\\path\\to\\your\\uploads\\folder"; // Windows example
    // public static final String UPLOAD_DIRECTORY = "/path/to/your/uploads/folder"; // Linux/macOS example
    // ...
    ```

### 4. Build and Deploy
1.  Navigate to the project's root directory in your terminal.
2.  Build the project using Maven to create a `.war` file:
    ```shell
    mvn clean install
    ```
3.  This will generate a `TechnikTeam.war` file in the `target/` directory.
4.  Deploy this `TechnikTeam.war` file to your Apache Tomcat's `webapps` directory.
5.  Start the Tomcat server. The application will be available at `http://localhost:8080/TechnikTeam`.

## Usage

Once the application is running, you can log in with the default administrator credentials:

*   **Username**: `admin`
*   **Password**: `admin123`

From there, you can create new users, manage events, and explore all the features.

## Project Structure

The project follows a standard Maven web application structure:

*   `src/main/java/de/technikteam/`
    *   `config/`: Configuration classes (database path, date formatters).
    *   `dao/`: Data Access Objects for all database interactions.
    *   `filter/`: Servlet filters for authentication, authorization, and encoding.
    *   `listener/`: Application lifecycle listeners for startup and shutdown tasks.
    *   `model/`: Plain Old Java Objects (POJOs) representing the application's data entities.
    *   `service/`: Service layer classes for business logic (e.g., Notifications).
    *   `servlet/`: All servlet controllers handling user requests.
    *   `util/`: Utility classes.
*   `src/main/webapp/`
    *   `admin/`: JSP files exclusive to the admin panel.
    *   `css/`: CSS stylesheets.
    *   `js/`: JavaScript files.
    *   `WEB-INF/`: Deployment descriptor (`web.xml`) and reusable JSP fragments (`.jspf`).
    *   `*.jsp`: Public-facing JSP view files.
*   `src/main/resources/`: Resource files like the `log4j2.xml` configuration.

https://github-readme-stats.hackclub.dev/api/wakatime?username=4801&api_domain=hackatime.hackclub.com&theme=darcula&custom_title=Hackatime+Stats&layout=compact&cache_seconds=0&langs_count=8