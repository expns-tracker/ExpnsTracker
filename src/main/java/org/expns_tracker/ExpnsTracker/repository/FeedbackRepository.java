package org.expns_tracker.ExpnsTracker.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.expns_tracker.ExpnsTracker.entity.Feedback;
import org.springframework.stereotype.Repository;

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
}