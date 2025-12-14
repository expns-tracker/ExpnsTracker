package org.expns_tracker.ExpnsTracker.repository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
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



    public List<Transaction> findByUserIdAndMonth(String userId, int year, int month) throws ExecutionException, InterruptedException {

        // Start of month
        LocalDate start = LocalDate.of(year, month, 1);
        Instant startInstant = start.atStartOfDay(ZoneId.systemDefault()).toInstant();

        // End of month
        LocalDate end = start.plusMonths(1).minusDays(1);
        Instant endInstant = end.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        CollectionReference transactionsRef = firestore.collection(COLLECTION_NAME);

        // Query Firebase: userId = userId AND timestamp BETWEEN start AND end
        Query query = transactionsRef
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("timestamp", startInstant.toEpochMilli())
                .whereLessThanOrEqualTo("timestamp", endInstant.toEpochMilli());

        List<Transaction> result = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            Transaction transaction = doc.toObject(Transaction.class);
            result.add(transaction);
        }

        return result;
    }

}
