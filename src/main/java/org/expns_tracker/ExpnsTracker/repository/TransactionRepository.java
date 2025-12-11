package org.expns_tracker.ExpnsTracker.repository;

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
}
