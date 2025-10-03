package de.technikteam.service;

import de.technikteam.dao.TrainingRequestDAO;
import de.technikteam.model.TrainingRequest;
import de.technikteam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TrainingRequestService {

	private final TrainingRequestDAO trainingRequestDAO;
	private final AdminLogService adminLogService;
	private final NotificationService notificationService;

	@Autowired
	public TrainingRequestService(TrainingRequestDAO trainingRequestDAO, AdminLogService adminLogService, NotificationService notificationService) {
		this.trainingRequestDAO = trainingRequestDAO;
		this.adminLogService = adminLogService;
		this.notificationService = notificationService;
	}

	public List<TrainingRequest> findAllWithInterestCount() {
		return trainingRequestDAO.findAllWithInterestCount();
	}

	@Transactional
	public TrainingRequest create(String topic, User requester) {
		TrainingRequest newRequest = trainingRequestDAO.create(topic, requester.getId());
		// Automatically register interest for the user who created the request
		trainingRequestDAO.addInterest(newRequest.getId(), requester.getId());
		notificationService.broadcastUIUpdate("TRAINING_REQUEST", "CREATED", newRequest);
		return newRequest;
	}

	public boolean registerInterest(int requestId, int userId) {
		boolean success = trainingRequestDAO.addInterest(requestId, userId);
		if (success) {
			notificationService.broadcastUIUpdate("TRAINING_REQUEST", "UPDATED", Map.of("id", requestId));
		}
		return success;
	}

	@Transactional
	public boolean delete(int id, User adminUser) {
		Optional<TrainingRequest> requestOpt = trainingRequestDAO.findById(id);
		if (requestOpt.isPresent()) {
			boolean success = trainingRequestDAO.delete(id);
			if (success) {
				adminLogService.log(adminUser.getUsername(), "TRAINING_REQUEST_DELETE",
						"Deleted training request: " + requestOpt.get().getTopic() + " (ID: " + id + ")");
				notificationService.broadcastUIUpdate("TRAINING_REQUEST", "DELETED", Map.of("id", id));
			}
			return success;
		}
		return false;
	}
}