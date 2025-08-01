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

	private static final Map<String, List<byte[]>> MAGIC_NUMBERS = new HashMap<>();

	static {
		// JPEG
		MAGIC_NUMBERS.put("image/jpeg", List.of(new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }));
		// PNG
		MAGIC_NUMBERS.put("image/png", List.of(new byte[] { (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47 }));
		// GIF
		MAGIC_NUMBERS.put("image/gif", List.of(new byte[] { (byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38 }));
		// PDF
		MAGIC_NUMBERS.put("application/pdf",
				List.of(new byte[] { (byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46 }));
		// DOCX, XLSX, PPTX (PKZIP archive)
		List<byte[]> pkzip = List.of(new byte[] { (byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04 });
		MAGIC_NUMBERS.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", pkzip);
		MAGIC_NUMBERS.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", pkzip);
		MAGIC_NUMBERS.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", pkzip);
	}

	private static final int MAX_HEADER_SIZE = 8;

	public static boolean isFileTypeAllowed(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return false;
		}
		String declaredMimeType = file.getContentType();
		List<byte[]> expectedSignatures = MAGIC_NUMBERS.get(declaredMimeType);

		if (expectedSignatures == null) {
			logger.warn("File upload blocked for undeclared MIME type: {}", declaredMimeType);
			return false; // Type not in our allowed map
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

		logger.warn("File signature mismatch. Declared MIME type: {}, but magic bytes do not match. Upload blocked.",
				declaredMimeType);
		return false;
	}
}