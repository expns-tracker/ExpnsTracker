package org.expns_tracker.ExpnsTracker.state.concrete;

import lombok.extern.slf4j.Slf4j;
import org.expns_tracker.ExpnsTracker.state.JobContext;
import org.expns_tracker.ExpnsTracker.state.JobState;

@Slf4j
public class FailedState implements JobState {
    @Override
    public void start(JobContext context, Runnable taskToRun) {
        log.info("Retrying {} job from FAILED state.", context.getJobType());
        taskToRun.run();
        context.setState(new RunningState());
    }

    @Override
    public void finish(JobContext context) { log.warn("Cannot finish a job that is FAILED."); }

    @Override
    public void fail(JobContext context, String error) { log.warn("Job already failed"); }

    @Override
    public String getStatusName() { return "FAILED"; }
}