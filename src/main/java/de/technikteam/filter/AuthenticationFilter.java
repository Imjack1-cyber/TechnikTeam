package de.technikteam.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebFilter(urlPatterns = { "/home", "/events", "/lager", "/dateien", "/lehrgaenge", "/login", "/logout", "/passwort",
		"/logout" }, asyncSupported = true)
public class AuthenticationFilter implements Filter {
	private static final Logger logger = LogManager.getLogger(AuthenticationFilter.class);

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession session = request.getSession(false);
		String requestURI = request.getRequestURI();

		logger.trace("Filtering request for URI: {}", requestURI);

		String loginURI = request.getContextPath() + "/login";
		String loginJSP = request.getContextPath() + "/login.jsp";
		String cssPath = request.getContextPath() + "/css";
		String jsPath = request.getContextPath() + "/js";

		boolean loggedIn = session != null && session.getAttribute("user") != null;
		boolean loginRequest = requestURI.equals(loginURI) || requestURI.equals(loginJSP);
		boolean resourceRequest = requestURI.startsWith(cssPath) || requestURI.startsWith(jsPath);

		if (loggedIn || loginRequest || resourceRequest) {
			if (loggedIn) {
				logger.trace("Request allowed. User is logged in. Forwarding to chain.");
			} else {
				logger.trace("Request allowed. Public resource requested. Forwarding to chain.");
			}
			chain.doFilter(request, response);
		} else {
			logger.warn("Unauthorized access attempt to {}. Redirecting to login page.", requestURI);
			response.sendRedirect(loginURI);
		}
	}

	public void init(FilterConfig fConfig) throws ServletException {
		logger.info("AuthenticationFilter initialized.");
	}

	public void destroy() {
		logger.info("AuthenticationFilter destroyed.");
	}
}