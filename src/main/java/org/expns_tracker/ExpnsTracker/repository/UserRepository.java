package org.expns_tracker.ExpnsTracker.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final Firestore firestore;

    private final String COLLECTION_NAME = "users";

    public User save(User user) throws ExecutionException, InterruptedException {
        DocumentReference docRef;

        if (user.getId() == null) {
            docRef = firestore.collection(COLLECTION_NAME).document();
            user.setId(docRef.getId());
            docRef.set(user).get();
        } else {
            docRef = firestore.collection(COLLECTION_NAME).document(user.getId());
            user.setUpdatedAt(null);
            docRef.set(user).get();
        }

        DocumentSnapshot snapshot = docRef.get().get();
        return snapshot.toObject(User.class);
    }

    public User findById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            return snapshot.toObject(User.class);
        }
        return null;
    }

    public User findByEmail(String email) throws ExecutionException, InterruptedException {
        CollectionReference users = firestore.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = users.whereEqualTo("email", email).get();
        List<QueryDocumentSnapshot> documents = query.get().getDocuments();
        if (!documents.isEmpty()) {
            return documents.getFirst().toObject(User.class);
        }
        return null;
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION_NAME).document(id).delete().get();
    }

    public List<User> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(User.class))
                .collect(Collectors.toList());
    }

    public List<User> findAllByTinkUserIdNotNull() throws ExecutionException, InterruptedException {
        CollectionReference users = firestore.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> future = users.whereEqualTo("tinkUserId", null).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(User.class))
                .collect(Collectors.toList());
    }
}
