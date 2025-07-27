# Stage 1: Build the project with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# Run Maven build to produce the WAR file
RUN mvn clean package -DskipTests

# Stage 2: Create the final image with Tomcat
# CORRECTED: Using the official '10.1-jdk21' tag which is compatible and exists on Docker Hub.
FROM tomcat:10.1-jdk21
# Remove the default ROOT webapp
RUN rm -rf /usr/local/tomcat/webapps/ROOT
# Copy the built WAR file from the 'build' stage to Tomcat's webapps directory
COPY --from=build /app/target/TechnikTeam.war /usr/local/tomcat/webapps/ROOT.war

# Create the upload directory and set permissions for the Tomcat user
RUN mkdir -p /usr/local/tomcat/uploads/temp && \
    chown -R www-data:www-data /usr/local/tomcat/uploads

EXPOSE 8080
CMD ["catalina.sh", "run"]