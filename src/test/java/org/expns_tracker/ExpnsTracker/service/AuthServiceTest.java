package org.expns_tracker.ExpnsTracker.service;

import com.google.firebase.ErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpSession;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthService authService;

    private MockedStatic<FirebaseAuth> firebaseAuthMockedStatic;
    private FirebaseAuth firebaseAuthMock;

    @BeforeEach
    void setUp() {
        firebaseAuthMock = mock(FirebaseAuth.class);
        firebaseAuthMockedStatic = mockStatic(FirebaseAuth.class);
        firebaseAuthMockedStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuthMock);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        if (firebaseAuthMockedStatic != null) {
            firebaseAuthMockedStatic.close();
        }

        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticateUser_NewUser_SyncsAndSetsContext() throws FirebaseAuthException {
        String rawToken = "valid-firebase-token";
        String uid = "user-123";
        String email = "test@example.com";

        FirebaseToken mockToken = mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn(uid);
        when(mockToken.getEmail()).thenReturn(email);
        when(firebaseAuthMock.verifyIdToken(rawToken)).thenReturn(mockToken);

        when(userRepository.findById(uid)).thenReturn(null);

//        doNothing().when(userRepository).save(any(User.class));

        authService.authenticateUser(rawToken, session);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(uid, savedUser.getId());
        assertEquals(email, savedUser.getEmail());
        assertFalse(savedUser.getProfileCompleted());
        assertTrue(savedUser.getNotifBudget());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(uid, auth.getPrincipal());
        assertEquals(email, auth.getCredentials());

        verify(session).setAttribute(
                eq(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY),
                any(SecurityContext.class)
        );
    }

    @Test
    void authenticateUser_ExistingUser_skipsSave() throws FirebaseAuthException {
        String rawToken = "valid-firebase-token";
        String uid = "existing-user-123";
        String email = "existing@example.com";

        FirebaseToken mockToken = mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn(uid);
        when(mockToken.getEmail()).thenReturn(email);
        when(firebaseAuthMock.verifyIdToken(rawToken)).thenReturn(mockToken);

        User existingUser = User.builder().id(uid).email(email).build();
        when(userRepository.findById(uid)).thenReturn(existingUser);

        authService.authenticateUser(rawToken, session);

        verify(userRepository, never()).save(any(User.class));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(uid, auth.getPrincipal());
    }

    @Test
    void authenticateUser_InvalidToken_ThrowsException() throws FirebaseAuthException {
        String invalidToken = "invalid-token";

        when(firebaseAuthMock.verifyIdToken(invalidToken))
                .thenThrow(new FirebaseAuthException(ErrorCode.INVALID_ARGUMENT, "Invalid token", null, null, null));

        assertThrows(FirebaseAuthException.class, () -> {
            authService.authenticateUser(invalidToken, session);
        });

        verifyNoInteractions(userRepository);
        verifyNoInteractions(session);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}