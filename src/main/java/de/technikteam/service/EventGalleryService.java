package de.technikteam.service;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventPhotoDAO;
import de.technikteam.model.Event;
import de.technikteam.model.EventPhoto;
import de.technikteam.model.File;
import de.technikteam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class EventGalleryService {

	private final EventPhotoDAO eventPhotoDAO;
	private final EventDAO eventDAO;
	private final FileService fileService;

	@Autowired
	public EventGalleryService(EventPhotoDAO eventPhotoDAO, EventDAO eventDAO, FileService fileService) {
		this.eventPhotoDAO = eventPhotoDAO;
		this.eventDAO = eventDAO;
		this.fileService = fileService;
	}

	public List<EventPhoto> findPhotosByEventId(int eventId) {
		return eventPhotoDAO.findByEventId(eventId);
	}

	@Transactional
	public EventPhoto addPhotoToGallery(int eventId, MultipartFile multipartFile, String caption, User uploader)
			throws IOException {
		Event event = eventDAO.getEventById(eventId);
		if (event == null) {
			throw new IllegalArgumentException("Event not found.");
		}
		if (!"ABGESCHLOSSEN".equals(event.getStatus())) {
			throw new SecurityException("Photos can only be added to completed events.");
		}
		if (!eventDAO.isUserAssociatedWithEvent(eventId, uploader.getId())) {
			throw new SecurityException("Only event participants can upload photos.");
		}

		// Store the file using FileService
		File savedFile = fileService.storeFile(multipartFile, null, "NUTZER", uploader, "event_galleries/" + eventId);

		// Create the gallery record
		EventPhoto photo = new EventPhoto();
		photo.setEventId(eventId);
		photo.setFileId(savedFile.getId());
		photo.setUploaderUserId(uploader.getId());
		photo.setCaption(caption);

		EventPhoto createdPhoto = eventPhotoDAO.create(photo);
		// Enrich with data for immediate frontend display
		createdPhoto.setFilepath(savedFile.getFilepath());
		createdPhoto.setUploaderUsername(uploader.getUsername());
		return createdPhoto;
	}

	@Transactional
	public void deletePhoto(int photoId, User currentUser) throws IOException {
		EventPhoto photo = eventPhotoDAO.findById(photoId)
				.orElseThrow(() -> new IllegalArgumentException("Photo not found."));

		Event event = eventDAO.getEventById(photo.getEventId());
		boolean isEventLeader = event != null && event.getLeaderUserId() == currentUser.getId();

		// Authorization check: User can delete their own photo, or an admin/event
		// leader can delete any.
		if (photo.getUploaderUserId() != currentUser.getId() && !currentUser.hasAdminAccess() && !isEventLeader) {
			throw new SecurityException("You do not have permission to delete this photo.");
		}

		// Delete the database record first
		eventPhotoDAO.delete(photoId);

		// Then delete the physical file
		fileService.deleteFile(photo.getFileId(), currentUser);
	}
}