package de.technikteam.config;

import de.technikteam.model.ApiResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
		String errors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.joining(", "));
		ApiResponse apiResponse = new ApiResponse(false, "Validierung fehlgeschlagen: " + errors, null);
		return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException ex) {
		ApiResponse apiResponse = new ApiResponse(false,
				"Zugriff verweigert: Sie haben nicht die erforderlichen Berechtigungen für diese Aktion.", null);
		return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse> handleAllExceptions(Exception ex) {
		logger.error("An unexpected internal server error occurred", ex);
		ApiResponse apiResponse = new ApiResponse(false,
				"Ein unerwarteter interner Serverfehler ist aufgetreten. Bitte kontaktieren Sie den Support.", null);
		return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}