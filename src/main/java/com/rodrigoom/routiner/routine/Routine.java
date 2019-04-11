package com.rodrigoom.routiner.routine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodrigoom.routiner.Utils;

public class Routine {

    public enum RoutineStatus {
        ON_QUEUE, RUNNING, FINISHED
    }

    private final AtomicInteger id;
    private final Runnable command;

    private RoutineStatus status;
    private Long intervalInMillis;
    private Long timeLastExecutionEnded;
    private volatile int executionsCount;
    private Long timeLastExecutionStarted;
    private ArrayList<Long> executionHistory;
    private volatile long nextExecutionTime;

    private static final Logger logger = LoggerFactory.getLogger(Routine.class);

    public Routine(Builder builder) {
        this.id = builder.id;
        this.command = builder.command;
        this.intervalInMillis = builder.interval;

        this.executionsCount = 0;
        this.nextExecutionTime = 0;
        this.timeLastExecutionEnded = 0L;
        this.timeLastExecutionStarted = 0L;
        this.status = RoutineStatus.ON_QUEUE;
        this.executionHistory = new ArrayList<>();
    }

    public RoutineStatus getStatus() {
        return status;
    }

    public Long getIntervalInMillis() {
        return intervalInMillis;
    }

    public long getNextExecutionTime() {
        return nextExecutionTime;
    }

    public int getExecutionsCount() {
        return executionsCount;
    }

    public Long getTimeLastExecutionStarted() {
        return timeLastExecutionStarted;
    }

    public Long getTimeLastExecutionEnded() {
        return timeLastExecutionEnded;
    }

    public AtomicInteger getId() {
        return id;
    }

    public ArrayList<Long> getExecutionHistory() {
        return executionHistory;
    }

    public Runnable getCommand() {
        return command;
    }

    public void setStatus(RoutineStatus status) {
        this.status = status;
    }

    public void setNextExecutionTime(long nextExecutionTime) {
        this.nextExecutionTime = nextExecutionTime;
    }

    void setExecutionsCount(int executionsCount) {
        this.executionsCount = executionsCount;
    }

    void setTimeLastExecutionStarted(Long timeLastExecutionStarted) {
        this.timeLastExecutionStarted = timeLastExecutionStarted;
    }

    void setTimeLastExecutionEnded(Long timeLastExecutionEnded) {
        this.timeLastExecutionEnded = timeLastExecutionEnded;
    }

    public Routine setIntervalInMillis(Long intervalInMillis) {
        this.intervalInMillis = intervalInMillis;
        return this;
    }

    public void calculateNextExecutionTime(long currentTime) {
        nextExecutionTime = currentTime + intervalInMillis;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "Routine " + id +
                " is currently [" + status + "]" +
                " and will run in "+ Instant.ofEpochMilli(nextExecutionTime);
    }

    public void execute() {
        timeLastExecutionStarted = Utils.getCurrentTime();

        executionHistory.add(timeLastExecutionStarted);

        try {
            command.run();
        } catch (Throwable t) {
            logger.error("Error while running routine {}", id, t);
        }

        executionsCount++;

        timeLastExecutionEnded = Utils.getCurrentTime();

        logger.info(
                "Routine '{}' executed in {}ms", id,
                timeLastExecutionEnded - timeLastExecutionStarted
        );
    }

    public static class Builder {

        private AtomicInteger id;
        private Long interval;
        private Runnable command;

        public Builder run(Runnable command) {
            this.command = command;
            return this;
        }

        public Builder withAnIntervalOf(Integer interval) {
            this.interval = (long) (interval * 1000);
            return this;
        }

        public Builder asId(int id) {
            this.id = new AtomicInteger(id);
            return this;
        }

        public Routine build() {
            return new Routine(this);
        }

    }
}
