package org.expns_tracker.ExpnsTracker.service;


import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TinkService tinkService;

    @Test
    public void getTinkUserId_Success() throws ExecutionException, InterruptedException {
        String userId = "valid-user-id";
        User mockUser = User.builder()
                .id(userId)
                .tinkUserId("tink-user-id")
                .build();

        when(userRepository.findById(userId)).thenReturn(mockUser);

        String tinkUserId = userService.getTinkUserId(userId);

        assertEquals("tink-user-id", tinkUserId);

    }

    @Test
    public void getTinkUserId_Success_Null() throws ExecutionException, InterruptedException {
        String userId = "valid-user-id";
        User mockUser = User.builder()
                .id(userId)
                .build();

        when(userRepository.findById(userId)).thenReturn(mockUser);
        when(tinkService.createPermanentUser(userId)).thenReturn("tink-user-id");
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        String tinkUserId = userService.getTinkUserId(userId);

        assertEquals("tink-user-id", tinkUserId);
    }

    @Test
    public void getTinkUserId_Failure_UserNotFound() throws ExecutionException, InterruptedException {
        String userId = "invalid-user-id";

        when(userRepository.findById(userId)).thenReturn(null);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getTinkUserId(userId));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void setTinkUserId_Success() throws ExecutionException, InterruptedException {
        String userId = "valid-user-id";
        String newTinkUserId = "new-tink-user-id";

        User mockUser = User.builder()
                .id(userId)
                .tinkUserId("tink-user-id")
                .build();

        when(userRepository.save(mockUser)).thenReturn(mockUser);
        when(userRepository.findById(userId)).thenReturn(mockUser);

        userService.setTinkUserId(userId, newTinkUserId);

        assertEquals(newTinkUserId, userService.getTinkUserId(userId));
    }
}
