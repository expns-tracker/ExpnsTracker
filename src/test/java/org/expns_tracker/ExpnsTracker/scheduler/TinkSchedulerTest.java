package org.expns_tracker.ExpnsTracker.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.repository.UserRepository;
import org.expns_tracker.ExpnsTracker.service.TinkService;
import org.expns_tracker.ExpnsTracker.service.TransactionService;
import org.expns_tracker.ExpnsTracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TinkSchedulerTest {

    @InjectMocks
    private TinkScheduler tinkScheduler;

    @Mock
    private UserRepository userRepository;
    @Mock
    private TinkService tinkService;
    @Mock
    private LockProvider lockProvider;
    @Mock
    private UserService userService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private SimpleLock simpleLock;

    private User user;
    private ObjectNode transactionsJson;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("user1")
                .email("test@test.com")
                .exportStatus("IDLE")
                .tinkUserId("tink_u1")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        transactionsJson = mapper.createObjectNode();
        ArrayNode txArray = mapper.createArrayNode();
        transactionsJson.set("transactions", txArray);
        transactionsJson.put("nextPageToken", "");
    }

    @Test
    @DisplayName("syncAllUsers: Should sync all users when lock is acquired")
    void syncAllUsers_Success() throws ExecutionException, InterruptedException {
        when(userRepository.findAllByTinkUserIdNotNull()).thenReturn(List.of(user));

        when(lockProvider.lock(any(LockConfiguration.class))).thenReturn(Optional.of(simpleLock));

        when(tinkService.getUserAccessCode("tink_u1")).thenReturn("auth_code");
        when(tinkService.getAccessToken("auth_code")).thenReturn("access_token");
        when(tinkService.fetchTransactions("access_token", null)).thenReturn(transactionsJson);
        when(userService.getUser(user.getId())).thenReturn(user);
        doNothing().when(userService).save(any(User.class));
        doNothing().when(transactionService).saveTinkTransactions(any(), any());

        tinkScheduler.syncAllUsers();

        verify(tinkService).fetchTransactions("access_token", null);
        verify(transactionService).saveTinkTransactions(transactionsJson, "user1");
        verify(simpleLock).unlock();
    }

    @Test
    @DisplayName("syncAllUsers: Should skip user if lock is not acquired")
    void syncAllUsers_LockBusy() throws ExecutionException, InterruptedException {
        // Arrange
        when(userRepository.findAllByTinkUserIdNotNull()).thenReturn(List.of(user));
        when(lockProvider.lock(any(LockConfiguration.class))).thenReturn(Optional.empty());

        tinkScheduler.syncAllUsers();

        verify(tinkService, never()).getUserAccessCode(anyString());
        verify(lockProvider).lock(any());
    }

    @Test
    @DisplayName("syncAllUsers: Should handle exception during user sync gracefully")
    void syncAllUsers_Exception() throws ExecutionException, InterruptedException {

        when(userRepository.findAllByTinkUserIdNotNull()).thenReturn(List.of(user));
        when(lockProvider.lock(any())).thenReturn(Optional.of(simpleLock));

        when(tinkService.getUserAccessCode("tink_u1")).thenThrow(new RuntimeException("API Error"));
        when(userService.getUser(user.getId())).thenReturn(user);
        doNothing().when(userService).save(any(User.class));

        tinkScheduler.syncAllUsers();


        verify(tinkService).getUserAccessCode("tink_u1");
        verify(transactionService, never()).saveTinkTransactions(any(), any());
        verify(simpleLock).unlock();
    }

    @Test
    @DisplayName("syncAllUsers: Should handle pagination")
    void syncAllUsers_Pagination() throws ExecutionException, InterruptedException {

        when(userRepository.findAllByTinkUserIdNotNull()).thenReturn(List.of(user));
        when(lockProvider.lock(any())).thenReturn(Optional.of(simpleLock));
        when(tinkService.getUserAccessCode("tink_u1")).thenReturn("code");
        when(tinkService.getAccessToken("code")).thenReturn("token");

        ObjectNode page1 = transactionsJson.deepCopy();
        page1.put("nextPageToken", "page_2_token");

        ObjectNode page2 = transactionsJson.deepCopy();
        page2.put("nextPageToken", "");

        when(tinkService.fetchTransactions("token", null)).thenReturn(page1);
        when(tinkService.fetchTransactions("token", "page_2_token")).thenReturn(page2);
        when(userService.getUser(user.getId())).thenReturn(user);
        doNothing().when(userService).save(any(User.class));
        doNothing().when(transactionService).saveTinkTransactions(any(), any());

        tinkScheduler.syncAllUsers();

        verify(tinkService, times(2)).fetchTransactions(eq("token"), any());
        verify(transactionService, times(2)).saveTinkTransactions(any(), eq("user1"));
    }

    @Test
    @DisplayName("syncAllUsers: Should handle DB error fetching users")
    void syncAllUsers_DbError() throws ExecutionException, InterruptedException {
        when(userRepository.findAllByTinkUserIdNotNull()).thenThrow(new ExecutionException(new RuntimeException("DB Down")));

        assertThrows(RuntimeException.class, () -> tinkScheduler.syncAllUsers());
    }

    @Test
    @DisplayName("syncSingleUser: Should throw exception if lock is busy")
    void syncSingleUser_LockBusy() {
        when(userService.getUser("user1")).thenReturn(user);
        when(lockProvider.lock(any())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> tinkScheduler.syncSingleUser("user1"));
    }

    @Test
    @DisplayName("syncSingleUser: Should start async sync when lock acquired")
    void syncSingleUser_Success() throws InterruptedException {
        when(userService.getUser("user1")).thenReturn(user);
        when(lockProvider.lock(any())).thenReturn(Optional.of(simpleLock));

        when(tinkService.getUserAccessCode("tink_u1")).thenReturn("code");
        when(tinkService.getAccessToken("code")).thenReturn("token");
        when(tinkService.fetchTransactions("token", null)).thenReturn(transactionsJson);

        tinkScheduler.syncSingleUser("user1");

        Thread.sleep(100);

        verify(tinkService).fetchTransactions("token", null);
        verify(simpleLock).unlock();
    }

    @Test
    @DisplayName("syncSingleUser: Should handle exception in background thread")
    void syncSingleUser_AsyncException() throws InterruptedException {
        when(userService.getUser("user1")).thenReturn(user);
        when(lockProvider.lock(any())).thenReturn(Optional.of(simpleLock));
        when(tinkService.getUserAccessCode("tink_u1")).thenThrow(new RuntimeException("Async Error"));

        tinkScheduler.syncSingleUser("user1");

        Thread.sleep(100);

        verify(tinkService).getUserAccessCode("tink_u1");
        verify(simpleLock).unlock();
    }
}