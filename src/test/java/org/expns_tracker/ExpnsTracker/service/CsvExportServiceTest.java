package org.expns_tracker.ExpnsTracker.service;

import com.google.cloud.Timestamp;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.expns_tracker.ExpnsTracker.config.ApplicationProperties;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvExportServiceTest {

    @InjectMocks
    private CsvExportService csvExportService;

    @Mock
    private UserService userService;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private LockProvider lockProvider;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private SimpleLock simpleLock;

    private User user;
    private final String userId = "user-123";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("Test");
    }

    @Test
    void triggerExport_Success(@TempDir Path tempDir) throws InterruptedException, ExecutionException {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        when(userService.getUser(userId)).thenReturn(user);
        when(lockProvider.lock(any(LockConfiguration.class))).thenReturn(Optional.of(simpleLock));
        when(applicationProperties.getExportDir()).thenReturn(tempDir.toString());

        Transaction t1 = new Transaction();
        t1.setAmount(100.0);
        t1.setCategoryId("Food");
        t1.setDescription("Lunch");
        t1.setDate(Timestamp.now());

        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(Timestamp.class), any(Timestamp.class)))
                .thenReturn(List.of(t1));

        csvExportService.triggerExport(userId, startDate, endDate);

        Thread.sleep(1000);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, atLeastOnce()).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser.getLastExportContent(), "Export content path should be set");
        assertTrue(savedUser.getLastExportContent().endsWith(".csv"));

        Path savedFile = Path.of(savedUser.getLastExportContent());
        assertTrue(Files.exists(savedFile), "The CSV file should be created on disk");

        verify(transactionRepository).findByUserIdAndDateBetween(eq(userId), any(), any());
        verify(simpleLock).unlock();
    }

    @Test
    void triggerExport_LockBusy() {

        when(userService.getUser(userId)).thenReturn(user);
        when(lockProvider.lock(any(LockConfiguration.class))).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                csvExportService.triggerExport(userId, LocalDate.now(), LocalDate.now())
        );

        assertEquals("System busy. Please try again in a moment.", ex.getMessage());

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void triggerExport_BackgroundFailure(@TempDir Path tempDir) throws InterruptedException, ExecutionException {

        when(userService.getUser(userId)).thenReturn(user);
        when(lockProvider.lock(any(LockConfiguration.class))).thenReturn(Optional.of(simpleLock));
        when(applicationProperties.getExportDir()).thenReturn(tempDir.toString());

        when(transactionRepository.findByUserIdAndDateBetween(any(), any(), any()))
                .thenThrow(new RuntimeException("DB Failed"));

        csvExportService.triggerExport(userId, LocalDate.now(), LocalDate.now());

        Thread.sleep(500);

        verify(userService, atLeastOnce()).save(any(User.class));

        verify(simpleLock).unlock();
    }

    @Test
    void getLastExportResource_FileExists(@TempDir Path tempDir) throws IOException {
        Path dummyFile = tempDir.resolve("export.csv");
        Files.writeString(dummyFile, "Date,Amount\n2023-01-01,100");

        user.setLastExportContent(dummyFile.toString());
        when(userService.getUser(userId)).thenReturn(user);

        Resource resource = csvExportService.getLastExportResource(userId);

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    void getLastExportResource_FileMissing() {
        user.setLastExportContent("/invalid/path/missing.csv");
        when(userService.getUser(userId)).thenReturn(user);

        Resource resource = csvExportService.getLastExportResource(userId);

        assertNull(resource);
    }
}