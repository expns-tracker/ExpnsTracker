package org.expns_tracker.ExpnsTracker.entity;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.expns_tracker.ExpnsTracker.entity.enums.Currency;
import org.expns_tracker.ExpnsTracker.entity.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @DocumentId
    private String id;

    private String email;
    private String name;
    private String passwordHash;

    private Role role;
    private Currency currency;

    private Double monthlyBudgetLimit;
    private Double currentMonthExpenses;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean isActive = true;
    private Boolean isVerified = false;

    private List<String> feedbackIds;
}
