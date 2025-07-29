package de.technikteam.servlet;

import com.google.inject.Singleton;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * A servlet to serve the Swagger UI static files from the WebJar. This servlet
 * forwards requests for /swagger-ui/* to the correct resource path inside the
 * swagger-ui.jar file.
 */
@Singleton
public class SwaggerUIServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SWAGGER_UI_VERSION = "5.17.14"; // Must match pom.xml

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path == null || path.equals("/")) {
			// Redirect root to the main index page, configured to load our API spec.
			resp.sendRedirect(req.getContextPath() + req.getServletPath() + "/index.html?url=/api/v1/openapi.json");
			return;
		}

		// Forward the request to the webjar's internal resource path.
		String resourcePath = "/META-INF/resources/webjars/swagger-ui/" + SWAGGER_UI_VERSION + path;
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(resourcePath);
		if (dispatcher != null) {
			dispatcher.forward(req, resp);
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
}