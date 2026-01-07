package org.expns_tracker.ExpnsTracker.service;

import com.google.cloud.Timestamp;
import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.entity.Feedback;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.entity.enums.FeedbackCategory;
import org.expns_tracker.ExpnsTracker.entity.enums.FeedbackStatus;
import org.expns_tracker.ExpnsTracker.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public void submitFeedback(User user, String category, String message) {
        Feedback feedback = Feedback.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .category(FeedbackCategory.valueOf(category.toUpperCase()))
                .message(message)
                .status(FeedbackStatus.OPEN)
                .build();

        feedbackRepository.save(feedback);
    }
}