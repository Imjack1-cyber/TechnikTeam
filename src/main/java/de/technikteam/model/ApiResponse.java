package de.technikteam.model;

/**
 * A generic Data Transfer Object for standardizing API responses from the server.
 * It provides a consistent structure for success/failure status, a message, and optional data payload.
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

    /**
     * Creates a success response with a message and data.
     * @param message A descriptive success message.
     * @param data The data payload to send back to the client.
     * @return A new ApiResponse object.
     */
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, data);
    }

    /**
     * Creates a success response with only a message.
     * @param message A descriptive success message.
     * @return A new ApiResponse object.
     */
    public static ApiResponse success(String message) {
        return new ApiResponse(true, message, null);
    }

    /**
     * Creates an error response with a message.
     * @param message A descriptive error message.
     * @return A new ApiResponse object.
     */
    public static ApiResponse error(String message) {
        return new ApiResponse(false, message, null);
    }
    
    /**
     * Creates an error response with a message and additional data.
     * @param message A descriptive error message.
     * @param data Additional context about the error.
     * @return A new ApiResponse object.
     */
    public static ApiResponse error(String message, Object data) {
        return new ApiResponse(false, message, data);
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