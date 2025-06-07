package de.technikteam.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

@WebFilter(value = "/*", asyncSupported = true)
public class CharacterEncodingFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		// Set the character encoding for the request
		request.setCharacterEncoding("UTF-8");

		// Set the character encoding for the response
		response.setCharacterEncoding("UTF-8");

		// Pass the request along the filter chain
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// No initialization needed
	}

	@Override
	public void destroy() {
		// No cleanup needed
	}
}