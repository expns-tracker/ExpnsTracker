package org.expns_tracker.ExpnsTracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "firebase", ignoreUnknownFields = false)
@Data
public class FirebaseProperties {
    private String configPath;
}
