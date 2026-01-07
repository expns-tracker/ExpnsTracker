package org.expns_tracker.ExpnsTracker.observer.concrete;

import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.expns_tracker.ExpnsTracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceUpdateObserverTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BalanceUpdateObserver balanceUpdateObserver;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("user-123");
        user.setEmail("test@example.com");
        user.setBalance(100.0);
    }

    @Test
    @DisplayName("onTransactionsSaved: Should calculate total balance and update user")
    void onTransactionsSaved_UpdatesBalance() {
        Double newCalculatedBalance = 1500.50;

        when(transactionRepository.calculateTotalAmountByUserId(user.getId()))
                .thenReturn(newCalculatedBalance);
        balanceUpdateObserver.onTransactionsSaved(user);

        verify(transactionRepository).calculateTotalAmountByUserId("user-123");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(newCalculatedBalance, savedUser.getBalance(), "User balance should match the calculated total");
        assertEquals("user-123", savedUser.getId());
    }

    @Test
    @DisplayName("onTransactionsSaved: Should handle null return from DB (e.g., no transactions)")
    void onTransactionsSaved_NullBalance() {
        when(transactionRepository.calculateTotalAmountByUserId(user.getId()))
                .thenReturn(null);
        balanceUpdateObserver.onTransactionsSaved(user);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertNull(savedUser.getBalance());
    }
}