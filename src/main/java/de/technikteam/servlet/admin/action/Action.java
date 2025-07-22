package de.technikteam.servlet.admin.action;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Represents a single, executable action within the Front Controller pattern.
 * Each implementation handles a specific business operation.
 */
public interface Action {

	/**
	 * Executes the business logic for a specific action.
	 *
	 * @param request  The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @return A string indicating the next view. This can be a path to a JSP for
	 *         forwarding, or a "redirect:/path" string to indicate a client-side
	 *         redirect.
	 * @throws ServletException If a servlet-specific error occurs.
	 * @throws IOException      If an I/O error occurs.
	 */
	String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}