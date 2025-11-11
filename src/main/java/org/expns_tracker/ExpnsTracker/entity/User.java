package org.expns_tracker.ExpnsTracker.entity;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.expns_tracker.ExpnsTracker.entity.enums.Currency;
import org.expns_tracker.ExpnsTracker.entity.enums.Role;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends Auditable{

    @DocumentId
    private String id;

    private String email;
    private String name;
    private String passwordHash;

    private Role role;
    private Currency currency;

    private Double monthlyBudgetLimit;
    private Double currentMonthExpenses;

    private Boolean isActive = true;
    private Boolean isVerified = false;

    private List<String> feedbackIds;
}
