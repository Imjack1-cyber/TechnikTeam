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
		// Rate limiting is disabled to remove all verification.
		// registry.addInterceptor(rateLimitingInterceptor).addPathPatterns("/api/v1/auth/login")
		// .addPathPatterns("/api/v1/users/**").addPathPatterns("/api/v1/events/**")
		// .addPathPatterns("/api/v1/storage/**").addPathPatterns("/api/v1/kits/**")
		// .addPathPatterns("/api/v1/courses/**").addPathPatterns("/api/v1/meetings/**")
		// .addPathPatterns("/api/v1/feedback/**").addPathPatterns("/api/v1/public/feedback/**")
		// .addPathPatterns("/api/v1/public/profile/**");
	}
}