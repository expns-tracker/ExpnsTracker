package org.expns_tracker.ExpnsTracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.entity.enums.TransactionType;
import org.expns_tracker.ExpnsTracker.mapper.TinkTransactionMapper;
import org.expns_tracker.ExpnsTracker.observer.TransactionObserver;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Log4j2
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TinkTransactionMapper tinkTransactionMapper;
    private final UserService userService;
    private final List<TransactionObserver> observers;

    public void saveTinkTransactions(JsonNode rootNode, String userId) {
        List<Transaction> transactionsToSave = new ArrayList<>();
        JsonNode results = rootNode.get("transactions");

        if (results != null && results.isArray()) {
            for (JsonNode node : results) {
                Transaction tx = tinkTransactionMapper.mapTinkTransaction(node, userId);
                transactionsToSave.add(tx);
            }
        }

        transactionRepository.saveAll(transactionsToSave);

        User user = userService.getUser(userId);
        notifyObservers(user, transactionsToSave);

        log.info("Saved {} transactions for user {}", transactionsToSave.size(), userId);
    }

    public List<Transaction> getCurrentMonthExpenses(String userId) {

        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        try {
            return transactionRepository.findByUserIdAndTypeAndMonth(
                    userId,
                    TransactionType.EXPENSE,
                    year,
                    month
            );
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void notifyObservers(User user, List<Transaction> transactions) {
        for (TransactionObserver observer : observers) {
            try {
                observer.onTransactionsSaved(user);
            } catch (Exception e) {
                log.error("Observer {} failed to process transactions", observer.getClass().getSimpleName(), e);
            }
        }
    }
}
