package de.technikteam.security;

import de.technikteam.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

	private final RateLimitingService rateLimitingService;

	@Autowired
	public RateLimitingInterceptor(RateLimitingService rateLimitingService) {
		this.rateLimitingService = rateLimitingService;
	}

	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull Object handler) throws Exception {
		String ipAddress = getClientIp(request);
		Bucket bucket = rateLimitingService.resolveBucket(ipAddress);
		ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

		if (probe.isConsumed()) {
			response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
			return true;
		} else {
			long waitForRefill = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
			response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
			response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
					"You have exhausted your API request quota. Please try again later.");
			return false;
		}
	}

	private String getClientIp(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader == null || xfHeader.isEmpty()) {
			return request.getRemoteAddr();
		}
		return xfHeader.split(",")[0];
	}
}