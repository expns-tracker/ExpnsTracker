package org.expns_tracker.ExpnsTracker.service;

import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TinkService tinkService;


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


    private User getUser(String userId) {
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
}
