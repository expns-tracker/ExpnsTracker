package org.expns_tracker.ExpnsTracker.service;

import com.google.cloud.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.expns_tracker.ExpnsTracker.config.ApplicationProperties;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.expns_tracker.ExpnsTracker.state.JobContext;
import org.expns_tracker.ExpnsTracker.state.enums.JobType;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class CsvExportService {

    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final LockProvider lockProvider;
    private final ApplicationProperties applicationProperties;



    public void triggerExport(String userId, LocalDate startDate, LocalDate endDate) {
        User user = userService.getUser(userId);

        LockConfiguration lockConfig = new LockConfiguration(
                Instant.now(),
                "export_lock_" + userId,
                Duration.ofSeconds(30),
                Duration.ofMillis(100)
        );

        Optional<SimpleLock> lock = lockProvider.lock(lockConfig);

        if (lock.isEmpty()) {
            throw new IllegalStateException("System busy. Please try again in a moment.");
        }

        try {

            JobContext context = new JobContext(user, userService, JobType.EXPORT);
            context.requestStart(() -> runBackgroundProcess(userId, context, startDate, endDate));

        } finally {
            lock.get().unlock();
        }


    }

    private void runBackgroundProcess(String userId, JobContext context, LocalDate startDate, LocalDate endDate){
        Thread.ofVirtual().start(() -> {
            try {
                log.info("Generating CSV for user {}", userId);

                Path userDir = Paths.get(applicationProperties.getExportDir(), userId);
                Files.createDirectories(userDir);

                Date startUtilDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Timestamp startTs = Timestamp.of(startUtilDate);

                Date endUtilDate = Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                Timestamp endTs = Timestamp.of(endUtilDate);

                List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(userId, startTs, endTs);

                log.info("Found {} transactions to export.", transactions.size());

                String csvContent = generateCsv(transactions);

                String fileName = "transactions_" + System.currentTimeMillis() + ".csv";
                Path filePath = userDir.resolve(fileName);

                Files.writeString(filePath, csvContent);
                log.info("Saved CSV to local disk: {}", filePath.toAbsolutePath());


                User user = userService.getUser(userId);
                user.setLastExportContent(filePath.toString());
                user.setLastExportTime(Timestamp.now());
                userService.save(user);


                context.signalSuccess();

            } catch (Exception e) {
                log.error("Export failed", e);
                context.signalFailure(e.getMessage());
            }
        });
    }

    public Resource getLastExportResource(String userId) {
        User user = userService.getUser(userId);
        String pathStr = user.getLastExportContent();

        if (pathStr == null || pathStr.isEmpty()) {
            return null;
        }

        Path path = Paths.get(pathStr);
        if (Files.exists(path)) {
            return new FileSystemResource(path);
        }
        return null;
    }

    private String generateCsv(List<Transaction> transactions) {
        StringBuilder sb = new StringBuilder();
        sb.append("Date,Amount,Category,Description\n");
        for (Transaction t : transactions) {
            sb.append(t.getDate()).append(",")
                    .append(t.getAmount()).append(",")
                    .append(t.getCategoryId()).append(",")
                    .append("\"").append(t.getDescription().replace("\"", "\"\"")).append("\"\n");
        }
        return sb.toString();
    }
}