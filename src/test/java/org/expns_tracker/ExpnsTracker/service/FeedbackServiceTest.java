package org.expns_tracker.ExpnsTracker.service;

import org.expns_tracker.ExpnsTracker.entity.Feedback;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.entity.enums.FeedbackCategory;
import org.expns_tracker.ExpnsTracker.entity.enums.FeedbackStatus;
import org.expns_tracker.ExpnsTracker.repository.FeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("user-1")
                .email("test@test.com")
                .build();
    }

    @Test
    @DisplayName("submitFeedback: Should create and save feedback with correct data")
    void submitFeedback_Success() {
        String categoryInput = "bug";
        String message = "Something is broken";

        feedbackService.submitFeedback(user, categoryInput, message);

        ArgumentCaptor<Feedback> feedbackCaptor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackRepository).save(feedbackCaptor.capture());

        Feedback savedFeedback = feedbackCaptor.getValue();
        assertEquals("user-1", savedFeedback.getUserId());
        assertEquals("test@test.com", savedFeedback.getUserEmail());
        assertEquals(FeedbackCategory.BUG, savedFeedback.getCategory()); // Assumes BUG is a valid Enum value
        assertEquals(message, savedFeedback.getMessage());
        assertEquals(FeedbackStatus.OPEN, savedFeedback.getStatus());
    }

    @Test
    @DisplayName("submitFeedback: Should throw Exception for invalid category string")
    void submitFeedback_InvalidCategory() {
        String invalidCategory = "invalid_category";

        assertThrows(IllegalArgumentException.class, () ->
                feedbackService.submitFeedback(user, invalidCategory, "msg")
        );

        verify(feedbackRepository, never()).save(any());
    }

    @Test
    @DisplayName("markAsResolved: Should update status to RESOLVED if found")
    void markAsResolved_Success() {
        String feedbackId = "fb-1";
        Feedback feedback = new Feedback();
        feedback.setId(feedbackId);
        feedback.setStatus(FeedbackStatus.OPEN);

        when(feedbackRepository.findById(feedbackId)).thenReturn(feedback);

        feedbackService.markAsResolved(feedbackId);

        assertEquals(FeedbackStatus.RESOLVED, feedback.getStatus());
        verify(feedbackRepository).save(feedback);
    }

    @Test
    @DisplayName("markAsResolved: Should do nothing if feedback not found")
    void markAsResolved_NotFound() {
        when(feedbackRepository.findById("non-existent-id")).thenReturn(null);

        feedbackService.markAsResolved("non-existent-id");

        verify(feedbackRepository, never()).save(any());
    }

    @Test
    @DisplayName("getFeedbacksPage: Should delegate to repository")
    void getFeedbacksPage_Success() {
        List<Feedback> mockList = List.of(new Feedback());
        when(feedbackRepository.findAllPaginated(0, 10, "OPEN", "BUG"))
                .thenReturn(mockList);

        List<Feedback> result = feedbackService.getFeedbacksPage(0, 10, "OPEN", "BUG");

        assertEquals(1, result.size());
        verify(feedbackRepository).findAllPaginated(0, 10, "OPEN", "BUG");
    }

    @Test
    @DisplayName("getTotalCount: Should delegate to repository")
    void getTotalCount_Success() {
        when(feedbackRepository.count("OPEN")).thenReturn(5L);

        long count = feedbackService.getTotalCount("OPEN");

        assertEquals(5L, count);
        verify(feedbackRepository).count("OPEN");
    }
}