package org.expns_tracker.ExpnsTracker.state;

public interface JobState {
    void start(JobContext context, Runnable taskToRun);
    void finish(JobContext context);
    void fail(JobContext context, String error);

    String getStatusName();
}