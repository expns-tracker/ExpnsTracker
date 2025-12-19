package org.expns_tracker.ExpnsTracker.repository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.entity.enums.TransactionType;
import org.springframework.stereotype.Repository;

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

    public List<Transaction> findByUserIdAndTypeAndMonth(String userId, TransactionType type, int year, int month) throws ExecutionException, InterruptedException {

        LocalDate startLocalDate = LocalDate.of(year, month, 1);
        Date startDate = Date.from(startLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(startLocalDate.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        CollectionReference transactionsRef = firestore.collection(COLLECTION_NAME);

        Query query = transactionsRef
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThan("date", endDate);

        List<Transaction> result = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            Transaction transaction = doc.toObject(Transaction.class);
            result.add(transaction);
        }

        return result;
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

    public List<Transaction> findByUserIdAndDateBetween(String userId, Timestamp start, Timestamp end) throws ExecutionException, InterruptedException {
        return firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", start)
                .whereLessThan("date", end)
                .get()
                .get()
                .toObjects(Transaction.class);
    }
}
