package org.expns_tracker.ExpnsTracker.service;

import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.expns_tracker.ExpnsTracker.repository.UserRepository;
import org.springframework.stereotype.Service;


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

        userRepository.save(user);

        return tinkUserId;

    }

    public void setTinkUserId(String userId, String tinkUserId) {
        User user = getUser(userId);
        user.setTinkUserId(tinkUserId);
        userRepository.save(user);
    }


    public User getUser(String userId) {
        User user;
        user = userRepository.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    public void save(User user) {
        userRepository.save(user);
    }
}
