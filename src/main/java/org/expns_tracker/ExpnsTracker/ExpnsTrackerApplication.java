package org.expns_tracker.ExpnsTracker;

import org.expns_tracker.ExpnsTracker.config.FirebaseProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({FirebaseProperties.class})
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class ExpnsTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpnsTrackerApplication.class, args);
	}

}
