package de.technikteam.listener;

import de.technikteam.servlet.http.SessionManager;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * Listens for HttpSession creation and destruction to keep the SessionManager
 * up to date.
 */
@WebListener
public class SessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		SessionManager.addSession(se.getSession());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		SessionManager.removeSession(se.getSession());
	}
}