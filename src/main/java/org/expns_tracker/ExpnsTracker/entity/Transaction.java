package org.expns_tracker.ExpnsTracker.entity;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;
import org.expns_tracker.ExpnsTracker.entity.enums.TransactionType;


@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction extends Auditable {

    @DocumentId
    private String id;

    private String userId;

    private TransactionType type;

    private String categoryId;

    private Double amount;

    private String description;

    private Timestamp date;
}
