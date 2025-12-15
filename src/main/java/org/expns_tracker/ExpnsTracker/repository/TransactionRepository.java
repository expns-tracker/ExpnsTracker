package org.expns_tracker.ExpnsTracker.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
@Log4j2
public class TransactionRepository {

    private final Firestore firestore;

    private static final String COLLECTION_NAME = "transactions";

    public ApiFuture<WriteResult> save(Transaction transaction) {
        return firestore.collection(COLLECTION_NAME).document(transaction.getId()).set(transaction);
    }

    public Transaction findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot =
                firestore.collection(COLLECTION_NAME).document(id).get().get();

        return snapshot.exists() ? snapshot.toObject(Transaction.class) : null;
    }

    public ApiFuture<WriteResult> delete(String id) {
        return firestore.collection(COLLECTION_NAME).document(id).delete();
    }

    public void saveAll(List<Transaction> transactionsToSave) {
        if (transactionsToSave == null || transactionsToSave.isEmpty()) {
            return;
        }

        final int BATCH_SIZE = 500;

        for (int i = 0; i < transactionsToSave.size(); i += BATCH_SIZE) {
            WriteBatch batch = firestore.batch();

            int end = Math.min(i + BATCH_SIZE, transactionsToSave.size());
            List<Transaction> batchList = transactionsToSave.subList(i, end);

            for (Transaction transaction : batchList) {
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(transaction.getId());
                batch.set(docRef, transaction);
            }

            try {
                batch.commit().get();
                log.info("Saved batch of {} transactions.", batchList.size());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to save transaction batch", e);
                throw new RuntimeException("Firestore batch save failed", e);
            }
        }

    }
}
