package de.technikteam.service;

import de.technikteam.dao.AnnouncementDAO;
import de.technikteam.model.Announcement;
import de.technikteam.model.User;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AnnouncementService {

	private final AnnouncementDAO announcementDAO;
	private final AdminLogService adminLogService;
	private final PolicyFactory richTextPolicy;
	private final NotificationService notificationService;

	@Autowired
	public AnnouncementService(AnnouncementDAO announcementDAO, AdminLogService adminLogService,
			@Qualifier("richTextPolicy") PolicyFactory richTextPolicy, NotificationService notificationService) {
		this.announcementDAO = announcementDAO;
		this.adminLogService = adminLogService;
		this.richTextPolicy = richTextPolicy;
		this.notificationService = notificationService;
	}

	public List<Announcement> findAll() {
		return announcementDAO.findAll();
	}

	@Transactional
	public Announcement create(Announcement announcement, User author) {
		announcement.setAuthorUserId(author.getId());
		announcement.setTitle(richTextPolicy.sanitize(announcement.getTitle()));
		announcement.setContent(richTextPolicy.sanitize(announcement.getContent()));

		Announcement created = announcementDAO.create(announcement);
		adminLogService.log(author.getUsername(), "ANNOUNCEMENT_CREATE", "Created announcement: " + created.getTitle());
		notificationService.broadcastUIUpdate("ANNOUNCEMENT", "CREATED", created);
		return created;
	}

	@Transactional
	public Announcement update(Announcement announcement, User editor) {
		Optional<Announcement> existingOpt = announcementDAO.findById(announcement.getId());
		if (existingOpt.isPresent()) {
			Announcement existing = existingOpt.get();
			existing.setTitle(richTextPolicy.sanitize(announcement.getTitle()));
			existing.setContent(richTextPolicy.sanitize(announcement.getContent()));
			Announcement updated = announcementDAO.update(existing);
			adminLogService.log(editor.getUsername(), "ANNOUNCEMENT_UPDATE",
					"Updated announcement: " + updated.getTitle());
			notificationService.broadcastUIUpdate("ANNOUNCEMENT", "UPDATED", updated);
			return updated;
		}
		return null;
	}

	@Transactional
	public boolean delete(int id, User adminUser) {
		Optional<Announcement> announcementOpt = announcementDAO.findById(id);
		if (announcementOpt.isPresent()) {
			boolean success = announcementDAO.delete(id);
			if (success) {
				adminLogService.log(adminUser.getUsername(), "ANNOUNCEMENT_DELETE",
						"Deleted announcement: " + announcementOpt.get().getTitle() + " (ID: " + id + ")");
				notificationService.broadcastUIUpdate("ANNOUNCEMENT", "DELETED", Map.of("id", id));
			}
			return success;
		}
		return false;
	}
}