package org.expns_tracker.ExpnsTracker.observer;

import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.entity.User;

import java.util.List;

public interface TransactionObserver {
    void onTransactionsSaved(User user);
}
