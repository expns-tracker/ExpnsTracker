package org.expns_tracker.ExpnsTracker.entity;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;
import org.expns_tracker.ExpnsTracker.entity.enums.FeedbackCategory;
import org.expns_tracker.ExpnsTracker.entity.enums.FeedbackStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Feedback extends Auditable{
    @DocumentId
    private String id;

    private String userId;
    private String userEmail;

    private FeedbackCategory category;
    private String message;

    private FeedbackStatus status;
}