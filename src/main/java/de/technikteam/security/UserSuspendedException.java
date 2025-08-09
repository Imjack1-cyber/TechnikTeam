package de.technikteam.security;

import org.springframework.security.core.AuthenticationException;

public class UserSuspendedException extends AuthenticationException {
	public UserSuspendedException(String msg) {
		super(msg);
	}

	public UserSuspendedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}