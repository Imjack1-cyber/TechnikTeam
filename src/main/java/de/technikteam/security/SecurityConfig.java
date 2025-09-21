package de.technikteam.security;

import de.technikteam.dao.UserDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;
	private final UserDAO userDAO;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDAO userDAO) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.userDAO = userDAO;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Define a Content Security Policy that allows Expo for Web's development behavior.
        String csp = "default-src 'self'; " +
                     "script-src 'self' 'unsafe-eval'; " + // Required for Expo Web dev builds
                     "style-src 'self' https://cdnjs.cloudflare.com 'unsafe-inline'; " + // Allow FontAwesome and inline styles from JS
                     "img-src 'self' data:; " +
                     "font-src 'self' https://cdnjs.cloudflare.com; " +
                     "connect-src 'self'; " + // Allow API calls and WebSockets to the same origin
                     "frame-ancestors 'none'; " +
                     "form-action 'self'; " +
                     "base-uri 'self';";

		http.headers(headers -> headers
                    .contentSecurityPolicy(cspConfig -> cspConfig.policyDirectives(csp))
                )
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/index.html", "/favicon.ico", "/*.js", "/*.css", "/assets/**", "/theme-loader.js",
                                 "/api/v1/auth/login", "/api/v1/auth/logout", "/api/v1/auth/verify-2fa", "/api/v1/auth/csrf-token", 
                                 "/ws/**", "/actuator/health", "/api/v1/public/notifications/sse", "/api/v1/public/verify/**",
                                 "/api/v1/public/files/share/**")
						.permitAll()
                        .requestMatchers("/api/v1/auth/me").authenticated() // Crucial fix: require authentication but no specific role
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/venues/*").hasRole("ADMIN") // Allow PUT for updates with multipart
						.requestMatchers("/admin/**", "/api/v1/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return username -> {
			de.technikteam.model.User user = userDAO.getUserByUsername(username);
			if (user == null) {
				throw new UsernameNotFoundException("User not found: " + username);
			}
			return new SecurityUser(user);
		};
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}