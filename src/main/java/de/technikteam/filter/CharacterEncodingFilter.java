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
import jakarta.servlet.annotation.WebFilter;

/* 
 * A simple utility filter that intercepts all requests to set the character encoding to UTF-8. This is a crucial filter that ensures special characters (like German umlauts) are correctly handled throughout the application.
 */

@WebFilter(value = "/*", asyncSupported = true)
public class CharacterEncodingFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(CharacterEncodingFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("CharacterEncodingFilter initialized.");
	}

	/**
	 * This filter ensures that all incoming requests and outgoing responses are
	 * handled with UTF-8 encoding to support special characters and umlauts. It is
	 * configured in web.xml to be the VERY FIRST filter in the chain.
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		// Log the start of the filtering process at TRACE level for verbosity
		logger.trace("Applying UTF-8 character encoding to request and response.");

		// Set the character encoding for the request to correctly interpret incoming
		// data
		request.setCharacterEncoding("UTF-8");

		// Set the character encoding for the response to ensure the browser renders it
		// correctly
		response.setCharacterEncoding("UTF-8");

		// Log successful application and pass the request to the next filter in the
		// chain
		logger.trace("Encoding set. Passing request to the next filter.");
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		logger.info("CharacterEncodingFilter destroyed.");
	}
}