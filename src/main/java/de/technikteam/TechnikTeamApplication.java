package de.technikteam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "de.technikteam")
@EnableScheduling
public class TechnikTeamApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechnikTeamApplication.class, args);
	}

}