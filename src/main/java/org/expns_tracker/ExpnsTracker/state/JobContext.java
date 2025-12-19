package org.expns_tracker.ExpnsTracker.state;

import lombok.Getter;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.service.UserService;
import org.expns_tracker.ExpnsTracker.state.enums.JobType;

@Getter
public class JobContext {
    private JobState currentState;
    private final String userId;
    private final UserService userService;
    private final JobType jobType;

    public JobContext(User user, UserService userService, JobType jobType) {
        this.userId = user.getId();
        this.userService = userService;
        this.jobType = jobType;

        String statusString = (jobType == JobType.SYNC)
                ? user.getSyncStatus()
                : user.getExportStatus();

        this.currentState = StateFactory.get(statusString);
    }

    public void setState(JobState newState) {
        this.currentState = newState;
        User user = userService.getUser(this.userId);
        if (jobType == JobType.SYNC) {
            user.setSyncStatus(newState.getStatusName());
        } else {
            user.setExportStatus(newState.getStatusName());
        }

        userService.save(user);
    }

    public void requestStart(Runnable taskToRun) { currentState.start(this, taskToRun); }
    public void signalSuccess() { currentState.finish(this); }
    public void signalFailure(String error) { currentState.fail(this, error); }
}