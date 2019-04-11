package com.rodrigoom.routiner;

import static io.javalin.apibuilder.ApiBuilder.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodrigoom.routiner.service.RoutineController;

import io.javalin.Javalin;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        init();
    }

    public static Javalin init() {
        Javalin app = Javalin.create().start(7000);

        //Mapping endpoints
        app.routes( () -> {
            path("routines", () -> {
                post(RoutineController::createRoutine);
                get(RoutineController::getRoutines);
                path(":id", () -> {
                    get(RoutineController::getRoutine);
                    delete(RoutineController::deleteRoutine);
                });
            });
        });

        app.get("/", ctx -> ctx.result("Hello! Welcome to Routiner!"));

        app.get("/health", ctx -> ctx.status(200));

        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            logger.error("Illegal Argument in request {} with body {}", ctx.url(), ctx.body());
            ctx.status(409);
            ctx.result(e.getMessage());
        });

        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Error on a request to {}", ctx.url(),e);
            ctx.status(500);
            ctx.result("An unknown error has ocurred. Please contact rodrigoom through his github page");
        });

        return app;
    }
}
