package de.technikteam.servlet.admin.action;

import de.technikteam.model.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A generic interface for a single, executable action within the
 * FrontController pattern. Each action MUST return an ApiResponse and is
 * permitted to throw ServletException or IOException.
 */
@FunctionalInterface
public interface Action {
	ApiResponse execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}