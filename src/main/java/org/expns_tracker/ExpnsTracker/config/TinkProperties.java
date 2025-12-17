package org.expns_tracker.ExpnsTracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tink", ignoreUnknownFields = false)
@Data
public class TinkProperties {
    String clientId;
    String clientSecret;
    String redirectUri;
    String apiUrl;

    public static final String TINK_LINK_ACTOR_ID = "df05e4b379934cd09963197cc855bfe9";
}
