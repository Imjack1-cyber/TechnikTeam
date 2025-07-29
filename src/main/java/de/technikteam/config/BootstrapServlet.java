package de.technikteam.config;

import com.google.inject.Singleton;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;

import java.util.Set;

/**
 * A startup servlet responsible for initializing the OpenAPI/Swagger
 * configuration. It runs once when the application starts, defines the API's
 * basic information and security schemes, and tells Swagger which packages to
 * scan for API annotations.
 */
@Singleton
public class BootstrapServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		// Define the basic API information.
		OpenAPI oas = new OpenAPI().info(new Info().title("TechnikTeam API")
				.description("This is the complete REST API for the TechnikTeam Event & Crew Management System.")
				.version("v1.0.0"));

		// Define the JWT Bearer security scheme.
		final String securitySchemeName = "bearerAuth";
		oas.components(new Components().addSecuritySchemes(securitySchemeName,
				new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList(securitySchemeName));

		// Configure Swagger to scan our API packages.
		SwaggerConfiguration oac = new SwaggerConfiguration().openAPI(oas)
				.resourcePackages(Set.of("de.technikteam.api.v1")).scan(true);

		// Store the configuration in the servlet context so the OpenApiServlet can find
		// it.
		config.getServletContext().setAttribute("openApiConfiguration", oac);
	}
}