-- Flyway migration V58, Part 2: Overhaul Technical Wiki Documentation

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('src/main/java/de/config/SanitizerConfig.java',
'## 1. File Overview & Purpose

This configuration class defines multiple security policies for the **OWASP Java HTML Sanitizer** library. It creates different `PolicyFactory` beans, each configured to allow a specific set of HTML tags and attributes. This enables different parts of the application to sanitize user input according to their specific needs (e.g., allowing rich text in an event description but only inline formatting in a chat message).

## 2. Architectural Role

This is a **Configuration** file that provides security components (the policies) to the **Service Tier**. Services inject these policies to clean user-provided content before it''s stored in the database.

## 3. Key Dependencies & Libraries

- **OWASP Java HTML Sanitizer (`org.owasp.html.*`)**: The library used for robust XSS prevention.

## 4. In-Depth Breakdown

- **`@Bean("richTextPolicy")`**: Creates a policy factory that allows a wide range of common formatting tags, including paragraphs, lists, and headings. This is suitable for long-form content like descriptions.
- **`@Bean("inlineFormattingPolicy")`**: Creates a stricter policy that only allows inline formatting elements like `<b>` and `<i>`. It disallows block elements like `<p>` or `<h1>`, making it ideal for single-line inputs or chat messages.
- **`@Bean("plainTextPolicy")`**: Creates the strictest policy, which allows no HTML tags at all. This effectively strips all HTML, leaving only the plain text content.'),

('src/main/java/de/config/WebMvcConfig.java',
'## 1. File Overview & Purpose

This class implements `WebMvcConfigurer` to customize Spring''s web-related configuration. Its primary role in this application is to register custom `HandlerInterceptor` beans, such as the `RateLimitingInterceptor`.

## 2. Architectural Role

This is a **Configuration** file for the **Web/API Tier**. It allows for the insertion of cross-cutting logic (like rate limiting) into the request processing pipeline for specified URL patterns.

## 3. Key Dependencies & Libraries

- **Spring Web MVC (`WebMvcConfigurer`, `InterceptorRegistry`)**: The core Spring interfaces for web configuration.
- `RateLimitingInterceptor`: The custom interceptor being registered.

## 4. In-Depth Breakdown

- **`addInterceptors(InterceptorRegistry registry)`**: This method is the hook for registering interceptors.
- **`registry.addInterceptor(...)`**: This registers the `rateLimitingInterceptor`.
- **`.addPathPatterns(...)`**: This is used to specify which API endpoints should be protected by the interceptor. The current code has the rate limiting logic commented out, but this is where it would be enabled for endpoints like `/api/v1/auth/login` to prevent brute-force attacks.'),

('src/main/java/de/security/JwtAuthFilter.java',
'## 1. File Overview & Purpose

This is a custom Spring Security filter that runs once for every incoming request. Its purpose is to inspect the request for the JWT authentication cookie (`TT_AUTH_TOKEN`). If the cookie is found and contains a valid JWT, this filter validates the token, extracts the user''s identity, and sets up the Spring Security context, effectively authenticating the user for the duration of the request.

## 2. Architectural Role

This is a core component of the **Security Tier**. It integrates the custom JWT-based authentication mechanism into the standard Spring Security filter chain. It runs before the authorization checks.

## 3. Key Dependencies & Libraries

- **Spring Security (`OncePerRequestFilter`, `SecurityContextHolder`)**: The base class for the filter and the context where the user''s authentication principal is stored.
- `AuthService`: The service used to validate the JWT and retrieve the corresponding `UserDetails`.

## 4. In-Depth Breakdown

- **`doFilterInternal(...)`**: The main logic of the filter.
    1.  It retrieves all cookies from the `HttpServletRequest`.
    2.  It finds the cookie with the name `AUTH_COOKIE_NAME`.
    3.  If the cookie is found, it passes the token value to `authService.validateTokenAndGetUser()`.
    4.  If the service returns a valid `UserDetails` object (our `SecurityUser`), it means the token is valid.
    5.  It then creates a `UsernamePasswordAuthenticationToken` and sets it in the `SecurityContextHolder`. This is the standard way to tell Spring Security that the current user is authenticated.
    6.  Finally, it calls `filterChain.doFilter()` to pass the request along to the next filter in the chain.'),

('src/main/java/de/security/RateLimitingInterceptor.java',
'## 1. File Overview & Purpose

This is a Spring `HandlerInterceptor` that provides rate limiting for specific API endpoints. It uses the client''s IP address as a key to track the number of requests made within a certain time window, preventing abuse and brute-force attacks.

## 2. Architectural Role

This is a component of the **Security Tier**, operating at the request-interception level of the **Web/API Tier**. It is registered in `WebMvcConfig`.

## 3. Key Dependencies & Libraries

- **Spring Web (`HandlerInterceptor`)**: The interface it implements to hook into the request lifecycle.
- `RateLimitingService`: The service that manages the rate limiting logic and buckets.

## 4. In-Depth Breakdown

- **`preHandle(...)`**: This method is executed *before* the controller method is called.
    1.  It extracts the client''s IP address.
    2.  It calls `rateLimitingService.resolveBucket(ipAddress)` to get or create a "bucket" of tokens for that IP.
    3.  It calls `bucket.tryConsumeAndReturnRemaining(1)` to attempt to consume one token.
    4.  **Success Path**: If the consumption is successful (`probe.isConsumed()` is true), it adds a `X-Rate-Limit-Remaining` header to the response and returns `true`, allowing the request to proceed.
    5.  **Failure Path**: If the bucket is empty, it adds a `X-Rate-Limit-Retry-After-Seconds` header and sends an HTTP 429 (Too Many Requests) error response, returning `false` to block the request.'),

('src/main/java/de/security/SecurityConfig.java',
'## 1. File Overview & Purpose

This is the central configuration class for **Spring Security**. It defines the entire security policy for the application, including which endpoints are public, which are protected, how CSRF protection is handled, how sessions are managed, and how the custom JWT filter is integrated.

## 2. Architectural Role

This is the cornerstone of the **Security Tier**. It defines the rules that govern all access to the application''s API endpoints.

## 3. Key Dependencies & Libraries

- **Spring Security (`@EnableWebSecurity`, `SecurityFilterChain`)**: The core components for security configuration.
- `JwtAuthFilter`: The custom filter for JWT authentication.
- `UserDAO`: Used to provide the `UserDetailsService`.

## 4. In-Depth Breakdown

- **`securityFilterChain(HttpSecurity http)`**: The main configuration method. It defines a chain of security rules in a fluent API style.
    - **CSRF Protection (`.csrf(...)`)**:
        - `CookieCsrfTokenRepository.withHttpOnlyFalse()`: Configures CSRF protection to use a cookie-based strategy. The `HttpOnly=false` part is crucial so that the frontend JavaScript can read the token from the `XSRF-TOKEN` cookie and include it in request headers.
        - `.ignoringRequestMatchers(...)`: This is a critical section that **disables CSRF protection for all API endpoints**. This is a deliberate choice for a stateless API where JWTs in HttpOnly cookies provide the primary defense. **NOTE:** This is a significant security decision and implies that the frontend will not be sending the `X-XSRF-TOKEN` header.
    - **Authorization (`.authorizeHttpRequests(...)`)**:
        - `.requestMatchers(...).permitAll()`: Defines a whitelist of endpoints that can be accessed without any authentication (e.g., login, swagger docs).
        - `.anyRequest().authenticated()`: A catch-all rule that specifies that any other request not on the whitelist requires successful authentication.
    - **Session Management (`.sessionManagement(...)`)**:
        - `SessionCreationPolicy.STATELESS`: This tells Spring Security not to create or manage `HttpSession` objects, as the API is stateless and relies on the JWT for authentication on each request.
    - **Filter Integration (`.addFilterBefore(...)`)**: This is where our custom `JwtAuthFilter` is inserted into the filter chain before the standard `UsernamePasswordAuthenticationFilter`.
- **`userDetailsService()`**: Creates a bean that integrates our `UserDAO` with Spring Security. When Spring needs to look up a user by username (after a token is validated), it will call this service, which in turn calls `userDAO.getUserByUsername()`.
- **`passwordEncoder()`**: Defines the `BCryptPasswordEncoder` as the official password hashing algorithm for the application.'),

('src/main/java/de/security/SecurityUser.java',
'## 1. File Overview & Purpose

This class is an adapter that wraps our application''s `User` model to make it compatible with Spring Security''s `UserDetails` interface. It acts as the bridge between our custom user representation and the framework''s security context.

## 2. Architectural Role

This is a core component of the **Security Tier**. An instance of this class is created for the authenticated user and stored as the "principal" in the `SecurityContext`.

## 3. Key Dependencies & Libraries

- **Spring Security (`UserDetails`)**: The interface it implements.
- `User` (Model): The application''s user object that it wraps.

## 4. In-Depth Breakdown

- **`getAuthorities()`**: This is the most important method. It takes the `Set<String>` of permission keys from our `User` object and converts them into a `Collection` of `SimpleGrantedAuthority` objects, which is the format Spring Security expects for authorization checks (e.g., in `@PreAuthorize` annotations).
- **`getPassword()`**: Returns the hashed password from the `User` object.
- **`getUsername()`**: Returns the username.
- **Other `is...()` methods**: These methods are part of the `UserDetails` contract and are hardcoded to `true` in this application, as account status (like expiration or locking) is handled by other services (`LoginAttemptService`).
- **`getUser()`**: A custom getter to allow easy access back to the original, full `User` model object from the security principal.');
COMMIT;