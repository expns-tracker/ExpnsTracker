package org.expns_tracker.ExpnsTracker.entity;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.*;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Auditable {
    @ServerTimestamp
    private Timestamp createdAt;
    @ServerTimestamp
    private Timestamp updatedAt;
}
