// src/main/java/de/technikteam/config/ServiceModule.java
package de.technikteam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import de.technikteam.api.v1.*;
import de.technikteam.api.v1.auth.*;
import de.technikteam.api.v1.public_api.*;
import de.technikteam.dao.*;
import de.technikteam.filter.AdminFilter;
import de.technikteam.filter.ApiAuthFilter;
import de.technikteam.filter.CharacterEncodingFilter;
import de.technikteam.filter.CorsFilter;
import de.technikteam.service.*;
import de.technikteam.servlet.NotificationServlet;

import java.time.LocalDateTime;
import java.util.Arrays;

public class ServiceModule extends ServletModule {
	@Override
	protected void configureServlets() {
		// --- Filter Bindings ---
		bind(CharacterEncodingFilter.class).in(Scopes.SINGLETON);
		bind(CorsFilter.class).in(Scopes.SINGLETON);
		bind(ApiAuthFilter.class).in(Scopes.SINGLETON);
		bind(AdminFilter.class).in(Scopes.SINGLETON);

		// --- Filter Chain Configuration ---
		filter("/*").through(CharacterEncodingFilter.class);
		filter("/api/*").through(CorsFilter.class);

		String[] securedApiPaths = { "/api/v1/public/*", "/api/v1/users/*", "/api/v1/wiki/*", "/api/v1/feedback/*",
				"/api/v1/profile-requests/*", "/api/v1/courses/*", "/api/v1/meetings/*", "/api/v1/storage/*",
				"/api/v1/kits/*", "/api/v1/achievements/*", "/api/v1/events/*", "/api/v1/logs", "/api/v1/reports/*",
				"/api/v1/system/stats", "/api/v1/matrix/*", "/api/v1/files/*" };
		filter(Arrays.asList(securedApiPaths)).through(ApiAuthFilter.class);

		String[] adminApiPaths = { "/api/v1/users/*", "/api/v1/wiki/*", "/api/v1/feedback/*",
				"/api/v1/profile-requests/*", "/api/v1/courses/*", "/api/v1/meetings/*", "/api/v1/storage/*",
				"/api/v1/kits/*", "/api/v1/achievements/*", "/api/v1/events/*", "/api/v1/logs", "/api/v1/reports/*",
				"/api/v1/system/stats", "/api/v1/matrix/*", "/api/v1/files/*" };
		filter(Arrays.asList(adminApiPaths)).through(AdminFilter.class);

		bind(Gson.class).toInstance(
				new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create());

		// --- Service and DAO Bindings (ALL SINGLETONS) ---
		bind(LoginAttemptService.class).in(Scopes.SINGLETON);
		bind(AuthService.class).in(Scopes.SINGLETON);
		bind(ConfigurationService.class).in(Scopes.SINGLETON);
		bind(DatabaseManager.class).in(Scopes.SINGLETON);
		bind(AuthorizationService.class).in(Scopes.SINGLETON);
		bind(AdminLogService.class).in(Scopes.SINGLETON);
		bind(UserService.class).in(Scopes.SINGLETON);
		bind(StorageService.class).in(Scopes.SINGLETON);
		bind(EventService.class).in(Scopes.SINGLETON);
		bind(PasskeyService.class).in(Scopes.SINGLETON);
		bind(SystemInfoService.class).in(Scopes.SINGLETON);
		bind(AchievementService.class).in(Scopes.SINGLETON);
		bind(AdminDashboardService.class).in(Scopes.SINGLETON);
		bind(WikiService.class).in(Scopes.SINGLETON);
		bind(TodoService.class).in(Scopes.SINGLETON);
		bind(NotificationService.class).in(Scopes.SINGLETON);
		bind(UserDAO.class).in(Scopes.SINGLETON);
		bind(RoleDAO.class).in(Scopes.SINGLETON);
		bind(PermissionDAO.class).in(Scopes.SINGLETON);
		bind(StorageDAO.class).in(Scopes.SINGLETON);
		bind(StorageLogDAO.class).in(Scopes.SINGLETON);
		bind(EventDAO.class).in(Scopes.SINGLETON);
		bind(EventTaskDAO.class).in(Scopes.SINGLETON);
		bind(EventChatDAO.class).in(Scopes.SINGLETON);
		bind(CourseDAO.class).in(Scopes.SINGLETON);
		bind(MeetingDAO.class).in(Scopes.SINGLETON);
		bind(MeetingAttendanceDAO.class).in(Scopes.SINGLETON);
		bind(UserQualificationsDAO.class).in(Scopes.SINGLETON);
		bind(AchievementDAO.class).in(Scopes.SINGLETON);
		bind(AdminLogDAO.class).in(Scopes.SINGLETON);
		bind(AttachmentDAO.class).in(Scopes.SINGLETON);
		bind(InventoryKitDAO.class).in(Scopes.SINGLETON);
		bind(MaintenanceLogDAO.class).in(Scopes.SINGLETON);
		bind(PasskeyDAO.class).in(Scopes.SINGLETON);
		bind(ProfileChangeRequestDAO.class).in(Scopes.SINGLETON);
		bind(EventFeedbackDAO.class).in(Scopes.SINGLETON);
		bind(EventCustomFieldDAO.class).in(Scopes.SINGLETON);
		bind(FeedbackSubmissionDAO.class).in(Scopes.SINGLETON);
		bind(FileDAO.class).in(Scopes.SINGLETON);
		bind(ReportDAO.class).in(Scopes.SINGLETON);
		bind(StatisticsDAO.class).in(Scopes.SINGLETON);
		bind(TodoDAO.class).in(Scopes.SINGLETON);
		bind(WikiDAO.class).in(Scopes.SINGLETON);

		// --- SERVLET BINDINGS ---
		serve("/notifications").with(NotificationServlet.class);

		// --- API v1 SERVLET BINDINGS ---
		serve("/api/v1/public/dashboard").with(PublicDashboardResource.class);
		serve("/api/v1/public/events", "/api/v1/public/events/*").with(PublicEventResource.class);
		serve("/api/v1/public/meetings", "/api/v1/public/meetings/*").with(PublicMeetingResource.class);
		serve("/api/v1/public/storage", "/api/v1/public/storage/*").with(PublicStorageResource.class);
		serve("/api/v1/public/profile", "/api/v1/public/profile/*").with(PublicProfileResource.class);
		serve("/api/v1/public/calendar.ics").with(PublicCalendarResource.class);
		serve("/api/v1/public/files", "/api/v1/public/files/*").with(PublicFilesResource.class);
		serve("/api/v1/public/calendar/entries").with(PublicCalendarEntriesResource.class);
		serve("/api/v1/public/feedback", "/api/v1/public/feedback/*").with(PublicFeedbackResource.class);
		serve("/api/v1/public/files/*").with(PublicFileStreamResource.class);

		serve("/api/v1/auth/login").with(AuthResource.class);
		serve("/api/v1/auth/passkey/register/start").with(RegistrationStartServlet.class);
		serve("/api/v1/auth/passkey/register/finish").with(RegistrationFinishServlet.class);
		serve("/api/v1/auth/passkey/login/start").with(AuthenticationStartServlet.class);
		serve("/api/v1/auth/passkey/login/finish").with(AuthenticationFinishServlet.class);

		serve("/api/v1/users", "/api/v1/users/*").with(UserResource.class);
		serve("/api/v1/wiki", "/api/v1/wiki/*").with(WikiResource.class);
		serve("/api/v1/feedback", "/api/v1/feedback/*").with(FeedbackResource.class);
		serve("/api/v1/profile-requests", "/api/v1/profile-requests/*").with(ProfileRequestResource.class);
		serve("/api/v1/courses", "/api/v1/courses/*").with(CourseResource.class);
		serve("/api/v1/meetings", "/api/v1/meetings/*").with(MeetingResource.class);
		serve("/api/v1/storage", "/api/v1/storage/*").with(StorageResource.class);
		serve("/api/v1/kits", "/api/v1/kits/*").with(KitResource.class);
		serve("/api/v1/achievements", "/api/v1/achievements/*").with(AchievementResource.class);
		serve("/api/v1/events", "/api/v1/events/*").with(EventResource.class);
		serve("/api/v1/logs").with(LogResource.class);
		serve("/api/v1/reports", "/api/v1/reports/*").with(ReportResource.class);
		serve("/api/v1/system/stats").with(SystemResource.class);
		serve("/api/v1/matrix", "/api/v1/matrix/*").with(MatrixResource.class);
		serve("/api/v1/files", "/api/v1/files/*").with(FileResource.class);
	}
}