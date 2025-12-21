package org.expns_tracker.ExpnsTracker.observer.concrete;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
@Component
@RequiredArgsConstructor
@Order(1)
public class CurrentMonthBudgetUpdateObserver implements TransactionObserver {

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
                    .findByUserIdAndMonth(user.getId(), year, month);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        double newExpenseTotal = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .mapToDouble(transaction -> Math.abs(transaction.getAmount()))
                .sum();

        double newIncomeTotal = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .mapToDouble(transaction -> Math.abs(transaction.getAmount()))
                .sum();

        log.info("New expense total is {}", newExpenseTotal);
        log.info("New income total is {}", newIncomeTotal);


        user.setCurrentMonthExpenses(newExpenseTotal);
        user.setCurrentMonthIncome(newIncomeTotal);

        userRepository.save(user);

        log.info("Updated current month income and expenses for user {}. Added: income={}, expenses={}",
                user.getEmail(), newIncomeTotal, newExpenseTotal);

    }
}