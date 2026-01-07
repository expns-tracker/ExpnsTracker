package org.expns_tracker.ExpnsTracker.service;

import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.config.ApplicationProperties;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.entity.enums.Role;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.expns_tracker.ExpnsTracker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TinkService tinkService;
    private final ApplicationProperties applicationProperties;


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

    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void toggleUserStatus(String userId) {
        User user = getUser(userId);

        if (user.getEmail().equals(applicationProperties.getSuperadmin())){
            throw new IllegalStateException("Cannot make the superadmin user inactive");
        }

        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    public void promoteToAdmin(String userId) {
        User user = getUser(userId);
        user.setRole(Role.ADMIN);
        userRepository.save(user);
    }

    public void revokeAdminRole(String userId) {
        User user = getUser(userId);

        if (user.getEmail().equals(applicationProperties.getSuperadmin())){
            throw new IllegalStateException("Cannot revoke admin role for the superadmin user");
        }

        user.setRole(Role.USER);
        userRepository.save(user);
    }
}
