package org.expns_tracker.ExpnsTracker.observer.concrete;

import org.expns_tracker.ExpnsTracker.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetAlertObserverTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private BudgetAlertObserver budgetAlertObserver;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("user-1");
        user.setEmail("test@expnstracker.org");
        user.setFirstName("Test");
        user.setLastName("Test");
        user.setMonthlyBudgetLimit(1000.0);
    }

    @Test
    @DisplayName("Should do nothing if budget limit is not set")
    void onTransactionsSaved_NoBudgetSet() {
        user.setMonthlyBudgetLimit(null);
        user.setCurrentMonthExpenses(5000.0);

        budgetAlertObserver.onTransactionsSaved(user);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should do nothing if spending is well below threshold (< 90%)")
    void onTransactionsSaved_SafeSpending() {
        user.setCurrentMonthExpenses(500.0);

        budgetAlertObserver.onTransactionsSaved(user);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send WARNING email if spending is between 90% and 100%")
    void onTransactionsSaved_WarningThreshold() {
        user.setCurrentMonthExpenses(900.0);

        budgetAlertObserver.onTransactionsSaved(user);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getTo());
        assertEquals("test@expnstracker.org", sentMessage.getTo()[0]);
        assertNotNull(sentMessage.getSubject());
        assertTrue(sentMessage.getSubject().contains("Approaching Budget Limit"));
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains("You are close to your monthly budget"));
    }

    @Test
    @DisplayName("Should send EXCEEDED email if spending is > 100%")
    void onTransactionsSaved_BudgetExceeded() {
        user.setCurrentMonthExpenses(1001.0);

        budgetAlertObserver.onTransactionsSaved(user);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getSubject());
        assertTrue(sentMessage.getSubject().contains("Budget Exceeded!"));
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains("You have exceeded your monthly budget"));
    }

    @Test
    @DisplayName("Should treat null current expenses as 0.0")
    void onTransactionsSaved_NullCurrentExpenses() {
        user.setCurrentMonthExpenses(null); // Should default to 0.0

        budgetAlertObserver.onTransactionsSaved(user);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should not crash if User has no email address")
    void onTransactionsSaved_NoEmailAddress() {
        user.setEmail(null);
        user.setCurrentMonthExpenses(2000.0);

        budgetAlertObserver.onTransactionsSaved(user);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should handle MailException gracefully")
    void onTransactionsSaved_MailSenderFails() {
        user.setCurrentMonthExpenses(2000.0);

        doThrow(new RuntimeException("Mail Server Down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> budgetAlertObserver.onTransactionsSaved(user));

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}