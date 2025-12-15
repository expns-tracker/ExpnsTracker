package org.expns_tracker.ExpnsTracker.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.entity.Category;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CategoryRepository {
    private final Firestore firestore;

    private final String COLLECTION_NAME = "categories";

    public Category findById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            return snapshot.toObject(Category.class);
        }
        return null;
    }

    public List<Category> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(Category.class))
                .collect(Collectors.toList());
    }
}
