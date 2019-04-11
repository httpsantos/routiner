package com.rodrigoom.routiner.routine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

public class RoutineTest {

    @Test
    void should_calculate_next_execution_correctly() {
        Routine routine = Routine.builder().run(()->{}).withAnIntervalOf(5).asId(1).build();
        long currentTime = System.currentTimeMillis();
        long expected = currentTime + 5000;
        routine.calculateNextExecutionTime(currentTime);
        assertThat(routine.getNextExecutionTime()).isCloseTo(expected,within(1L));
    }

    @Test
    void should_update_executions_history_with_every_execution() {
        Routine routine = Routine.builder().run(()->{}).withAnIntervalOf(0).asId(1).build();
        assertThat(routine.getExecutionHistory().size()).isZero();
        routine.execute();
        assertThat(routine.getExecutionHistory().size()).isEqualTo(1);
        routine.execute();
        assertThat(routine.getExecutionHistory().size()).isEqualTo(2);
    }

    @Test
    void should_save_interval_in_millis() {
        int interval = 1;
        long intervalInMillis = interval * 1000;

        Routine routine = Routine.builder().run(()->{})
                .withAnIntervalOf(interval)
                .asId(1).build();

        assertThat(routine.getIntervalInMillis()).isEqualTo(intervalInMillis);
    }
}
