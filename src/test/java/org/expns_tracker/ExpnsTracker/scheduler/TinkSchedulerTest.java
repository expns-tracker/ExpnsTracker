package org.expns_tracker.ExpnsTracker.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.repository.UserRepository;
import org.expns_tracker.ExpnsTracker.service.TinkService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;


@Slf4j
@ExtendWith(MockitoExtension.class)
public class TinkSchedulerTest {
    @InjectMocks
    private TinkScheduler scheduler;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TinkService tinkService;

    List<User> users = new ArrayList<>();

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException {
        User user1 = User.builder()
                .id("user-id-1")
                .name("Test User 1")
                .email("user1@email.com")
                .tinkUserId("tink-user-id-1")
                .build();

        users.add(user1);

        User user2 = User.builder()
                .id("user-id-2")
                .name("Test User 2")
                .email("user2@email.com")
                .tinkUserId("tink-user-id-2")
                .build();

        users.add(user2);

        ObjectNode rootNode = getTransactions();

        for (User user : users) {
            lenient().when(tinkService.getUserAccessCode(user.getTinkUserId())).thenReturn("code-"+user.getId());
            lenient().when(tinkService.getAccessToken("code-"+user.getId())).thenReturn("token-"+user.getId());
            lenient().when(tinkService.fetchTransactions("token-"+user.getId(), null)).thenReturn(rootNode);
        }

        when(userRepository.findAllByTinkUserIdNotNull()).thenReturn(users);
    }

    @AfterEach
    void tearDown() {
        users.clear();
    }

    @Test
    void syncAllUsers_Success() {

        scheduler.syncAllUsers();

        verify(tinkService).getUserAccessCode("tink-user-id-1");
        verify(tinkService).getAccessToken("code-user-id-1");
        verify(tinkService).fetchTransactions("token-user-id-1", null);

        verify(tinkService).getUserAccessCode("tink-user-id-2");
        verify(tinkService).getAccessToken("code-user-id-2");
        verify(tinkService).fetchTransactions("token-user-id-2", null);

        verifyNoMoreInteractions(tinkService);

    }

    @Test
    void syncAllUsers_FirstUserFailure() {
        when(tinkService.getUserAccessCode("tink-user-id-1"))
                .thenThrow(new RuntimeException("Tink API down for user 1"));

        scheduler.syncAllUsers();

        verify(tinkService, never()).getAccessToken("code-user-id-1");

        verify(tinkService).getUserAccessCode("tink-user-id-2");
        verify(tinkService).getAccessToken("code-user-id-2");
        verify(tinkService).fetchTransactions("token-user-id-2", null);

        verifyNoMoreInteractions(tinkService);
    }

    @NotNull
    private static ObjectNode getTransactions() {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode transaction1 = mapper.createObjectNode();
        transaction1.put("id", "t12345");
        transaction1.put("accountId", "acc_999");
        transaction1.put("amount", 150.50);
        transaction1.put("description", "Starbucks Coffee");
        transaction1.put("date", "2025-12-09");
        transaction1.put("pending", false);

        ObjectNode transaction2 = mapper.createObjectNode();
        transaction2.put("id", "t67890");
        transaction2.put("accountId", "acc_999");
        transaction2.put("amount", -25.00);
        transaction2.put("description", "Netflix Subscription");
        transaction2.put("date", "2025-12-08");
        transaction2.put("pending", true);

        ArrayNode resultsArray = mapper.createArrayNode();
        resultsArray.add(transaction1);
        resultsArray.add(transaction2);

        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.set("transactions", resultsArray);
        return rootNode;
    }
}
