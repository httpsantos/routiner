package com.rodrigoom.routiner.service;

import static io.restassured.RestAssured.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodrigoom.routiner.App;
import com.rodrigoom.routiner.routine.Routine;
import com.rodrigoom.routiner.routine.WriteToFileRunnable;
import com.rodrigoom.routiner.routiner.RoutineManager;

import io.javalin.Javalin;
import io.restassured.RestAssured;

public class RoutineServiceTest {

    private static Javalin app;

    @BeforeAll
    public static void setUpApp() {
        app = App.init();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7000;
    }

    @BeforeEach
    public void beforeEach() {
        RoutineManager.cleanRoutines();
    }

    @AfterAll
    public static void stopApp() {
        app.stop();
    }

    @Test
    public void should_return_empty_list_of_routines() {
        get("/routines").then().statusCode(200).assertThat()
                .body("data", empty());
    }

    @Test
    public void should_return_only_one_routine() {
        Routine routine = Routine.builder().run(newMockRunnable()).withAnIntervalOf(10).asId(1).build();
        RoutineManager.addRoutine(routine);
        get("/routines").then().statusCode(200).assertThat()
                .body("data", hasSize(1));
    }

    @Test
    public void should_return_routine_correctly() {
        Routine routine = Routine.builder().run(newMockRunnable()).withAnIntervalOf(1).asId(1).build();
        RoutineManager.addRoutine(routine);
        get("/routines/1").then().statusCode(200).assertThat()
                .body("", hasKey("mensagem"))
                .body("", hasKey("id"))
                .body("", hasKey("command"))
                .body("", hasKey("status"))
                .body("", hasKey("executionHistory"))
                .body("", hasKey("interval_in_seconds"));
    }

    public static MockWriteToFileRunnable newMockRunnable() {
        return new MockWriteToFileRunnable("mock","write_to_file_one",1);
    }

    public static class MockWriteToFileRunnable extends WriteToFileRunnable {
        public MockWriteToFileRunnable(String message, String command, int id) {
            super(message, command, id);
        }

        @Override
        public void run() {
        }
    }
}
