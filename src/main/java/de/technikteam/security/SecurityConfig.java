package de.technikteam.security;

import de.technikteam.dao.UserDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

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
		CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
		requestHandler.setCsrfRequestAttributeName(null);

		http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.csrfTokenRequestHandler(requestHandler).ignoringRequestMatchers(
						// --- Authentication ---
						"/api/v1/auth/login", "/api/v1/auth/logout",

						// --- Public Actions ---
						"/api/v1/public/announcements/**", "/api/v1/public/changelog/**", "/api/v1/public/chat/**",
						"/api/v1/public/events/**", "/api/v1/public/feedback/**", "/api/v1/public/meetings/**",
						"/api/v1/public/profile/**", "/api/v1/public/storage/**", "/api/v1/public/training-requests/**",

						// --- Admin Actions ---
						"/api/v1/users/**", "/api/v1/requests/**", "/api/v1/events/**", "/api/v1/meetings/**",
						"/api/v1/courses/**", "/api/v1/storage/**", "/api/v1/kits/**", "/api/v1/matrix/**",
						"/api/v1/achievements/**", "/api/v1/feedback/**", "/api/v1/admin/**" // Broad rule for all other
																								// admin endpoints
				)).authorizeHttpRequests(auth -> auth.requestMatchers(
						// Public Authentication endpoints
						"/api/v1/auth/**", "/api/v1/admin/notifications/sse", // Explicitly permit
																				// SSE endpoint
						// Publicly accessible assets and docs
						"/api/v1/public/calendar.ics", "/api/v1/public/files/avatars/**", "/swagger-ui.html",
						"/swagger-ui/**", "/v3/api-docs/**").permitAll().requestMatchers("/api/v1/admin/**")
						.authenticated() // Secure all admin endpoints
						.anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())
						.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
						.contentSecurityPolicy(csp -> csp.policyDirectives(
								"default-src 'self'; script-src 'self'; style-src 'self' https://cdnjs.cloudflare.com; font-src 'self' https://cdnjs.cloudflare.com; object-src 'none'; base-uri 'self';")))
				.httpBasic(AbstractHttpConfigurer::disable).formLogin(AbstractHttpConfigurer::disable);

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
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}