package de.technikteam.service;

import de.technikteam.dao.FileDAO;
import de.technikteam.model.User;
import de.technikteam.util.FileSignatureValidator;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

	private final FileDAO fileDAO;
	private final AdminLogService adminLogService;
	private final Path fileStorageLocation;
	private static final Logger logger = LogManager.getLogger(FileService.class);

	private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

	@Autowired
	public FileService(FileDAO fileDAO, ConfigurationService configService, AdminLogService adminLogService) {
		this.fileDAO = fileDAO;
		this.adminLogService = adminLogService;
		this.fileStorageLocation = Paths.get(configService.getProperty("upload.directory")).toAbsolutePath()
				.normalize();
	}

	public de.technikteam.model.File storeFile(MultipartFile multipartFile, Integer categoryId, String requiredRole,
			User adminUser) throws IOException {
		return storeFile(multipartFile, categoryId, requiredRole, adminUser, "docs");
	}

	public de.technikteam.model.File storeFile(MultipartFile multipartFile, Integer categoryId, String requiredRole,
			User adminUser, String subDirectory) throws IOException {
		logger.debug("Starting file storage process. User: {}, CategoryID: {}, Role: {}, SubDir: {}",
				adminUser.getUsername(), categoryId, requiredRole, subDirectory);

		// Security Validations
		if (multipartFile.getSize() > MAX_FILE_SIZE_BYTES) {
			logger.warn("File upload blocked for user {}: File size {} exceeds limit of {} bytes.",
					adminUser.getUsername(), multipartFile.getSize(), MAX_FILE_SIZE_BYTES);
			throw new IOException("Dateigröße überschreitet das Limit von 10MB.");
		}
		logger.trace("File size check passed: {} bytes.", multipartFile.getSize());

		FileSignatureValidator.FileTypeValidationResult validationResult = FileSignatureValidator
				.validateFileType(multipartFile);
		if (validationResult == FileSignatureValidator.FileTypeValidationResult.DISALLOWED) {
			logger.warn("File upload blocked for user {}: Invalid or disallowed file type detected. MIME: {}",
					adminUser.getUsername(), multipartFile.getContentType());
			throw new IOException("Ungültiger oder nicht erlaubter Dateityp erkannt.");
		}
		boolean needsWarning = validationResult == FileSignatureValidator.FileTypeValidationResult.ALLOWED_WITH_WARNING;
		logger.trace("File signature validation passed. Needs warning: {}", needsWarning);

		String originalFileName = FilenameUtils.getName(multipartFile.getOriginalFilename());
		String sanitizedFileName = originalFileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
		String uniqueFileName = UUID.randomUUID() + "-" + sanitizedFileName;
		logger.trace("Original filename: '{}', Sanitized and unique filename: '{}'", originalFileName, uniqueFileName);

		Path targetDirectory = this.fileStorageLocation.resolve(subDirectory);
		Path targetPath = targetDirectory.resolve(uniqueFileName);
		Files.createDirectories(targetDirectory);
		Files.copy(multipartFile.getInputStream(), targetPath);
		logger.debug("File successfully copied to physical path: {}", targetPath);

		de.technikteam.model.File file = new de.technikteam.model.File();
		file.setFilename(originalFileName);
		file.setFilepath(subDirectory + "/" + uniqueFileName);
		file.setCategoryId(categoryId);
		file.setRequiredRole(requiredRole);
		file.setNeedsWarning(needsWarning);

		logger.debug("Prepared file model for database insertion: {}", file);

		int newFileId = fileDAO.createFile(file);
		if (newFileId > 0) {
			adminLogService.log(adminUser.getUsername(), "UPLOAD_FILE",
					"Datei '" + originalFileName + "' hochgeladen.");
			de.technikteam.model.File savedFile = fileDAO.getFileById(newFileId);
			logger.info("File '{}' successfully stored and saved to database with ID {}.", originalFileName, newFileId);
			logger.debug("Final saved file data from DB: {}", savedFile);
			return savedFile;
		} else {
			Files.deleteIfExists(targetPath);
			logger.error("Failed to save file metadata to database for '{}'. Physical file has been rolled back.",
					originalFileName);
			throw new RuntimeException("Fehler beim Speichern der Datei-Metadaten in der Datenbank.");
		}
	}

	public boolean deleteFile(int fileId, User adminUser) throws IOException {
		de.technikteam.model.File file = fileDAO.getFileById(fileId);
		if (file == null) {
			return false;
		}

		boolean success = fileDAO.deleteFile(fileId);
		if (success) {
			Path filePath = this.fileStorageLocation.resolve(file.getFilepath()).normalize();
			Files.deleteIfExists(filePath);
			adminLogService.log(adminUser.getUsername(), "DELETE_FILE",
					"Datei '" + file.getFilename() + "' (ID: " + fileId + ") gelöscht.");
		}
		return success;
	}
}