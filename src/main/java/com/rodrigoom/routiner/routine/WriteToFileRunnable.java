package com.rodrigoom.routiner.routine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodrigoom.routiner.Utils;
import com.rodrigoom.routiner.routiner.RoutineManager;

public class WriteToFileRunnable implements Runnable {

    private static final String PATH = "/tmp/routiner-data/";

    private Integer id;
    private String message;
    private String command;
    private String file;

    private static final Logger logger = LoggerFactory.getLogger(WriteToFileRunnable.class);

    static {
        Path parentPath = Paths.get(PATH);
        if (!Files.exists(parentPath)) {
            try {
                Files.createDirectories(parentPath);
            } catch (IOException e) {
                logger.error("Error creating directory to write files", e);
            }
        }
    }

    public String getMessage() {
        return message;
    }

    public String getCommand() {
        return command;
    }

    public WriteToFileRunnable(String message, String command, int id) {
        this.message = message;
        this.command = command;
        this.id = id;

        String fileName;

        switch (command) {
            case "write_to_file_one":
                fileName = "one.txt";
                break;
            case "write_to_file_two":
                fileName = "two.txt";
                break;
            default:
                throw new IllegalArgumentException();
        }

        this.file = PATH + fileName;
    }

    @Override
    public void run() {
        try {
            String text = message
                    + " - Executada pela rotina "+ id
                    +" em " + Utils.formatDate(LocalDateTime.now())
                    +"\n";

            Files.write(Paths.get(file), text.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.error("IO Error writing to file "+file+"", e);
        }
    }
}
