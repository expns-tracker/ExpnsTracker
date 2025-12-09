package org.expns_tracker.ExpnsTracker.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.repository.UserRepository;
import org.expns_tracker.ExpnsTracker.service.TinkService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Log4j2
@RequiredArgsConstructor
public class TinkScheduler {

    private final UserRepository userRepository;
    private final TinkService tinkService;

    @Scheduled(cron = "0 0 4 * * *")
    public void syncAllUsers() {
        log.info("Starting scheduled transaction sync...");
        List<User> users;
        try {
            users = userRepository.findAllByTinkUserIdNotNull();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        for  (User user : users) {
            try {
                syncUserTransactions(user);
            } catch (Exception e) {
                log.error(
                        "Failed while syncing transactions for user {}:{}.",
                        user.getEmail(),
                        user.getName(),
                        e
                );
            }
        }

        log.info("Finished scheduled transaction sync...");
    }

    private void syncUserTransactions(User user) {
        log.info("Syncing transactions for user {}:{}", user.getEmail(), user.getName());

        String code = tinkService.getUserAccessCode(user.getTinkUserId());

        String accessToken = tinkService.getAccessToken(code);

        JsonNode transactions = tinkService.fetchTransactions(accessToken);

        log.info(
                "Successfully synced {} transactions for user {}:{}",
                transactions.path("transactions").size(),
                user.getEmail(),
                user.getName()
        );
    }
}
