package org.expns_tracker.ExpnsTracker.observer.concrete;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.observer.TransactionObserver;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.expns_tracker.ExpnsTracker.repository.UserRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
@Order(2)
public class BalanceUpdateObserver implements TransactionObserver {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    @Override
    public void onTransactionsSaved(User user) {
        Double balance = transactionRepository.calculateTotalAmountByUserId(user.getId());

        user.setBalance(balance);
        userRepository.save(user);
        log.info("Updated balance for user {}: {}",
                user.getEmail(), balance);
    }
}
