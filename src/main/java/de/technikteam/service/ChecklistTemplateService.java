package de.technikteam.service;

import de.technikteam.api.v1.dto.ChecklistTemplateItemValidationDTO;
import de.technikteam.dao.ChecklistTemplateDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.model.ChecklistTemplate;
import de.technikteam.model.ChecklistTemplateItem;
import de.technikteam.model.StorageItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChecklistTemplateService {

	private final ChecklistTemplateDAO templateDAO;
	private final StorageDAO storageDAO;

	@Autowired
	public ChecklistTemplateService(ChecklistTemplateDAO templateDAO, StorageDAO storageDAO) {
		this.templateDAO = templateDAO;
		this.storageDAO = storageDAO;
	}

	public List<ChecklistTemplateItemValidationDTO> getTemplateForEventApplication(int templateId) {
		// Since findAll is efficient and likely cached in a real app, we use it.
		// A findById would be slightly more direct if available.
		ChecklistTemplate template = templateDAO.findAll().stream().filter(t -> t.getId() == templateId).findFirst()
				.orElse(null);

		if (template == null) {
			return List.of();
		}

		List<ChecklistTemplateItemValidationDTO> validationList = new ArrayList<>();
		for (ChecklistTemplateItem item : template.getItems()) {
			if (item.getStorageItemId() != null && item.getStorageItemId() > 0) {
				StorageItem storageItem = storageDAO.getItemById(item.getStorageItemId());
				if (storageItem != null) {
					validationList.add(new ChecklistTemplateItemValidationDTO(storageItem.getId(),
							storageItem.getName(), item.getQuantity(), storageItem.getAvailableQuantity()));
				}
			}
		}
		return validationList;
	}
}