package org.expns_tracker.ExpnsTracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "expns-tracker", ignoreUnknownFields = false)
@Data
public class ApplicationProperties {
    private String exportDir;
}
