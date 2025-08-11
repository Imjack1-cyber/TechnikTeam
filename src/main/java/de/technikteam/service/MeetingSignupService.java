package de.technikteam.service;

import de.technikteam.dao.MeetingAttendanceDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.dao.MeetingWaitlistDAO;
import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MeetingSignupService contains business logic for signups, waitlisting, and
 * admin promotions.
 */
@Service
public class MeetingSignupService {

	private final MeetingDAO meetingDAO;
	private final MeetingAttendanceDAO attendanceDAO;
	private final MeetingWaitlistDAO waitlistDAO;

	@Autowired
	public MeetingSignupService(MeetingDAO meetingDAO, MeetingAttendanceDAO attendanceDAO,
			MeetingWaitlistDAO waitlistDAO) {
		this.meetingDAO = meetingDAO;
		this.attendanceDAO = attendanceDAO;
		this.waitlistDAO = waitlistDAO;
	}

	public enum SignupStatus {
		ENROLLED, WAITLISTED, ALREADY_ENROLLED, ERROR
	}

	public static class SignupResult {
		public SignupStatus status;
		public String message;

		public SignupResult(SignupStatus status, String message) {
			this.status = status;
			this.message = message;
		}
	}

	/**
	 * Sign up user for meeting: if meeting has a parent_meeting_id and the user has
	 * attended the original, then add to waitlist. Otherwise enroll directly.
	 *
	 * requestedBy can be null; usually the same as userId but allows admins to sign
	 * up on behalf of user.
	 */
	@Transactional
	public SignupResult signupOrWaitlist(int userId, int meetingId, Integer requestedBy) {
		try {
			Meeting meeting = meetingDAO.getMeetingById(meetingId);
			if (meeting == null) {
				return new SignupResult(SignupStatus.ERROR, "Meeting not found.");
			}

			// Check if signup deadline has passed
			LocalDateTime deadline = meeting.getSignupDeadline() != null ? meeting.getSignupDeadline()
					: meeting.getMeetingDateTime().minusHours(36);
			if (LocalDateTime.now().isAfter(deadline)) {
				return new SignupResult(SignupStatus.ERROR, "Die Anmeldefrist für diesen Termin ist abgelaufen.");
			}

			if (attendanceDAO.hasAttendedMeeting(userId, meetingId)) {
				return new SignupResult(SignupStatus.ALREADY_ENROLLED, "User already enrolled for this meeting.");
			}

			boolean isFull = meeting.getMaxParticipants() != null
					&& meetingDAO.getParticipantCount(meetingId) >= meeting.getMaxParticipants();

			boolean attendedOriginal = false;
			if (meeting.getParentMeetingId() > 0) {
				attendedOriginal = attendanceDAO.hasAttendedMeeting(userId, meeting.getParentMeetingId());
			}

			if (isFull || attendedOriginal) {
				boolean added = waitlistDAO.addToWaitlist(meetingId, userId, requestedBy);
				if (added) {
					String message = attendedOriginal
							? "Sie wurden auf die Warteliste gesetzt, da Sie den Lehrgang bereits absolviert haben."
							: "Sie wurden auf die Warteliste gesetzt, da der Termin voll ist.";
					return new SignupResult(SignupStatus.WAITLISTED, message);
				} else {
					return new SignupResult(SignupStatus.ERROR, "Failed to add user to waitlist.");
				}
			}

			boolean enrolled = attendanceDAO.enrollUser(userId, meetingId);
			if (enrolled) {
				waitlistDAO.removeFromWaitlist(meetingId, userId);
				return new SignupResult(SignupStatus.ENROLLED, "User successfully enrolled.");
			} else {
				return new SignupResult(SignupStatus.ERROR, "Failed to enroll user.");
			}
		} catch (Exception e) {
			return new SignupResult(SignupStatus.ERROR, "Unexpected error: " + e.getMessage());
		}
	}

	@Transactional
	public boolean signoffFromMeeting(int userId, int meetingId) {
		// Remove from both potential places
		boolean unenrolled = attendanceDAO.unenrollUser(userId, meetingId);
		boolean removedFromWaitlist = waitlistDAO.removeFromWaitlist(meetingId, userId);
		// Return true if either action had an effect
		return unenrolled || removedFromWaitlist;
	}

	/**
	 * Admin: get waitlist (users) for a meeting in FIFO order.
	 */
	public List<User> getWaitlist(int meetingId) {
		return waitlistDAO.getWaitlistForMeeting(meetingId);
	}

	/**
	 * Admin: promote a specific user from waitlist to enrollment.
	 */
	@Transactional
	public boolean promoteUserFromWaitlist(int meetingId, int userId, int adminUserId) {
		// double-check: user is on waitlist
		if (!waitlistDAO.isUserOnWaitlist(meetingId, userId)) {
			return false;
		}

		// enroll first
		boolean enrolled = attendanceDAO.enrollUser(userId, meetingId);
		if (!enrolled) {
			return false;
		}

		// mark promoted and remove from waitlist
		return waitlistDAO.markPromoted(meetingId, userId, adminUserId);
	}

	/**
	 * Admin: automatically promote the next user in queue (FIFO) if exists. Returns
	 * the userId that was promoted or -1 if none promoted.
	 */
	public int promoteNextInWaitlist(int meetingId, int adminUserId) {
		List<User> queue = waitlistDAO.getWaitlistForMeeting(meetingId);
		if (queue == null || queue.isEmpty())
			return -1;
		int userId = queue.get(0).getId();
		boolean ok = promoteUserFromWaitlist(meetingId, userId, adminUserId);
		return ok ? userId : -1;
	}
}