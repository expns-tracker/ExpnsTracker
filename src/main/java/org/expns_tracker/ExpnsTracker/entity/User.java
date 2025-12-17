package org.expns_tracker.ExpnsTracker.entity;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.IgnoreExtraProperties;
import lombok.*;
import org.expns_tracker.ExpnsTracker.entity.enums.Currency;
import org.expns_tracker.ExpnsTracker.entity.enums.Role;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@IgnoreExtraProperties
public class User extends Auditable{

    @DocumentId
    private String id;

    private String email;
    private String firstName;
    private String lastName;

    private Role role;
    private Currency currency;

    private Double monthlyBudgetLimit;
    private Double currentMonthExpenses;

    private Boolean isActive = true;
    private Boolean isVerified = false;

    private List<String> feedbackIds;

    private String tinkUserId;
    private Boolean profileCompleted;

    private String syncStatus;
    private String exportStatus;
    private String lastExportContent;
    private Timestamp lastExportTime;

    @Exclude
    public String getName() {
        return firstName + " " + lastName;
    }
}

