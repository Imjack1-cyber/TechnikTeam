package de.technikteam.service;

import de.technikteam.dao.FileDAO;
import de.technikteam.model.User;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

	private final FileDAO fileDAO;
	private final ConfigurationService configService;
	private final AdminLogService adminLogService;
	private final Path fileStorageLocation;

	private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
	private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList("application/pdf", "image/jpeg", "image/png",
			"image/gif", "application/msword", // .doc
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
			"application/vnd.ms-excel", // .xls
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // .xlsx
	);

	@Autowired
	public FileService(FileDAO fileDAO, ConfigurationService configService, AdminLogService adminLogService) {
		this.fileDAO = fileDAO;
		this.configService = configService;
		this.adminLogService = adminLogService;
		this.fileStorageLocation = Paths.get(configService.getProperty("upload.directory")).toAbsolutePath()
				.normalize();
	}

	public de.technikteam.model.File storeFile(MultipartFile multipartFile, Integer categoryId, String requiredRole,
			User adminUser) throws IOException {

		// Security Validations
		if (multipartFile.getSize() > MAX_FILE_SIZE_BYTES) {
			throw new IOException("File size exceeds the limit of 10MB.");
		}
		if (!ALLOWED_MIME_TYPES.contains(multipartFile.getContentType())) {
			throw new IOException("Invalid file type. Allowed types are PDF, images, and Office documents.");
		}

		String originalFileName = FilenameUtils.getName(multipartFile.getOriginalFilename());
		String sanitizedFileName = originalFileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
		String uniqueFileName = UUID.randomUUID() + "-" + sanitizedFileName;

		Path targetPath = this.fileStorageLocation.resolve(Paths.get("docs", uniqueFileName));
		Files.createDirectories(targetPath.getParent());
		Files.copy(multipartFile.getInputStream(), targetPath);

		de.technikteam.model.File file = new de.technikteam.model.File();
		file.setFilename(originalFileName);
		file.setFilepath("docs/" + uniqueFileName);
		file.setCategoryId(categoryId != null ? categoryId : 0);
		file.setRequiredRole(requiredRole);

		int newFileId = fileDAO.createFile(file);
		if (newFileId > 0) {
			adminLogService.log(adminUser.getUsername(), "UPLOAD_FILE",
					"Datei '" + originalFileName + "' hochgeladen.");
			return fileDAO.getFileById(newFileId);
		} else {
			Files.deleteIfExists(targetPath);
			throw new RuntimeException("Failed to save file metadata to database.");
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