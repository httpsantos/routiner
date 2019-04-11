package com.rodrigoom.routiner.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoutineDTO {

    public int id;

    @JsonProperty("interval_in_seconds")
    public Long intervalInSeconds;

    public String command;

    public String status;

    public String getStatus() {
        return status;
    }

    public RoutineDTO setStatus(String status) {
        this.status = status;
        return this;
    }

    public int getId() {
        return id;
    }

    public RoutineDTO setId(int id) {
        this.id = id;
        return this;
    }

    public Long getIntervalInSeconds() {
        return intervalInSeconds;
    }

    public RoutineDTO setIntervalInSeconds(Long intervalInSeconds) {
        this.intervalInSeconds = intervalInSeconds;
        return this;
    }

    public String getCommand() {
        return command;
    }

    public RoutineDTO setCommand(String command) {
        this.command = command;
        return this;
    }
}
