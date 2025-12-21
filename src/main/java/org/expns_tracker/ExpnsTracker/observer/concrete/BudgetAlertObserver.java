package org.expns_tracker.ExpnsTracker.observer.concrete;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.observer.TransactionObserver;
import org.springframework.core.annotation.Order;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j2
@Component
@Order(3)
@RequiredArgsConstructor
public class BudgetAlertObserver implements TransactionObserver {

    private static final double WARNING_THRESHOLD = 0.90;
    private final JavaMailSender mailSender;

    @Override
    public void onTransactionsSaved(User user) {
        if (user.getMonthlyBudgetLimit() == null || user.getMonthlyBudgetLimit() <= 0) {
            return;
        }

        Double currentSpent = user.getCurrentMonthExpenses();
        Double limit = user.getMonthlyBudgetLimit();

        if (currentSpent == null) currentSpent = 0.0;

        if (currentSpent > limit) {
            log.warn("User {} has exceeded their monthly budget! Spent: {}, Limit: {}",
                    user.getEmail(), currentSpent, limit);

            sendEmail(user, "Budget Exceeded!",
                    String.format("You have exceeded your monthly budget.\nLimit: %.2f\nSpent: %.2f", limit, currentSpent));
        }

        else if (currentSpent >= (limit * WARNING_THRESHOLD)) {
            log.info("User {} is near their budget limit ({}%).",
                    user.getEmail(), (currentSpent / limit) * 100);

            sendEmail(user, "Approaching Budget Limit",
                    String.format("You are close to your monthly budget.\nLimit: %.2f\nSpent: %.2f", limit, currentSpent));
        }
    }

    private void sendEmail(User user, String subject, String text) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            log.warn("Cannot send email: User {} has no email address.", user.getId());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@expnstracker.org");
            message.setTo(user.getEmail());
            message.setSubject("ExpnsTracker Alert: " + subject);
            message.setText("Hello " + user.getName() + ",\n\n" + text + "\n\nBest regards,\nExpnsTracker Team");

            mailSender.send(message);
            log.info("Email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email to {}", user.getEmail(), e);
        }
    }
}