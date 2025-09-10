package de.technikteam.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class DevCsrfCookieFilter implements Filter {

	private final boolean isDev;

	public DevCsrfCookieFilter(Environment environment) {
		String[] profiles = environment.getActiveProfiles();
		this.isDev = profiles.length == 0 || java.util.Arrays.asList(profiles).contains("dev");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		chain.doFilter(request, response);

		if (!isDev || !(response instanceof HttpServletResponse)) {
			return;
		}

		HttpServletResponse httpResp = (HttpServletResponse) response;
		Collection<String> headers = httpResp.getHeaders("Set-Cookie");

		boolean first = true;
		for (String header : headers) {
			if (header.startsWith("XSRF-TOKEN=") && header.contains("Secure")) {
				String modified = header.replace("; Secure", "");
				if (first) {
					httpResp.setHeader("Set-Cookie", modified);
					first = false;
				} else {
					httpResp.addHeader("Set-Cookie", modified);
				}
			}
		}
	}
}