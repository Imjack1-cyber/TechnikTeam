package de.technikteam.model;

/**
 * A standard wrapper for all API responses. This object is returned by all
 * Actions and processed by the FrontControllerServlet. It contains a success
 * flag, a message, and a generic data payload.
 */
public class ApiResponse {

	private final boolean success;
	private final String message;
	private final Object data;

	public ApiResponse(boolean success, String message, Object data) {
		this.success = success;
		this.message = message;
		this.data = data;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public Object getData() {
		return data;
	}
}