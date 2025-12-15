package org.expns_tracker.ExpnsTracker.service;

import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.expns_tracker.ExpnsTracker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TinkService tinkService;
    private final TransactionRepository transactionRepository;


    public String getTinkUserId(String userId) {
        User user = getUser(userId);

        if (user.getTinkUserId() != null){
            return user.getTinkUserId();
        }

        String tinkUserId = tinkService.createPermanentUser(userId);
        user.setTinkUserId(tinkUserId);

        try {
            userRepository.save(user);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return tinkUserId;

    }

    public void setTinkUserId(String userId, String tinkUserId) {
        User user = getUser(userId);
        user.setTinkUserId(tinkUserId);
        try {
            userRepository.save(user);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public User getUser(String userId) {
        User user;
        try {
            user = userRepository.findById(userId);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    public void save(User user) {
        try {
            userRepository.save(user);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Double getCurrentMonthExpenses(String userId) {

        LocalDate today = LocalDate.now();
        int year = today.getYear();       // ex: 2025
        int month = today.getMonthValue(); // 1 = January, 12 = December

        try {
            List<Transaction> transactions=transactionRepository.findByUserIdAndMonth(userId,year, month);
            return transactions.stream().mapToDouble(Transaction::getAmount).sum();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
