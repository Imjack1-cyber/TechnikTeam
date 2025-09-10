package de.technikteam.service;

import de.technikteam.api.v1.dto.NotificationPayload;
import de.technikteam.dao.ScheduledNotificationDAO;
import de.technikteam.model.ScheduledNotification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@EnableScheduling
public class NotificationScheduler {

	private static final Logger logger = LogManager.getLogger(NotificationScheduler.class);

	private final ScheduledNotificationDAO notificationDAO;
	private final NotificationService notificationService;

	@Autowired
	public NotificationScheduler(ScheduledNotificationDAO notificationDAO, NotificationService notificationService) {
		this.notificationDAO = notificationDAO;
		this.notificationService = notificationService;
	}

	@Scheduled(fixedRate = 60000) // Run every 60 seconds
	public void sendPendingNotifications() {
		logger.debug("Running scheduled notification task...");
		List<ScheduledNotification> pending = notificationDAO.findPendingNotifications();

		if (pending.isEmpty()) {
			logger.debug("No pending notifications to send.");
			return;
		}

		logger.info("Found {} pending notifications to send.", pending.size());
		for (ScheduledNotification notification : pending) {
            NotificationPayload payload = new NotificationPayload();
            payload.setTitle(notification.getTitle());
            payload.setDescription(notification.getDescription());
            payload.setLevel("Important"); // Reminders are always important
            payload.setUrl(notification.getUrl());
			notificationService.sendNotificationToUser(notification.getTargetUserId(), payload);
		}

		List<Integer> sentIds = pending.stream().map(ScheduledNotification::getId).collect(Collectors.toList());
		notificationDAO.markAsSent(sentIds);
		logger.info("Successfully sent and marked {} notifications.", sentIds.size());
	}
}