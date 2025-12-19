package org.expns_tracker.ExpnsTracker.state.concrete;

import lombok.extern.slf4j.Slf4j;
import org.expns_tracker.ExpnsTracker.state.JobContext;
import org.expns_tracker.ExpnsTracker.state.JobState;

@Slf4j
public class IdleState implements JobState {
    @Override
    public void start(JobContext context, Runnable taskToRun) {
        log.info("Starting {} job for user {}", context.getJobType(), context.getUserId());
        taskToRun.run();
        context.setState(new RunningState());
    }

    @Override
    public void finish(JobContext context) {
        log.warn("Cannot finish a job that is IDLE.");
    }

    @Override
    public void fail(JobContext context, String error) {
        log.warn("Cannot fail a job that is IDLE.");
    }

    @Override
    public String getStatusName() { return "IDLE"; }
}