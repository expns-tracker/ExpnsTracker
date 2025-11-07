package org.expns_tracker.ExpnsTracker.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@AllArgsConstructor
@Log4j2
public class FirebaseConfig {
    private final FirebaseProperties firebaseProperties;

    @PostConstruct
    public void initialize() throws IOException {
        FileInputStream serviceAccount = new FileInputStream(firebaseProperties.getConfigPath());

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully");
        }
    }
}
