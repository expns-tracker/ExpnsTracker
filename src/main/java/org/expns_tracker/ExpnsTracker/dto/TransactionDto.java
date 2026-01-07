package org.expns_tracker.ExpnsTracker.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.expns_tracker.ExpnsTracker.entity.enums.TransactionType;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {
    private String id;

    private String userId;

    private TransactionType type;

    private String categoryName;

    private Double amount;

    private String description;

    private Timestamp date;
}
