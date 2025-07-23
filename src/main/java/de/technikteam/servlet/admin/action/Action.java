package de.technikteam.servlet.admin.action;

import de.technikteam.model.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Represents a single, executable action within the Front Controller pattern.
 * Each implementation handles a specific business operation and returns a standardized API response.
 */
public interface Action {

	/**
	 * Executes the business logic for a specific action.
	 *
	 * @param request  The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @return An ApiResponse object containing the result of the action (success/fail, message, data).
	 * @throws ServletException If a servlet-specific error occurs.
	 * @throws IOException      If an I/O error occurs.
	 */
	ApiResponse execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}