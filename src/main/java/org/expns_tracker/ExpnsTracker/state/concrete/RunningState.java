package org.expns_tracker.ExpnsTracker.state.concrete;

import lombok.extern.slf4j.Slf4j;
import org.expns_tracker.ExpnsTracker.state.JobContext;
import org.expns_tracker.ExpnsTracker.state.JobState;

@Slf4j
public class RunningState implements JobState {
    @Override
    public void start(JobContext context, Runnable taskToRun) {
        throw new IllegalStateException(context.getJobType() + " is already running for this user.");
    }

    @Override
    public void finish(JobContext context) {
        log.info("{} job finished successfully.", context.getJobType());
        context.setState(new IdleState());
    }

    @Override
    public void fail(JobContext context, String error) {
        log.error("{} job failed: {}", context.getJobType(), error);
        context.setState(new FailedState());
    }

    @Override
    public String getStatusName() { return "RUNNING"; }
}