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
import java.util.Set;

public class FileSignatureValidator {
	private static final Logger logger = LogManager.getLogger(FileSignatureValidator.class);

	public enum FileTypeValidationResult {
		ALLOWED, ALLOWED_WITH_WARNING, DISALLOWED
	}

	// A set of MIME types considered generally safe and do not require a warning.
	private static final Set<String> SAFE_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "application/pdf",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
			"application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
			"application/msword", // .doc
			"application/vnd.ms-excel", // .xls
			"application/vnd.ms-powerpoint" // .ppt
	);

	public static FileTypeValidationResult validateFileType(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return FileTypeValidationResult.DISALLOWED;
		}
		String declaredMimeType = file.getContentType();

		if (declaredMimeType == null || declaredMimeType.isBlank()) {
			logger.warn("File uploaded with no MIME type, flagging for warning.");
			return FileTypeValidationResult.ALLOWED_WITH_WARNING;
		}

		if (SAFE_MIME_TYPES.contains(declaredMimeType)) {
			return FileTypeValidationResult.ALLOWED;
		}

		// Any other file type is allowed but will be flagged for a warning on download.
		logger.debug("File with MIME type '{}' is not in the safe list. Flagging for warning.", declaredMimeType);
		return FileTypeValidationResult.ALLOWED_WITH_WARNING;
	}
}