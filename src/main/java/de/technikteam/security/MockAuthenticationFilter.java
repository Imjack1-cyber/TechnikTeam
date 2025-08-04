package de.technikteam.security;

import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * A filter that simulates a permanently logged-in admin user for every request.
 * This is used to bypass all authentication and authorization checks as per the
 * simplified security requirement.
 */
public class MockAuthenticationFilter extends OncePerRequestFilter {

	private final UserDAO userDAO;
	private User adminUser; // Cache the admin user to avoid DB hits on every request

	public MockAuthenticationFilter(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	private void loadAdminUser() {
		if (this.adminUser == null) {
			// Load the default admin user (ID 1) created by InitialAdminCreator
			this.adminUser = userDAO.getUserById(1);
			if (this.adminUser == null) {
				// Fallback if the user was deleted
				this.adminUser = new User();
				this.adminUser.setId(1);
				this.adminUser.setUsername("admin");
				this.adminUser.setRoleName("ADMIN");
			}
		}
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		// Ensure the admin user is loaded
		loadAdminUser();

		// If there is no authentication in the context, create one for the admin user
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			SecurityUser securityUser = new SecurityUser(this.adminUser);
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(securityUser, null,
					securityUser.getAuthorities());
			authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authToken);
		}

		filterChain.doFilter(request, response);
	}
}