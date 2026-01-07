package org.expns_tracker.ExpnsTracker.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.expns_tracker.ExpnsTracker.entity.Feedback;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FeedbackRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "feedback";

    public Feedback save(Feedback feedback) {
        log.info("Saving feedback {}", feedback);
        DocumentReference docRef;

        if (feedback.getId() == null) {
            docRef = firestore.collection(COLLECTION_NAME).document();
            feedback.setId(docRef.getId());
            try {
                docRef.set(feedback).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            docRef = firestore.collection(COLLECTION_NAME).document(feedback.getId());
            try {
                docRef.set(feedback).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        DocumentSnapshot snapshot = null;
        try {
            snapshot = docRef.get().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return snapshot.toObject(Feedback.class);
    }

    public List<Feedback> findAllPaginated(int page, int size, String status, String category) {
        try {
            Query query = firestore.collection(COLLECTION_NAME);

            if (status != null && !status.isEmpty()) {
                query = query.whereEqualTo("status", status);
            }

            if (category != null && !category.isEmpty()) {
                query = query.whereEqualTo("category", category);
            }

            query = query.orderBy("createdAt", Query.Direction.DESCENDING)
                    .offset(page * size)
                    .limit(size);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            return querySnapshot.get().toObjects(Feedback.class);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to fetch paginated feedback", e);
            throw new RuntimeException(e);
        }
    }

    public long count(String status) {
        try {
            Query query = firestore.collection(COLLECTION_NAME);

            if (status != null && !status.isEmpty()) {
                query = query.whereEqualTo("status", status);
            }

            AggregateQuery snapshot = query.count();
            return snapshot.get().get().getCount();
        } catch (Exception e) {
            log.error("Failed to count feedback", e);
            return 0;
        }
    }

    public Feedback findById(String id) {
        try {
            DocumentSnapshot snap = firestore.collection(COLLECTION_NAME).document(id).get().get();
            log.info("Found feedback {}", snap);
            return snap.exists() ? snap.toObject(Feedback.class) : null;
        } catch(Exception e) {
            log.error("Failed to find feedback by ID", e);
            return null;
        }
    }
}