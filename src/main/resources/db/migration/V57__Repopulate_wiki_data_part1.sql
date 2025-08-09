-- Flyway migration V57, Part 1: Overhaul Technical Wiki Documentation

-- Step 1: Clear all existing documentation to ensure a fresh start.
DELETE FROM `page_documentation` WHERE `wiki_entry_id` IS NOT NULL;
DELETE FROM `wiki_documentation`;

-- Step 2: Repopulate with up-to-date descriptions.

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('pom.xml',
'## 1. File Overview & Purpose

This is the Project Object Model (POM) file for **Maven**. It is the central configuration file that defines the project''s dependencies, build process, and metadata. It ensures that the application is built consistently with all the correct library versions.

## 2. Architectural Role

This is a top-level **Project Configuration** file. It defines the software stack for the entire application, influencing all architectural tiers by providing them with the necessary libraries (e.g., Spring Boot, JWT, database drivers).

## 3. Key Dependencies & Libraries

- **`spring-boot-starter-parent`**: Inherits sensible default configurations from the Spring Boot team, managing dependency versions and plugin settings.
- **Spring Boot Starters**: Bundles of dependencies for specific functionalities:
    - `web`: For building REST APIs with an embedded Tomcat server.
    - `jdbc`: For database access using Spring''s `JdbcTemplate`.
    - `security`: For authentication and authorization.
    - `validation`: For using `@Valid` annotations on DTOs.
    - `websocket`: For real-time communication.
    - `log4j2`: For structured, high-performance logging.
- **Database & Migrations**:
    - `flyway-core` & `flyway-mysql`: For managing and applying SQL database schema migrations.
    - `mariadb-java-client`: The JDBC driver for connecting to MariaDB/MySQL.
- **Security**:
    - `jjwt` (Java JWT): For creating, parsing, and verifying JSON Web Tokens used in authentication.
- **API Documentation**:
    - `springdoc-openapi-starter-webmvc-ui`: Automatically generates OpenAPI 3 documentation and a Swagger UI for the REST API.
- **Utilities**:
    - `ical4j`: For generating iCalendar (.ics) files for the calendar subscription feature.
    - `owasp-java-html-sanitizer`: For preventing Cross-Site Scripting (XSS) by cleaning user-provided HTML content.
    - `bucket4j`: For implementing API rate limiting.

## 4. In-Depth Breakdown

- **`<properties>`**: Centralizes version management for third-party libraries and sets the required Java version to **21**.
- **`<dependencies>`**: The main section listing all required libraries. The use of starters simplifies dependency management significantly.
- **`<build>`**: Configures how the project is built.
    - `spring-boot-maven-plugin`: Packages the application into a single executable JAR file.
    - `flyway-maven-plugin`: Allows running database migrations from the command line.
    - `maven-compiler-plugin`: Ensures the code is compiled with Java 21 compatibility.'),

('src/main/java/de/technikteam/TechnikTeamApplication.java',
'## 1. File Overview & Purpose

This is the main entry point of the Spring Boot application. The `@SpringBootApplication` annotation triggers auto-configuration, component scanning, and enables the application to be run as a standalone executable JAR.

## 2. Architectural Role

This is the **Application Bootstrap** component. It initializes the Spring Application Context, which in turn discovers and wires together all the other components (Controllers, Services, DAOs, etc.).

## 3. Key Dependencies & Libraries

- **Spring Boot (`@SpringBootApplication`)**: This single annotation is a meta-annotation that combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`.

## 4. In-Depth Breakdown

- **`@SpringBootApplication`**: This is the key annotation that makes this a Spring Boot application.
- **`@ComponentScan(basePackages = "de.technikteam")`**: This explicitly tells Spring to scan the `de.technikteam` package and all its sub-packages for components (like `@Service`, `@RestController`, `@Repository`) to manage.
- **`main(String[] args)`**: The standard Java entry point. `SpringApplication.run()` starts the entire application, including the embedded web server (Tomcat).'),

('src/main/java/de/technikteam/config/DateFormatter.java',
'## 1. File Overview & Purpose

This is a simple utility class that provides static methods for consistently formatting `java.time.LocalDateTime` objects into German-locale strings. It ensures that dates and times are displayed uniformly across the application.

## 2. Architectural Role

This is a cross-cutting **Utility** class. It is used primarily in the **Model Tier** within `getFormatted...()` methods to provide display-ready strings, abstracting away formatting logic from the views.

## 3. Key Dependencies & Libraries

- **`java.time.format.DateTimeFormatter`**: The core Java 8+ class for defining and using date-time format patterns.

## 4. In-Depth Breakdown

- **Static Formatters**: The class pre-compiles `DateTimeFormatter` instances for different formats (date-time, date-only, time-only) for efficiency.
- **`formatDateTime(LocalDateTime ldt)`**: Formats a `LocalDateTime` into a full date and time string (e.g., "10.06.2025, 17:45").
- **`formatDate(LocalDateTime ldt)`**: Formats a `LocalDateTime` into a date-only string (e.g., "10.06.2025").
- **`formatDateTimeRange(LocalDateTime start, LocalDateTime end)`**: An intelligent formatter that creates a compact range string. For example, if the start and end are on the same day, it produces "10.06.2025, 17:45 - 19:00 Uhr" instead of repeating the date.'),

('src/main/java/de/technikteam/config/GlobalExceptionHandler.java',
'## 1. File Overview & Purpose

This class acts as a global, centralized exception handler for the entire REST API. By using the `@ControllerAdvice` annotation, it intercepts exceptions thrown from any `@RestController`, preventing raw stack traces from being sent to the client and ensuring that all error responses follow the standard `ApiResponse` JSON format.

## 2. Architectural Role

This is a cross-cutting **Configuration** component that provides a uniform error handling policy for the **Web/API Tier**.

## 3. Key Dependencies & Libraries

- **Spring Framework (`@ControllerAdvice`, `@ExceptionHandler`)**: Core annotations for implementing a global exception handler.
- `ApiResponse` (Model): The standard DTO used for all API responses, including errors.
- **Log4j**: For logging unexpected exceptions.

## 4. In-Depth Breakdown

- **`@ExceptionHandler(MethodArgumentNotValidException.class)`**: This method specifically catches errors from the `@Valid` annotation on DTOs. It extracts the validation error messages and returns a clear, user-friendly summary with an HTTP 400 (Bad Request) status.
- **`@ExceptionHandler(AccessDeniedException.class)`**: This catches security-related exceptions from `@PreAuthorize` or other Spring Security checks. It returns a generic "Access Denied" message with an HTTP 403 (Forbidden) status.
- **`@ExceptionHandler(Exception.class)`**: This is the catch-all handler for any other unhandled exception. It logs the full error for debugging purposes and returns a generic "Internal Server Error" message with an HTTP 500 status, avoiding the leak of implementation details.'),

('src/main/java/de/technikteam/config/InitialAdminCreator.java',
'## 1. File Overview & Purpose

This component is a `CommandLineRunner` that executes once on application startup. Its sole responsibility is to check if a default "admin" user exists in the database. If not (indicating a first-time setup), it creates the user with full administrative permissions and a strong, randomly generated password.

## 2. Architectural Role

This is a critical **Application Bootstrap / Configuration** component. It ensures that a fresh deployment of the application is always accessible by guaranteeing the existence of at least one superuser.

## 3. Key Dependencies & Libraries

- **Spring Boot (`CommandLineRunner`)**: An interface that signals Spring to run this component after the application context is loaded but before the application starts accepting requests.
- `UserDAO`, `UserService`: Used to check for and create the new user.
- `PermissionDAO`: Used to find the IDs of the essential permissions to grant.
- `java.security.SecureRandom`: Used to generate a cryptographically secure random password.

## 4. In-Depth Breakdown

- **`run(String... args)`**: The main execution method.
    - It first queries the database via `userDAO` to see if a user with the username "admin" already exists.
    - If it does not exist, it proceeds to generate a random password.
    - It then fetches the IDs for the master admin permission (`ACCESS_ADMIN_PANEL`) and the notification permission (`NOTIFICATION_SEND`) from the database.
    - It calls the transactional `userService.createUserWithPermissions` method to create the user and assign these permissions.
    - **Crucially**, it logs the newly generated password to the console with a prominent warning. This password is only ever displayed this one time and must be copied and stored securely by the system administrator.'),

('src/main/java/de/technikteam/config/LocalDateTimeAdapter.java',
'## 1. File Overview & Purpose

This is a custom `TypeAdapter` for the **Gson** library. It provides explicit instructions on how to serialize `java.time.LocalDateTime` objects into JSON strings and deserialize them back. This is necessary to ensure consistent, standard formatting (ISO 8601) and to work around potential reflection issues in modern Java versions.

## 2. Architectural Role

This is a **Configuration/Utility** class used by the **Web/API Tier**. It is registered with the Gson instance used for serializing `ApiResponse` objects and other DTOs that contain `LocalDateTime` fields.

## 3. Key Dependencies & Libraries

- **Gson (`com.google.gson.TypeAdapter`)**: The base class for creating custom serialization/deserialization logic.
- `java.time.LocalDateTime`: The modern Java date-time class this adapter supports.

## 4. In-Depth Breakdown

- **`write(...)`**: This method is called during serialization (Java -> JSON). It takes a `LocalDateTime` object and writes its ISO 8601 string representation (e.g., `"2025-08-05T22:19:35.516"`) to the JSON output.
- **`read(...)`**: This method is called during deserialization (JSON -> Java). It reads a string from the JSON input and parses it back into a `LocalDateTime` object using the same ISO 8601 format.'),

('src/main/java/de/technikteam/config/OpenApiConfig.java',
'## 1. File Overview & Purpose

This class configures the **Springdoc OpenAPI** library, which automatically generates interactive API documentation (Swagger UI) for the application''s REST controllers.

## 2. Architectural Role

This is a **Configuration** file for the **Web/API Tier**. It provides metadata and security definitions for the auto-generated documentation.

## 3. Key Dependencies & Libraries

- **Springdoc OpenAPI (`io.swagger.v3.oas.models.*`)**: The core classes for programmatically building an OpenAPI definition.

## 4. In-Depth Breakdown

- **`@Bean`**: This annotation tells Spring to create an `OpenAPI` bean and manage its lifecycle.
- **`info(...)`**: Sets the title, version, and description that appear at the top of the Swagger UI page.
- **`addSecurityItem(...)` & `addSecuritySchemes(...)`**: This is the most important part for a secured API. It defines a security scheme named "bearerAuth" of type "HTTP" with a "Bearer" format for JWTs. This configuration adds the "Authorize" button to the Swagger UI, allowing developers to paste in a JWT and make authenticated API calls directly from the documentation page.');
COMMIT;