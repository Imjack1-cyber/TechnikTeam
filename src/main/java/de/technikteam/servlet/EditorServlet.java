package de.technikteam.servlet;

import de.technikteam.config.AppConfig;
import de.technikteam.model.User;
import de.technikteam.util.WopiTokenManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * A secure controller servlet for launching the Collabora Online editor. It
 * generates the necessary iframe URL with a secure access token.
 */
@WebServlet("/editor")
public class EditorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(EditorServlet.class);

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		User user = (User) request.getSession().getAttribute("user");

		if (user == null || !user.hasAdminAccess()) {
			logger.warn("Non-admin user '{}' or guest attempted to access the editor.",
					(user != null ? user.getUsername() : "GUEST"));
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
			return;
		}

		String fileId = request.getParameter("fileId");
		if (fileId == null || fileId.trim().isEmpty()) {
			logger.warn("EditorServlet called without a fileId parameter.");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing fileId parameter.");
			return;
		}

		// Generate a secure, single-use access token and store it in our dedicated
		// token manager.
		String accessToken = UUID.randomUUID().toString();
		WopiTokenManager.getInstance().storeToken(accessToken, fileId, user.getUsername());
		logger.debug("Generated and stored WOPI access token for file {} and user {}", fileId, user.getUsername());

		// Capture the browser's origin. This is what Collabora needs for PostMessage.
		String browserOrigin = String.format("%s://%s:%d", request.getScheme(), request.getServerName(),
				request.getServerPort());

		// Construct the WOPI source URL, which points to our new WOPI API servlet,
		// and pass the browser's origin along with it.
		String wopiSrcUrl = String.format("%s/wopi/files/%s?origin=%s", getBaseUrl(request), fileId,
				URLEncoder.encode(browserOrigin, StandardCharsets.UTF_8));

		// Construct the full Collabora URL for the iframe.
		String collaboraUrl = String.format("%s/loleaflet.html?WOPISrc=%s", AppConfig.COLLABORA_SERVER_URL,
				URLEncoder.encode(wopiSrcUrl, StandardCharsets.UTF_8));

		// Pass the access token and the Collabora URL to the JSP.
		request.setAttribute("collaboraUrl", collaboraUrl);
		request.setAttribute("accessToken", accessToken);

		logger.info("Forwarding user '{}' to Collabora editor for file ID {}.", user.getUsername(), fileId);
		request.getRequestDispatcher("/views/public/collabora_editor.jsp").forward(request, response);
	}

	private String getBaseUrl(HttpServletRequest request) {
		String scheme = request.getScheme();

		// *** BUG FIX: Use the special Docker DNS name 'host.docker.internal'. ***
		// This is the standard, reliable way for a container to reach the host machine
		// in Docker Desktop environments (Windows/macOS).
		String serverName = "host.docker.internal";

		int serverPort = request.getServerPort();
		String contextPath = request.getContextPath();

		StringBuilder url = new StringBuilder();
		url.append(scheme).append("://").append(serverName);

		if ((serverPort != 80 && "http".equals(scheme)) || (serverPort != 443 && "https".equals(scheme))) {
			url.append(':').append(serverPort);
		}

		url.append(contextPath);

		logger.debug("Generated WOPI Base URL for Collabora callback: {}", url.toString());
		return url.toString();
	}
}