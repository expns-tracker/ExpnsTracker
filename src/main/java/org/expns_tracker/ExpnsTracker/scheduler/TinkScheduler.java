package org.expns_tracker.ExpnsTracker.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.repository.UserRepository;
import org.expns_tracker.ExpnsTracker.service.TinkService;
import org.expns_tracker.ExpnsTracker.service.TransactionService;
import org.expns_tracker.ExpnsTracker.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@Component
@Log4j2
@RequiredArgsConstructor
public class TinkScheduler {

    private final UserRepository userRepository;
    private final TinkService tinkService;
    private final LockProvider lockProvider;
    private final UserService userService;
    private final TransactionService transactionService;

    @Scheduled(cron = "0 0 4 * * *")
    @SchedulerLock(name = "TinkSyncTask", lockAtLeastFor = "5m", lockAtMostFor = "1h")
    public void syncAllUsers() {
        log.info("Starting scheduled transaction sync...");
        List<User> users;
        try {
            users = userRepository.findAllByTinkUserIdNotNull();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (User user : users) {
                executor.submit(() -> {
                    try {
                        syncUserSafely(user);
                    } catch (Exception e) {
                        log.error(
                                "Failed while syncing transactions for user {}:{}.",
                                user.getEmail(),
                                user.getName(),
                                e
                        );
                    }
                });
            }
        }

        log.info("Finished scheduled transaction sync...");
    }

    public void syncSingleUser(String userId) {
        User user = userService.getUser(userId);


        LockConfiguration lockConfig = getUserLockConfig(user);
        Optional<SimpleLock> lock = lockProvider.lock(lockConfig);

        if (lock.isEmpty()) {
            throw new IllegalStateException("Sync already in progress for this user.");
        }

        Thread.ofVirtual().start(() -> {
            try {
                syncUserTransactions(user);
            } catch (Exception e) {
                log.error("Manual sync failed for user {}: {}", user.getEmail(), e.getMessage());
            } finally {
                lock.get().unlock();
            }
        });
    }

    private void syncUserSafely(User user) {
        LockConfiguration lockConfig = getUserLockConfig(user);
        Optional<SimpleLock> lock = lockProvider.lock(lockConfig);

        if (lock.isPresent()) {
            try {
                syncUserTransactions(user);
            } finally {
                lock.get().unlock();
            }
        } else {
            log.info("Skipping user {} - already being synced.", user.getEmail());
        }
    }

    private void syncUserTransactions(@NotNull User user) {
        log.info("Syncing transactions for user {}:{}", user.getEmail(), user.getName());

        String code = tinkService.getUserAccessCode(user.getTinkUserId());

        String accessToken = tinkService.getAccessToken(code);
        String pageToken = null;
        do{
            JsonNode transactions = tinkService.fetchTransactions(accessToken, pageToken);
            log.info("Transactions for user {}:{}", user.getEmail(), transactions);
            transactionService.saveTinkTransactions(transactions, user.getId());

            log.info(
                    "Successfully synced {} transactions for user {}:{}",
                    transactions.path("transactions").size(),
                    user.getEmail(),
                    user.getName()
            );
            pageToken = transactions.get("nextPageToken").asText();
            pageToken = pageToken.isEmpty() ? null :  pageToken;
        } while (pageToken != null);

    }

    private LockConfiguration getUserLockConfig(User user) {
        return new LockConfiguration(
                Instant.now(),
                "sync_user_" + user.getId(),
                Duration.ofMinutes(2),
                Duration.ofSeconds(1)
        );
    }

}
