package org.expns_tracker.ExpnsTracker.state;

import org.expns_tracker.ExpnsTracker.state.concrete.FailedState;
import org.expns_tracker.ExpnsTracker.state.concrete.IdleState;
import org.expns_tracker.ExpnsTracker.state.concrete.RunningState;

public class StateFactory {
    public static JobState get(String status) {
        if (status == null) return new IdleState();

        return switch (status.toUpperCase()) {
            case "RUNNING", "SYNCING", "EXPORTING" -> new RunningState();
            case "FAILED" -> new FailedState();
            default -> new IdleState();
        };
    }
}