package de.technikteam.service;

import de.technikteam.dao.TrainingRequestDAO;
import de.technikteam.model.TrainingRequest;
import de.technikteam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingRequestService {

	private final TrainingRequestDAO trainingRequestDAO;
	private final AdminLogService adminLogService;

	@Autowired
	public TrainingRequestService(TrainingRequestDAO trainingRequestDAO, AdminLogService adminLogService) {
		this.trainingRequestDAO = trainingRequestDAO;
		this.adminLogService = adminLogService;
	}

	public List<TrainingRequest> findAllWithInterestCount() {
		return trainingRequestDAO.findAllWithInterestCount();
	}

	@Transactional
	public TrainingRequest create(String topic, User requester) {
		TrainingRequest newRequest = trainingRequestDAO.create(topic, requester.getId());
		// Automatically register interest for the user who created the request
		trainingRequestDAO.addInterest(newRequest.getId(), requester.getId());
		return newRequest;
	}

	public boolean registerInterest(int requestId, int userId) {
		return trainingRequestDAO.addInterest(requestId, userId);
	}

	@Transactional
	public boolean delete(int id, User adminUser) {
		Optional<TrainingRequest> requestOpt = trainingRequestDAO.findById(id);
		if (requestOpt.isPresent()) {
			boolean success = trainingRequestDAO.delete(id);
			if (success) {
				adminLogService.log(adminUser.getUsername(), "TRAINING_REQUEST_DELETE",
						"Deleted training request: " + requestOpt.get().getTopic() + " (ID: " + id + ")");
			}
			return success;
		}
		return false;
	}
}