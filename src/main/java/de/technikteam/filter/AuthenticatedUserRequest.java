package de.technikteam.filter;

import de.technikteam.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.security.Principal;

/**
 * An HttpServletRequest wrapper that integrates the authenticated User object
 * into the standard Jakarta EE security context. This allows downstream
 * components and frameworks to access the user via standard methods like
 * getUserPrincipal() and isUserInRole().
 */
public class AuthenticatedUserRequest extends HttpServletRequestWrapper {

	private final User userPrincipal;

	public AuthenticatedUserRequest(HttpServletRequest request, User user) {
		super(request);
		this.userPrincipal = user;
	}

	@Override
	public Principal getUserPrincipal() {
		if (this.userPrincipal == null) {
			return super.getUserPrincipal();
		}
		// Return a Principal object whose name is the username.
		return () -> userPrincipal.getUsername();
	}

	@Override
	public boolean isUserInRole(String role) {
		if (this.userPrincipal == null || this.userPrincipal.getPermissions() == null) {
			return super.isUserInRole(role);
		}
		// This allows for checking against both role names and specific permission
		// keys, providing flexible authorization.
		return userPrincipal.getRoleName().equalsIgnoreCase(role) || userPrincipal.getPermissions().contains(role);
	}

	/**
	 * Overrides the default getAttribute method to ensure the 'user' attribute is
	 * always available directly from this wrapper. This makes the propagation of
	 * the authenticated user object between filters and servlets more robust.
	 */
	@Override
	public Object getAttribute(String name) {
		if ("user".equals(name)) {
			return this.userPrincipal;
		}
		return super.getAttribute(name);
	}
}