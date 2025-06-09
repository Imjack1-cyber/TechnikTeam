package de.technikteam.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// KEINE @WebFilter Annotation mehr, Konfiguration erfolgt in web.xml
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