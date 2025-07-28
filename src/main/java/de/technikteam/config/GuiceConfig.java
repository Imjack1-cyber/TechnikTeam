package de.technikteam.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class GuiceConfig extends GuiceServletContextListener {

	private static Injector injector;

	@Override
	protected Injector getInjector() {
		if (injector == null) {
			injector = Guice.createInjector(new ServiceModule());
		}
		return injector;
	}

	/**
	 * Provides static access to the Guice injector. This is crucial for components
	 * that are not instantiated by Guice, such as standard servlets or listeners.
	 * 
	 * @return The application's Guice injector.
	 */
	public static Injector getInjectorInstance() {
		return injector;
	}
}