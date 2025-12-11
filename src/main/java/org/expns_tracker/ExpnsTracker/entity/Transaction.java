package org.expns_tracker.ExpnsTracker.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;
import org.expns_tracker.ExpnsTracker.entity.enums.TransactionType;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction extends Auditable {

    @DocumentId
    private String id;

    private String userId;

    private TransactionType type; // income or expense

    private String category;

    private Double amount;

    private String description;

    private LocalDateTime date;
}
