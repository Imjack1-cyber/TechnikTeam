package de.technikteam.service;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.dao.WikiDAO;
import de.technikteam.model.Event;
import de.technikteam.model.Meeting;
import de.technikteam.model.SearchResultDTO;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import de.technikteam.model.WikiEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchService {

	private final EventDAO eventDAO;
	private final StorageDAO storageDAO;
	private final MeetingDAO meetingDAO;
	private final WikiDAO wikiDAO;

	@Autowired
	public SearchService(EventDAO eventDAO, StorageDAO storageDAO, MeetingDAO meetingDAO, WikiDAO wikiDAO) {
		this.eventDAO = eventDAO;
		this.storageDAO = storageDAO;
		this.meetingDAO = meetingDAO;
		this.wikiDAO = wikiDAO;
	}

	public List<SearchResultDTO> performSearch(String query, User user) {
		// Since security is frontend-only, we don't need to filter results based on the
		// user object here.
		// In a secure app, we would pass the user to each DAO method.

		List<SearchResultDTO> eventResults = eventDAO.search(query).stream().map(this::mapEventToSearchResult)
				.collect(Collectors.toList());

		List<SearchResultDTO> itemResults = storageDAO.search(query).stream().map(this::mapStorageItemToSearchResult)
				.collect(Collectors.toList());

		List<SearchResultDTO> meetingResults = meetingDAO.search(query).stream().map(this::mapMeetingToSearchResult)
				.collect(Collectors.toList());

		List<SearchResultDTO> wikiResults = new ArrayList<>();
		// Only search wiki if user is an admin
		if (user != null && user.hasAdminAccess()) {
			wikiResults = wikiDAO.search(query).stream().map(this::mapWikiEntryToSearchResult)
					.collect(Collectors.toList());
		}

		return Stream.of(eventResults, itemResults, meetingResults, wikiResults).flatMap(List::stream)
				.collect(Collectors.toList());
	}

	private SearchResultDTO mapEventToSearchResult(Event event) {
		String url = "/veranstaltungen/details/" + event.getId();
		return new SearchResultDTO("Veranstaltung", event.getName(), url, event.getLocation());
	}

	private SearchResultDTO mapStorageItemToSearchResult(StorageItem item) {
		String url = "/lager/details/" + item.getId();
		return new SearchResultDTO("Lagerartikel", item.getName(), url, item.getLocation());
	}

	private SearchResultDTO mapMeetingToSearchResult(Meeting meeting) {
		String url = "/lehrgaenge/details/" + meeting.getId();
		String title = meeting.getParentCourseName() + ": " + meeting.getName();
		return new SearchResultDTO("Lehrgang", title, url, meeting.getLocation());
	}

	private SearchResultDTO mapWikiEntryToSearchResult(WikiEntry entry) {
		// Note: The Wiki is admin-only, so this URL will only work for admins.
		String url = "/admin/wiki"; // The wiki is a single page app, we can't link to a sub-page directly
		return new SearchResultDTO("Dokumentation", entry.getFilePath(), url, "Wiki-Eintrag");
	}
}