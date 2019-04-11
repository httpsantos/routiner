package com.rodrigoom.routiner.service;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rodrigoom.routiner.routine.Routine;
import com.rodrigoom.routiner.routine.WriteToFileRunnable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WriteToFileRoutineDTO extends RoutineDTO {

    @JsonProperty("mensagem")
    private String message;

    private List<ExecutionDTO> executionHistory;

    public String getMessage() {
        return message;
    }

    public WriteToFileRoutineDTO setMessage(String message) {
        this.message = message;
        return this;
    }

    public List<ExecutionDTO> getExecutionHistory() {
        return executionHistory;
    }

    public WriteToFileRoutineDTO setExecutionHistory(List<ExecutionDTO> executionHistory) {
        this.executionHistory = executionHistory;
        return this;
    }

    public static WriteToFileRoutineDTO newFromRoutine(Routine routine) {
        if (!(routine.getCommand() instanceof WriteToFileRunnable)) {
            throw new InvalidParameterException();
        }

        WriteToFileRunnable command = (WriteToFileRunnable) routine.getCommand();

        List<ExecutionDTO> executionHistory = new ArrayList<>();
        synchronized (routine.getExecutionHistory()) {
            routine.getExecutionHistory().forEach((executionTimestamp) -> {
                executionHistory.add(new ExecutionDTO(executionTimestamp));
            });
        }

        WriteToFileRoutineDTO routineVO = new WriteToFileRoutineDTO();
        routineVO.setExecutionHistory(executionHistory)
                .setMessage(command.getMessage())
                .setId(routine.getId().get())
                .setIntervalInSeconds(routine.getIntervalInMillis() / 1000L)
                .setCommand(command.getCommand())
                .setStatus(routine.getStatus().name());

        return routineVO;
    }

    //Sets executionHistory to null for listing which doesnt require executionHistory to be sent
    public static WriteToFileRoutineDTO newSimpleFromRoutine(Routine routine) {
        return newFromRoutine(routine).setExecutionHistory(null);
    }
}
