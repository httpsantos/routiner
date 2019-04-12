package com.rodrigoom.routiner.routiner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodrigoom.routiner.Utils;
import com.rodrigoom.routiner.routine.Routine;

public class RoutineManager {

    private static int idCounter = 0;
    private static final Map<Integer, Routine> routinesById;
    private static final ThreadPoolExecutor threadPoolExecutor;
    private static final AtomicBoolean queueKeepWaitingSemaphore;
    private static final ArrayList<Routine> routineExecutionsQueue;

    private static final Logger logger = LoggerFactory.getLogger(RoutineManager.class);

    static {
        routinesById = new ConcurrentHashMap<>();
        routineExecutionsQueue = new ArrayList<>();
        queueKeepWaitingSemaphore = new AtomicBoolean(true);
        threadPoolExecutor = new ThreadPoolExecutor(0, 2147483647, 60L, TimeUnit.SECONDS, new SynchronousQueue());

        Thread queueExecutorThread = new Thread(RoutineManager::queueExecutor);
        queueExecutorThread.start();
    }

    public static void addRoutine(Routine newRoutine) {
        Objects.requireNonNull(newRoutine, "Routine must not be null");
        Objects.requireNonNull(newRoutine.getCommand(), "Command must not be null");

        if (isDuplicatedId(newRoutine.getId().get()))
            throw new IllegalArgumentException("Duplicated id");

        synchronized (routinesById) {
            routinesById.put(newRoutine.getId().get(), newRoutine);
            idCounter=newRoutine.getId().get();
        }

        logger.info(
                "Creating new routine: id:'{}' - interval: {} seconds",
                newRoutine.getId(),
                (newRoutine.getIntervalInMillis() / 1000));

        reorderQueueWith(newRoutine);
    }

    private static boolean isDuplicatedId(int id) {
        return routinesById.containsKey(id);
    }

    private static synchronized void reorderQueueWith(Routine routine) {
        routine.calculateNextExecutionTime(Utils.getCurrentTime());

        if (routine.getStatus() != Routine.RoutineStatus.FINISHED) {

            routine.setStatus(Routine.RoutineStatus.ON_QUEUE);
            routineExecutionsQueue.add(routine);
            routineExecutionsQueue.sort(
                    Comparator.comparing(Routine::getNextExecutionTime)
            );

            synchronized (queueKeepWaitingSemaphore) {
                queueKeepWaitingSemaphore.set(false);
                queueKeepWaitingSemaphore.notify();
            }
        } else {
            logger.info("Routine '{}' has been finished previously", routine.getId());
        }
    }

    private static void queueExecutor() {
        while (true) {
            Long timeUntilNextOnQueue = findTimeUntilNextOnQueue();

            if (timeUntilNextOnQueue == null || timeUntilNextOnQueue > 0L) {
                /* Some logics are sometimes a little "spaghetti" to understand, so let's go through it
                When the semaphore is true, that means nothing new happened to the queue and the thread can wait
                until the next execution. When the semaphore is false, it means something new happened to the queue,
                and the thread should check it. Adds and deletes are the ones setting the semaphore to false when
                needed, and they also use "notify" to wake up the thread if its currently waiting.

                This flow only matters while the timeUntilNextOnQueue is greater than zero or there is none, because
                if timeUntilNextOnQueue is lesser than zero, it means there is a routine that needs to be executed,
                so it executes it and repeats the above flow.*/
                synchronized (queueKeepWaitingSemaphore) {
                    if (queueKeepWaitingSemaphore.get()) {
                        try {
                            if (timeUntilNextOnQueue == null) {
                                queueKeepWaitingSemaphore.wait();
                            } else {
                                queueKeepWaitingSemaphore.wait(timeUntilNextOnQueue);
                            }
                        } catch (InterruptedException e) {
                            logger.error("There was an interruption on queue executor", e);
                        }
                    }
                    queueKeepWaitingSemaphore.set(true);
                }
            } else {
                synchronized (Routine.class) {
                    if (!routineExecutionsQueue.isEmpty()) {
                        Routine nextRoutineToRun = routineExecutionsQueue.remove(0);
                        nextRoutineToRun.setStatus(Routine.RoutineStatus.RUNNING);
                        threadPoolExecutor.execute(() -> executeRoutine(nextRoutineToRun));
                    }
                }
            }
        }
    }

    private static Long findTimeUntilNextOnQueue() {
        Long timeUntilNextOnQueue = null;
        synchronized (Routine.class) {
            if (routineExecutionsQueue.size() > 0) {
                timeUntilNextOnQueue = routineExecutionsQueue
                        .get(0)
                        .getNextExecutionTime() - Utils.getCurrentTime();
            }
        }
        return timeUntilNextOnQueue;
    }

    private static void executeRoutine(Routine routine) {
        routine.execute();
        synchronized (Routine.class) {
            reorderQueueWith(routine);
        }
    }

    public static boolean delete(int routineId) throws IllegalArgumentException {
        Routine routine = findRoutine(routineId);

        synchronized (RoutineManager.class) {
            if (routine.getStatus() == Routine.RoutineStatus.FINISHED) {
                return true;
            }

            routine.setStatus(Routine.RoutineStatus.FINISHED);

            routineExecutionsQueue.removeIf(nextRoutine -> nextRoutine == routine);

            reorderQueueWith(routine);

            return true;
        }
    }

    public static Routine findRoutine(int routineId) {
        synchronized (routinesById) {
            if (!routinesById.containsKey(routineId)) {
                throw new IllegalArgumentException("Non-existent id provided");
            }
            return routinesById.get(routineId);
        }
    }

    public static Collection<Routine> listAllRoutines() {
        return routinesById.values();
    }

    public static void deleteAll() throws IllegalArgumentException {
        routinesById.keySet().forEach((id) -> {
            delete(id);
        });
    }

    public static void cleanRoutines() {
        deleteAll();
        routinesById.clear();
    }

    public static int generateNextId() {
        return idCounter+1;
    }
}
