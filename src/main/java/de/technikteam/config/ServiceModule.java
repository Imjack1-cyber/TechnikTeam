// src/main/java/de/technikteam/config/ServiceModule.java
package de.technikteam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import de.technikteam.api.v1.*;
import de.technikteam.api.v1.public_api.*;
import de.technikteam.dao.*;
import de.technikteam.filter.AdminFilter;
import de.technikteam.filter.AuthenticationFilter;
import de.technikteam.filter.CharacterEncodingFilter;
import de.technikteam.service.*;
import de.technikteam.servlet.*;
import de.technikteam.servlet.api.*;
import de.technikteam.servlet.api.passkey.*;
import java.time.LocalDateTime;

public class ServiceModule extends ServletModule {
	@Override
	protected void configureServlets() {
		// --- Filter Bindings ---
		bind(CharacterEncodingFilter.class).in(Scopes.SINGLETON);
		bind(AuthenticationFilter.class).in(Scopes.SINGLETON);
		bind(AdminFilter.class).in(Scopes.SINGLETON);
		filter("/*").through(CharacterEncodingFilter.class);
		filter("/*").through(AuthenticationFilter.class);
		filter("/api/v1/*").through(AdminFilter.class);

		// --- Shared Instances ---
		bind(Gson.class).toInstance(
				new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create());

		// --- Service and DAO Bindings ---
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

		// --- Servlet Bindings ---
		// Public Page Shell Servlets
		bind(RootServlet.class).in(Scopes.SINGLETON);
		bind(LoginServlet.class).in(Scopes.SINGLETON);
		bind(LogoutServlet.class).in(Scopes.SINGLETON);
		bind(HomeServlet.class).in(Scopes.SINGLETON);
		bind(ProfileServlet.class).in(Scopes.SINGLETON);
		bind(PasswordServlet.class).in(Scopes.SINGLETON);
		bind(CalendarServlet.class).in(Scopes.SINGLETON);
		bind(EventDetailsServlet.class).in(Scopes.SINGLETON);
		bind(MeetingDetailsServlet.class).in(Scopes.SINGLETON);
		bind(StorageItemDetailsServlet.class).in(Scopes.SINGLETON);
		bind(FileServlet.class).in(Scopes.SINGLETON);
		bind(FeedbackServlet.class).in(Scopes.SINGLETON);
		bind(MyFeedbackServlet.class).in(Scopes.SINGLETON);
		bind(PackKitServlet.class).in(Scopes.SINGLETON);
		bind(MarkdownEditorServlet.class).in(Scopes.SINGLETON);
		bind(StorageItemActionServlet.class).in(Scopes.SINGLETON);

		// Legacy Public APIs & Actions
		bind(IcalServlet.class).in(Scopes.SINGLETON);
		bind(DownloadServlet.class).in(Scopes.SINGLETON);
		bind(ImageServlet.class).in(Scopes.SINGLETON);
		bind(MarkdownApiServlet.class).in(Scopes.SINGLETON);
		bind(NotificationServlet.class).in(Scopes.SINGLETON);
		bind(AuthenticationStartServlet.class).in(Scopes.SINGLETON);
		bind(AuthenticationFinishServlet.class).in(Scopes.SINGLETON);
		bind(RegistrationStartServlet.class).in(Scopes.SINGLETON);
		bind(RegistrationFinishServlet.class).in(Scopes.SINGLETON);

		// New API v1 Admin Resources
		bind(UserResource.class).in(Scopes.SINGLETON);
		bind(WikiResource.class).in(Scopes.SINGLETON);
		bind(FeedbackResource.class).in(Scopes.SINGLETON);
		bind(ProfileRequestResource.class).in(Scopes.SINGLETON);
		bind(CourseResource.class).in(Scopes.SINGLETON);
		bind(MeetingResource.class).in(Scopes.SINGLETON);
		bind(StorageResource.class).in(Scopes.SINGLETON);
		bind(KitResource.class).in(Scopes.SINGLETON);
		bind(AchievementResource.class).in(Scopes.SINGLETON);
		bind(EventResource.class).in(Scopes.SINGLETON);
		bind(LogResource.class).in(Scopes.SINGLETON);
		bind(ReportResource.class).in(Scopes.SINGLETON);
		bind(SystemResource.class).in(Scopes.SINGLETON);
		bind(MatrixResource.class).in(Scopes.SINGLETON);

		// New API v1 Public Resources
		bind(PublicDashboardResource.class).in(Scopes.SINGLETON);
		bind(PublicEventResource.class).in(Scopes.SINGLETON);
		bind(PublicMeetingResource.class).in(Scopes.SINGLETON);
		bind(PublicStorageResource.class).in(Scopes.SINGLETON);
		bind(PublicProfileResource.class).in(Scopes.SINGLETON);

		// --- URL Mappings ---
		serve("").with(RootServlet.class);
		serve("/login").with(LoginServlet.class);
		serve("/logout").with(LogoutServlet.class);
		serve("/home").with(HomeServlet.class);
		serve("/profil").with(ProfileServlet.class);
		serve("/passwort").with(PasswordServlet.class);
		serve("/kalender").with(CalendarServlet.class);
		serve("/veranstaltungen/details").with(EventDetailsServlet.class);
		serve("/meetingDetails").with(MeetingDetailsServlet.class);
		serve("/lager/details").with(StorageItemDetailsServlet.class);
		serve("/dateien").with(FileServlet.class);
		serve("/feedback").with(FeedbackServlet.class);
		serve("/my-feedback").with(MyFeedbackServlet.class);
		serve("/pack-kit").with(PackKitServlet.class);
		serve("/editor").with(MarkdownEditorServlet.class);
		serve("/lager/aktionen").with(StorageItemActionServlet.class);
		serve("/calendar.ics").with(IcalServlet.class);
		serve("/download").with(DownloadServlet.class);
		serve("/image").with(ImageServlet.class);
		serve("/api/save-markdown").with(MarkdownApiServlet.class);
		serve("/notifications").with(NotificationServlet.class);
		serve("/api/auth/passkey/register/start").with(RegistrationStartServlet.class);
		serve("/api/auth/passkey/register/finish").with(RegistrationFinishServlet.class);
		serve("/api/auth/passkey/login/start").with(AuthenticationStartServlet.class);
		serve("/api/auth/passkey/login/finish").with(AuthenticationFinishServlet.class);

		// API v1 Mappings
		serve("/api/v1/public/dashboard").with(PublicDashboardResource.class);
		serve("/api/v1/public/events/*").with(PublicEventResource.class);
		serve("/api/v1/public/meetings/*").with(PublicMeetingResource.class);
		serve("/api/v1/public/storage/*").with(PublicStorageResource.class);
		serve("/api/v1/public/profile/*").with(PublicProfileResource.class);

		serve("/api/v1/users/*").with(UserResource.class);
		serve("/api/v1/wiki/*").with(WikiResource.class);
		serve("/api/v1/feedback/*").with(FeedbackResource.class);
		serve("/api/v1/profile-requests/*").with(ProfileRequestResource.class);
		serve("/api/v1/courses/*").with(CourseResource.class);
		serve("/api/v1/meetings/*").with(MeetingResource.class);
		serve("/api/v1/storage/*").with(StorageResource.class);
		serve("/api/v1/kits/*").with(KitResource.class);
		serve("/api/v1/achievements/*").with(AchievementResource.class);
		serve("/api/v1/events/*").with(EventResource.class);
		serve("/api/v1/logs").with(LogResource.class);
		serve("/api/v1/reports/*").with(ReportResource.class);
		serve("/api/v1/system/stats").with(SystemResource.class);
		serve("/api/v1/matrix/*").with(MatrixResource.class);
	}
}