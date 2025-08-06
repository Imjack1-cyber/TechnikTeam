package de.technikteam.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSignatureValidator {
	private static final Logger logger = LogManager.getLogger(FileSignatureValidator.class);

	public enum FileTypeValidationResult {
		ALLOWED, ALLOWED_WITH_WARNING, DISALLOWED
	}

	private static final Map<String, List<byte[]>> ALLOWED_SIGNATURES = new HashMap<>();
	private static final Map<String, List<byte[]>> WARNING_SIGNATURES = new HashMap<>();

	static {
		// Secure, common types
		ALLOWED_SIGNATURES.put("image/jpeg", List.of(new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }));
		ALLOWED_SIGNATURES.put("image/png", List.of(new byte[] { (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47 }));
		ALLOWED_SIGNATURES.put("image/gif", List.of(new byte[] { (byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38 }));
		ALLOWED_SIGNATURES.put("application/pdf",
				List.of(new byte[] { (byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46 }));
		List<byte[]> pkzip = List.of(new byte[] { (byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04 });
		ALLOWED_SIGNATURES.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", pkzip);
		ALLOWED_SIGNATURES.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", pkzip);
		ALLOWED_SIGNATURES.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", pkzip);

		// Potentially risky types that are allowed but should trigger a warning on
		// download
		WARNING_SIGNATURES.put("application/javascript", List.of()); // JS files have no reliable magic number, so we
																		// allow them by MIME type but flag for warning.
		WARNING_SIGNATURES.put("text/html", List.of()); // HTML files also lack a consistent magic number.
		WARNING_SIGNATURES.put("application/zip", pkzip);
	}

	private static final int MAX_HEADER_SIZE = 8;

	public static FileTypeValidationResult validateFileType(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return FileTypeValidationResult.DISALLOWED;
		}
		String declaredMimeType = file.getContentType();

		// Check if it's an explicitly allowed type
		List<byte[]> allowedSignatures = ALLOWED_SIGNATURES.get(declaredMimeType);
		if (allowedSignatures != null) {
			if (verifySignature(file, allowedSignatures)) {
				return FileTypeValidationResult.ALLOWED;
			} else {
				logger.warn("File signature mismatch for allowed type. Declared: {}, but magic bytes do not match.",
						declaredMimeType);
				return FileTypeValidationResult.DISALLOWED;
			}
		}

		// Check if it's a type that is allowed but needs a warning
		List<byte[]> warningSignatures = WARNING_SIGNATURES.get(declaredMimeType);
		if (warningSignatures != null) {
			if (verifySignature(file, warningSignatures)) {
				return FileTypeValidationResult.ALLOWED_WITH_WARNING;
			} else {
				logger.warn("File signature mismatch for warning type. Declared: {}, but magic bytes do not match.",
						declaredMimeType);
				return FileTypeValidationResult.DISALLOWED;
			}
		}

		logger.warn("File upload blocked for undeclared MIME type: {}", declaredMimeType);
		return FileTypeValidationResult.DISALLOWED;
	}

	private static boolean verifySignature(MultipartFile file, List<byte[]> expectedSignatures) {
		// If the signature list is empty, it means we are trusting the MIME type alone
		// for this category.
		if (expectedSignatures.isEmpty()) {
			return true;
		}

		try (InputStream is = file.getInputStream()) {
			byte[] header = new byte[MAX_HEADER_SIZE];
			int bytesRead = is.read(header);
			if (bytesRead < 1)
				return false;

			byte[] actualHeader = Arrays.copyOf(header, bytesRead);

			for (byte[] signature : expectedSignatures) {
				if (actualHeader.length >= signature.length) {
					byte[] headerToCompare = Arrays.copyOf(actualHeader, signature.length);
					if (Arrays.equals(headerToCompare, signature)) {
						return true; // Match found
					}
				}
			}
		} catch (IOException e) {
			logger.error("Could not read file header for validation", e);
			return false;
		}

		return false;
	}
}