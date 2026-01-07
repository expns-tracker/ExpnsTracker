package org.expns_tracker.ExpnsTracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.expns_tracker.ExpnsTracker.dto.TransactionDto;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.entity.enums.TransactionType;
import org.expns_tracker.ExpnsTracker.mapper.TinkTransactionMapper;
import org.expns_tracker.ExpnsTracker.mapper.TransactionDtoMapper;
import org.expns_tracker.ExpnsTracker.observer.TransactionObserver;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TinkTransactionMapper tinkTransactionMapper;
    @Mock
    private TransactionDtoMapper transactionDtoMapper;
    @Mock
    private UserService userService;
    @Mock
    private TransactionObserver observer1;
    @Mock
    private TransactionObserver observer2;

    private User user;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        // Manually inject the observers list to ensure iteration works correctly
        List<TransactionObserver> observers = List.of(observer1, observer2);

        transactionService = new TransactionService(
                transactionRepository,
                tinkTransactionMapper,
                transactionDtoMapper,
                userService,
                observers
        );

        user = new User();
        user.setId("user-1");
        user.setEmail("test@test.com");

        transaction = new Transaction();
        transaction.setId("tx-1");
        transaction.setAmount(100.0);
        transaction.setUserId("user-1");
        transaction.setType(TransactionType.EXPENSE);
    }

    @Test
    @DisplayName("saveTinkTransactions: Should map, save, and notify observers")
    void saveTinkTransactions_Success() {
        // Arrange
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ArrayNode transactionsArray = mapper.createArrayNode();
        ObjectNode txNode = mapper.createObjectNode();
        txNode.put("id", "tink-tx-1");
        transactionsArray.add(txNode);
        root.set("transactions", transactionsArray);

        when(tinkTransactionMapper.mapTinkTransaction(any(), eq("user-1"))).thenReturn(transaction);
        when(userService.getUser("user-1")).thenReturn(user);

        // Act
        transactionService.saveTinkTransactions(root, "user-1");

        // Assert
        verify(tinkTransactionMapper).mapTinkTransaction(any(), eq("user-1"));

        ArgumentCaptor<List<Transaction>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(transactionRepository).saveAll(listCaptor.capture());
        assertEquals(1, listCaptor.getValue().size());
        assertEquals("tx-1", listCaptor.getValue().get(0).getId());

        // Verify Observers notified
        verify(observer1).onTransactionsSaved(user);
        verify(observer2).onTransactionsSaved(user);
    }

    @Test
    @DisplayName("getCurrentMonthExpenses: Should return expenses for current month")
    void getCurrentMonthExpenses_Success() throws ExecutionException, InterruptedException {
        // Arrange
        LocalDate today = LocalDate.now();
        when(transactionRepository.findByUserIdAndTypeAndMonth(
                "user-1",
                TransactionType.EXPENSE,
                today.getYear(),
                today.getMonthValue()
        )).thenReturn(List.of(transaction));

        // Act
        List<Transaction> result = transactionService.getCurrentMonthExpenses("user-1");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getCurrentMonthExpenses: Should throw RuntimeException on Repo failure")
    void getCurrentMonthExpenses_Exception() throws ExecutionException, InterruptedException {
        // Arrange
        when(transactionRepository.findByUserIdAndTypeAndMonth(any(), any(), anyInt(), anyInt()))
                .thenThrow(new ExecutionException(new Throwable("DB Error")));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> transactionService.getCurrentMonthExpenses("user-1"));
    }

    @Test
    @DisplayName("getRecentTransactions: Should return N transactions")
    void getRecentTransactions_Success() throws ExecutionException, InterruptedException {
        // Arrange
        when(transactionRepository.findNByUserIdOrderByDate("user-1", 5))
                .thenReturn(List.of(transaction));

        // Act
        List<Transaction> result = transactionService.getRecentTransactions("user-1", 5);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("addTransaction: Should set negative amount for EXPENSE")
    void addTransaction_Expense() {
        // Arrange
        transaction.setAmount(50.0); // Positive input
        transaction.setType(TransactionType.EXPENSE);
        transaction.setId(null); // Ensure ID generation

        when(userService.getUser("user-1")).thenReturn(user);

        // Act
        transactionService.addTransaction(transaction, "user-1");

        // Assert
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction saved = captor.getValue();
        assertEquals(-50.0, saved.getAmount(), "Expense amount should be negative");
        assertNotNull(saved.getId(), "ID should be generated");
        assertNotNull(saved.getDate(), "Date should be set");
        assertEquals("user-1", saved.getUserId());

        verify(observer1).onTransactionsSaved(user);
    }

    @Test
    @DisplayName("addTransaction: Should set positive amount for INCOME")
    void addTransaction_Income() {
        // Arrange
        transaction.setAmount(-100.0); // Negative input
        transaction.setType(TransactionType.INCOME);
        transaction.setId(null);

        when(userService.getUser("user-1")).thenReturn(user);

        // Act
        transactionService.addTransaction(transaction, "user-1");

        // Assert
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        assertEquals(100.0, captor.getValue().getAmount(), "Income amount should be positive");
    }

    @Test
    @DisplayName("updateTransaction: Should preserve old data and update amount sign")
    void updateTransaction_Success() throws ExecutionException, InterruptedException {
        Transaction oldTx = new Transaction();
        oldTx.setId("tx-1");
        oldTx.setCreatedAt(com.google.cloud.Timestamp.now());

        Transaction updateTx = new Transaction();
        updateTx.setId("tx-1");
        updateTx.setUserId("user-1");
        updateTx.setAmount(200.0);
        updateTx.setType(TransactionType.EXPENSE);

        when(transactionRepository.findById("tx-1")).thenReturn(oldTx);
        when(userService.getUser("user-1")).thenReturn(user);

        transactionService.updateTransaction(updateTx);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction saved = captor.getValue();
        assertEquals(-200.0, saved.getAmount());
        assertEquals(oldTx.getCreatedAt(), saved.getCreatedAt(), "Should preserve original createdAt");
        assertNull(saved.getUpdatedAt(), "UpdatedAt should be null (or handled by logic)"); // Logic sets it to null explicitly

        verify(observer1).onTransactionsSaved(user);
    }

    @Test
    @DisplayName("getTransactions: Should map to DTOs")
    void getTransactions_Pagination() {
        when(transactionRepository.findByUserIdAndPage("user-1", 0, 10))
                .thenReturn(List.of(transaction));
        when(transactionDtoMapper.map(transaction)).thenReturn(new TransactionDto());

        List<TransactionDto> result = transactionService.getTransactions("user-1", 0, 10);

        assertEquals(1, result.size());
        verify(transactionDtoMapper).map(transaction);
    }

    @Test
    @DisplayName("delete: Should delete and notify")
    void delete_Success() {
        when(userService.getUser("user-1")).thenReturn(user);

        transactionService.delete("tx-1", "user-1");

        verify(transactionRepository).delete("tx-1");
        verify(observer1).onTransactionsSaved(user);
    }

    @Test
    @DisplayName("notifyObservers: Should handle observer exception gracefully")
    void notifyObservers_ExceptionSafe() {
        doThrow(new RuntimeException("Observer Failed")).when(observer1).onTransactionsSaved(any());

        when(userService.getUser("user-1")).thenReturn(user);

        transactionService.delete("tx-1", "user-1");

        verify(observer2).onTransactionsSaved(user);
    }
}