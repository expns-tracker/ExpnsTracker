package org.expns_tracker.ExpnsTracker.observer.concrete;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.entity.enums.TransactionType;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.observer.TransactionObserver;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.expns_tracker.ExpnsTracker.repository.UserRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class BudgetUpdateObserver implements TransactionObserver {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public void onTransactionsSaved(User user) {

        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        List<Transaction> transactions = null;
        try {
            transactions = transactionRepository
                    .findByUserIdAndTypeAndMonth(user.getId(), TransactionType.EXPENSE, year, month);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("{} transactions saved", transactions.size());
        double newExpenseTotal = transactions.stream()
                .mapToDouble(transaction -> Math.abs(transaction.getAmount()))
                .sum();

        log.info("New expense total is {}", newExpenseTotal);

        if (newExpenseTotal > 0) {
            user.setCurrentMonthExpenses(newExpenseTotal);

            userRepository.save(user);

            log.info("Updated budget for user {}. Added: {}. New Total: {}",
                    user.getEmail(), newExpenseTotal, user.getCurrentMonthExpenses());
        }
    }
}