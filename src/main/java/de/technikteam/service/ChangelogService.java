package de.technikteam.service;

import de.technikteam.dao.ChangelogDAO;
import de.technikteam.model.Changelog;
import de.technikteam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChangelogService {

	private final ChangelogDAO changelogDAO;
	private final AdminLogService adminLogService;

	@Autowired
	public ChangelogService(ChangelogDAO changelogDAO, AdminLogService adminLogService) {
		this.changelogDAO = changelogDAO;
		this.adminLogService = adminLogService;
	}

	public List<Changelog> findAll() {
		return changelogDAO.findAll();
	}

	@Transactional
	public boolean create(Changelog changelog, User adminUser) {
		boolean success = changelogDAO.create(changelog);
		if (success) {
			adminLogService.log(adminUser.getUsername(), "CHANGELOG_CREATE",
					"Created changelog for version: " + changelog.getVersion());
		}
		return success;
	}

	@Transactional
	public boolean update(Changelog changelog, User adminUser) {
		boolean success = changelogDAO.update(changelog);
		if (success) {
			adminLogService.log(adminUser.getUsername(), "CHANGELOG_UPDATE",
					"Updated changelog for version: " + changelog.getVersion());
		}
		return success;
	}

	@Transactional
	public boolean delete(int id, User adminUser) {
		Optional<Changelog> changelogOpt = changelogDAO.findById(id);
		if (changelogOpt.isPresent()) {
			boolean success = changelogDAO.delete(id);
			if (success) {
				adminLogService.log(adminUser.getUsername(), "CHANGELOG_DELETE",
						"Deleted changelog for version: " + changelogOpt.get().getVersion() + " (ID: " + id + ")");
			}
			return success;
		}
		return false;
	}
}