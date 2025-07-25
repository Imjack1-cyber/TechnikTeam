package de.technikteam.filter;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * A crucial utility filter that intercepts all incoming requests (`/*`) to set
 * the character encoding to UTF-8. This ensures that any data submitted in
 * requests (e.g., form fields with special characters like German umlauts) and
 * any content sent in responses is correctly interpreted and rendered by the
 * browser. It should be the first filter in the chain.
 */
public class CharacterEncodingFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(CharacterEncodingFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("CharacterEncodingFilter initialized and set to enforce UTF-8.");
	}

	/**
	 * Sets the character encoding for both the request and response to UTF-8.
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		logger.trace("Applying UTF-8 character encoding to request and response.");

		request.setCharacterEncoding("UTF-8");

		response.setCharacterEncoding("UTF-8");

		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		logger.info("CharacterEncodingFilter destroyed.");
	}
}