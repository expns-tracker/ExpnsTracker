package org.expns_tracker.ExpnsTracker.config;

import com.google.cloud.firestore.Firestore;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.firestore.FirestoreLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class SchedulerConfig {

    @Bean
    public LockProvider lockProvider(Firestore firestore) {
        return new FirestoreLockProvider(firestore);
    }
}
