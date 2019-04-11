package com.rodrigoom.routiner.routine;

public class CommandFactory {

    public enum Commands {
        write_to_file_one, write_to_file_two;
    }

    public static boolean isAValidCommand(String command) {
        try {
            Commands.valueOf(command);
            return true;
        } catch (IllegalArgumentException ignored) {}

        return false;
    }


}
