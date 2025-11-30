package org.expns_tracker.ExpnsTracker;

import org.expns_tracker.ExpnsTracker.config.FirebaseProperties;
import org.expns_tracker.ExpnsTracker.config.TinkProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({FirebaseProperties.class, TinkProperties.class})
@SpringBootApplication
public class ExpnsTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpnsTrackerApplication.class, args);
	}

}
