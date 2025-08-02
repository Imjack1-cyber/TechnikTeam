package de.technikteam.config;

import de.technikteam.security.RateLimitingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	private final RateLimitingInterceptor rateLimitingInterceptor;

	@Autowired
	public WebMvcConfig(RateLimitingInterceptor rateLimitingInterceptor) {
		this.rateLimitingInterceptor = rateLimitingInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// Apply rate limiting to sensitive state-changing endpoints
		registry.addInterceptor(rateLimitingInterceptor).addPathPatterns("/api/v1/users/*/reset-password")
				.addPathPatterns("/api/v1/public/profile/request-change").addPathPatterns("/api/v1/auth/login");
	}
}