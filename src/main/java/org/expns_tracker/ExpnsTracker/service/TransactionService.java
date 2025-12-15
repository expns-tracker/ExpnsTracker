package org.expns_tracker.ExpnsTracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.mapper.TinkTransactionMapper;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TinkTransactionMapper tinkTransactionMapper;

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

        log.info("Saved {} transactions for user {}", transactionsToSave.size(), userId);
    }
}
