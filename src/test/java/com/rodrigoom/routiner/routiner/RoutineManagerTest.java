package com.rodrigoom.routiner.routiner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodrigoom.routiner.routine.Routine;
import com.rodrigoom.routiner.routine.WriteToFileRunnable;

public class RoutineManagerTest {

    @BeforeEach
    public void beforeEach() {
        RoutineManager.cleanRoutines();
    }

    @Test
    public void routines_have_right_status_throughout_flow() throws InterruptedException {
        Runnable sleepThrough1s = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        };

        Routine routine = Routine.builder()
                .run(sleepThrough1s)
                .withAnIntervalOf(2)
                .asId(RoutineManager.generateNextId())
                .build();

        RoutineManager.addRoutine(routine);
        assertThat(routine.getStatus()).isEqualTo(Routine.RoutineStatus.ON_QUEUE);
        Thread.sleep(2000);
        assertThat(routine.getStatus()).isEqualTo(Routine.RoutineStatus.RUNNING);
        RoutineManager.delete(routine.getId().get());
        assertThat(routine.getStatus()).isEqualTo(Routine.RoutineStatus.FINISHED);
    }

    @Test
    void routines_cannot_be_added_with_same_id() {
        Routine routine = Routine.builder().run(()->{}).withAnIntervalOf(1).asId(1).build();
        Routine duplicatedRoutine = Routine.builder().run(()->{}).withAnIntervalOf(1).asId(1).build();
        RoutineManager.addRoutine(routine);
        assertThatIllegalArgumentException().isThrownBy(() -> RoutineManager.addRoutine(duplicatedRoutine));
    }

    @Test
    void routine_should_not_run_after_delete() throws InterruptedException {
        Runnable emptyRunnable = () -> {};

        Routine routine = Routine.builder()
                .run(emptyRunnable)
                .withAnIntervalOf(1)
                .asId(RoutineManager.generateNextId())
                .build();

        RoutineManager.addRoutine(routine);
        Thread.sleep(1200);
        RoutineManager.delete(routine.getId().get());
        Thread.sleep(1000);

        assertThat(routine.getExecutionsCount()).isEqualTo(1);
    }

    @Test
    void routine_should_run_despite_previous_exceptions() throws InterruptedException {
        Runnable throwsExceptionRunnable = () -> { throw new RuntimeException("ignored"); };

        Routine routine = Routine.builder()
                .run(throwsExceptionRunnable)
                .withAnIntervalOf(1)
                .asId(RoutineManager.generateNextId())
                .build();

        RoutineManager.addRoutine(routine);
        Thread.sleep(3000);
        RoutineManager.delete(routine.getId().get());

        assertThat(routine.getExecutionsCount()).isGreaterThan(1);
    }
}
