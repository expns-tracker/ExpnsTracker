package org.expns_tracker.ExpnsTracker.observer.concrete;

import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.entity.enums.TransactionType;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentMonthBudgetUpdateObserverTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CurrentMonthBudgetUpdateObserver budgetUpdateObserver;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("user-1");
        user.setEmail("test@test.com");
    }

    @Test
    @DisplayName("onTransactionsSaved: Should calculate income and expenses correctly")
    void onTransactionsSaved_CalculatesTotals() throws ExecutionException, InterruptedException {

        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        Transaction t1 = new Transaction();
        t1.setAmount(-50.0);
        t1.setType(TransactionType.EXPENSE);

        Transaction t2 = new Transaction();
        t2.setAmount(20.0);
        t2.setType(TransactionType.EXPENSE);

        Transaction t3 = new Transaction();
        t3.setAmount(100.0);
        t3.setType(TransactionType.INCOME);

        Transaction t4 = new Transaction();
        t4.setAmount(-10.0);
        t4.setType(TransactionType.INCOME);

        when(transactionRepository.findByUserIdAndMonth(eq("user-1"), eq(year), eq(month)))
                .thenReturn(List.of(t1, t2, t3, t4));

        budgetUpdateObserver.onTransactionsSaved(user);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(70.0, savedUser.getCurrentMonthExpenses(), 0.001);

        assertEquals(110.0, savedUser.getCurrentMonthIncome(), 0.001);
    }

    @Test
    @DisplayName("onTransactionsSaved: Should handle empty transactions list (reset to 0)")
    void onTransactionsSaved_NoTransactions() throws ExecutionException, InterruptedException {
        LocalDate today = LocalDate.now();
        when(transactionRepository.findByUserIdAndMonth(eq("user-1"), eq(today.getYear()), eq(today.getMonthValue())))
                .thenReturn(Collections.emptyList());

        user.setCurrentMonthExpenses(500.0);
        user.setCurrentMonthIncome(500.0);

        budgetUpdateObserver.onTransactionsSaved(user);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(0.0, savedUser.getCurrentMonthExpenses());
        assertEquals(0.0, savedUser.getCurrentMonthIncome());
    }

    @Test
    @DisplayName("onTransactionsSaved: Should throw RuntimeException on Repo failure")
    void onTransactionsSaved_RepoException() throws ExecutionException, InterruptedException {

        LocalDate today = LocalDate.now();
        when(transactionRepository.findByUserIdAndMonth(anyString(), anyInt(), anyInt()))
                .thenThrow(new ExecutionException(new RuntimeException("DB Connection Failed")));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                budgetUpdateObserver.onTransactionsSaved(user)
        );

        assertEquals("java.util.concurrent.ExecutionException: java.lang.RuntimeException: DB Connection Failed", ex.getMessage());

        verify(userRepository, never()).save(any());
    }
}