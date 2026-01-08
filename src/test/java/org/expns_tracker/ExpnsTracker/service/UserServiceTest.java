package org.expns_tracker.ExpnsTracker.service;


import org.expns_tracker.ExpnsTracker.config.ApplicationProperties;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.entity.enums.Role;
import org.expns_tracker.ExpnsTracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TinkService tinkService;

    @Mock
    private ApplicationProperties applicationProperties;

    @Test
    void getTinkUserId_Success() throws ExecutionException, InterruptedException {
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
    void getTinkUserId_Success_Null() throws ExecutionException, InterruptedException {
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
    void getTinkUserId_Failure_UserNotFound() throws ExecutionException, InterruptedException {
        String userId = "invalid-user-id";

        when(userRepository.findById(userId)).thenReturn(null);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getTinkUserId(userId));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void setTinkUserId_Success() throws ExecutionException, InterruptedException {
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

    @Test
    void toggleUserStatus_Deactivate_Success() {

        String userId = "user-1";
        User user = new User();
        user.setId(userId);
        user.setEmail("regular@test.com");
        user.setIsActive(true);

        when(userRepository.findById(userId)).thenReturn(user);
        when(applicationProperties.getSuperadmin()).thenReturn("super@admin.com");

        userService.toggleUserStatus(userId);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertFalse(savedUser.getIsActive(), "User status should be flipped to false");
    }

    @Test
    void toggleUserStatus_Activate_Success() {
        String userId = "user-2";
        User user = new User();
        user.setId(userId);
        user.setEmail("regular@test.com");
        user.setIsActive(false);

        when(userRepository.findById(userId)).thenReturn(user);
        when(applicationProperties.getSuperadmin()).thenReturn("super@admin.com");

        userService.toggleUserStatus(userId);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertTrue(captor.getValue().getIsActive(), "User status should be flipped to true");
    }

    @Test
    void toggleUserStatus_Superadmin_ThrowsException() {

        String superEmail = "super@admin.com";
        User superUser = new User();
        superUser.setId("super-id");
        superUser.setEmail(superEmail);
        superUser.setIsActive(true);

        when(userRepository.findById("super-id")).thenReturn(superUser);
        when(applicationProperties.getSuperadmin()).thenReturn(superEmail);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                userService.toggleUserStatus("super-id")
        );

        assertEquals("Cannot make the superadmin user inactive", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void promoteToAdmin_Success() {
        // Arrange
        String userId = "user-1";
        User user = new User();
        user.setId(userId);
        user.setRole(Role.USER);

        when(userRepository.findById(userId)).thenReturn(user);

        userService.promoteToAdmin(userId);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertEquals(Role.ADMIN, captor.getValue().getRole());
    }

    @Test
    void revokeAdminRole_Success() {
        String userId = "admin-1";
        User adminUser = new User();
        adminUser.setId(userId);
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(Role.ADMIN);

        when(userRepository.findById(userId)).thenReturn(adminUser);
        when(applicationProperties.getSuperadmin()).thenReturn("super@admin.com");

        userService.revokeAdminRole(userId);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertEquals(Role.USER, captor.getValue().getRole());
    }

    @Test
    void revokeAdminRole_Superadmin_ThrowsException() {

        String superEmail = "super@admin.com";
        User superUser = new User();
        superUser.setId("super-id");
        superUser.setEmail(superEmail);
        superUser.setRole(Role.ADMIN);

        when(userRepository.findById("super-id")).thenReturn(superUser);
        when(applicationProperties.getSuperadmin()).thenReturn(superEmail);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                userService.revokeAdminRole("super-id")
        );

        assertEquals("Cannot revoke admin role for the superadmin user", ex.getMessage());
        verify(userRepository, never()).save(any());
    }
}
