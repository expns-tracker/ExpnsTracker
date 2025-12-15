package org.expns_tracker.ExpnsTracker.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Category {
    @DocumentId
    private String categoryId;
    private String categoryName;
}
