package de.technikteam.service;

import de.technikteam.dao.StorageItemRelationDAO;
import de.technikteam.model.StorageItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StorageItemRelationService {

	private final StorageItemRelationDAO relationDAO;

	@Autowired
	public StorageItemRelationService(StorageItemRelationDAO relationDAO) {
		this.relationDAO = relationDAO;
	}

	public List<StorageItem> findRelatedItems(int itemId) {
		return relationDAO.findRelatedItems(itemId);
	}

	@Transactional
	public void updateRelations(int itemId, List<Integer> relatedItemIds) {
		relationDAO.updateRelations(itemId, relatedItemIds);
	}
}