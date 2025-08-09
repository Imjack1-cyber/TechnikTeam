package de.technikteam.service;

import de.technikteam.dao.PageDocumentationDAO;
import de.technikteam.model.PageDocumentation;
import de.technikteam.model.User;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PageDocumentationService {

	private final PageDocumentationDAO documentationDAO;
	private final AdminLogService adminLogService;
	private final PolicyFactory richTextPolicy;

	@Autowired
	public PageDocumentationService(PageDocumentationDAO documentationDAO, AdminLogService adminLogService,
			@Qualifier("richTextPolicy") PolicyFactory richTextPolicy) {
		this.documentationDAO = documentationDAO;
		this.adminLogService = adminLogService;
		this.richTextPolicy = richTextPolicy;
	}

	public List<PageDocumentation> findAll(boolean isAdmin) {
		return documentationDAO.findAll(isAdmin).stream().peek(this::enrichWithWikiLink).collect(Collectors.toList());
	}

	public Optional<PageDocumentation> findByKey(String pageKey) {
		Optional<PageDocumentation> docOpt = documentationDAO.findByKey(pageKey);
		docOpt.ifPresent(this::enrichWithWikiLink);
		return docOpt;
	}

	@Transactional
	public PageDocumentation create(PageDocumentation doc, User adminUser) {
		doc.setFeatures(richTextPolicy.sanitize(doc.getFeatures()));
		PageDocumentation createdDoc = documentationDAO.create(doc);
		adminLogService.log(adminUser.getUsername(), "DOCS_CREATE", "Created documentation page: " + doc.getTitle());
		return createdDoc;
	}

	@Transactional
	public PageDocumentation update(PageDocumentation doc, User adminUser) {
		doc.setFeatures(richTextPolicy.sanitize(doc.getFeatures()));
		PageDocumentation updatedDoc = documentationDAO.update(doc);
		adminLogService.log(adminUser.getUsername(), "DOCS_UPDATE", "Updated documentation page: " + doc.getTitle());
		return updatedDoc;
	}

	@Transactional
	public boolean delete(int id, User adminUser) {
		Optional<PageDocumentation> docOpt = documentationDAO.findById(id); // Assuming findById exists
		if (docOpt.isPresent()) {
			boolean success = documentationDAO.delete(id);
			if (success) {
				adminLogService.log(adminUser.getUsername(), "DOCS_DELETE",
						"Deleted documentation page: " + docOpt.get().getTitle());
			}
			return success;
		}
		return false;
	}

	private void enrichWithWikiLink(PageDocumentation doc) {
		if (doc.getWikiEntryId() != null && doc.getWikiEntryId() > 0) {
			// The frontend knows how to handle this link.
			doc.setWikiLink("/admin/wiki");
		}
	}
}