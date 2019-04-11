package com.rodrigoom.routiner.service;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jetty.http.HttpStatus;

import com.rodrigoom.routiner.Utils;
import com.rodrigoom.routiner.routine.CommandFactory;
import com.rodrigoom.routiner.routine.Routine;
import com.rodrigoom.routiner.routine.WriteToFileRunnable;
import com.rodrigoom.routiner.routiner.RoutineManager;

import io.javalin.Context;

public class RoutineController {

    public static void deleteRoutine(Context ctx) throws IllegalArgumentException {
        String paramId = ctx.pathParam("id");

        if (failOnInvalidId(ctx, paramId)) return;

        int id = Integer.parseInt(paramId);
        RoutineManager.delete(id);

        ctx.status(200);
    }

    public static void getRoutine(Context ctx) throws IllegalArgumentException {
        String paramId = ctx.pathParam("id");

        if (failOnInvalidId(ctx, paramId)) return;

        int id = Integer.parseInt(paramId);

        Routine routine = RoutineManager.findRoutine(id);
        ctx.json(WriteToFileRoutineDTO.newFromRoutine(routine));
    }

    public static void getRoutines(Context ctx) {
        Collection<Routine> routines = RoutineManager.listAllRoutines();

        Collection<WriteToFileRoutineDTO> routinesDTO = routines.stream()
                .map(WriteToFileRoutineDTO::newSimpleFromRoutine)
                .collect(Collectors.toList());

        ctx.json(routinesDTO);
    }

    public static void createRoutine(Context ctx) throws Exception{
        if (failOnInvalidCreationFields(ctx)) return;

        WriteToFileRoutineDTO creationInstructions = ctx.bodyAsClass(WriteToFileRoutineDTO.class);
        int id = RoutineManager.generateNextId();

        Routine routine = Routine.builder()
                .run(new WriteToFileRunnable(
                        creationInstructions.getMessage(),
                        creationInstructions.getCommand(),
                        id))
                .withAnIntervalOf(creationInstructions.getIntervalInSeconds().intValue())
                .asId(id)
                .build();

        RoutineManager.addRoutine(routine);
    }

    private static boolean failOnInvalidCreationFields(Context ctx) {
        Objects.requireNonNull(ctx.body());

        Map body = ctx.bodyAsClass(Map.class);

        String interval = body.containsKey("interval_in_seconds") ? body.get("interval_in_seconds").toString() : "";
        String command = body.containsKey("command") ? body.get("command").toString() : "";
        String message = body.containsKey("mensagem") ? body.get("mensagem").toString() : "";

        if (!Utils.isValidNumber(interval)) {
            setBadRequest(ctx, "Invalid interval provided");
            return true;
        }

        if (!CommandFactory.isAValidCommand(command)) {
            setBadRequest(ctx, "Invalid command provided");
            return true;
        }

        if (message.isEmpty()) {
            setBadRequest(ctx, "Invalid message provided - maybe it is empty?");
            return true;
        }

        return false;
    }

    private static boolean failOnInvalidId(Context ctx, String stringId) {
        if(!Utils.isValidNumber(stringId)) {
            setBadRequest(ctx, "Invalid routine id");
            return true;
        }
        return false;
    }

    private static void setBadRequest(Context ctx, String message) {
        ctx.status(HttpStatus.BAD_REQUEST_400);
        ctx.result(message);
    }
}
