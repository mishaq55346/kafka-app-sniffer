package ru.mikhail.kafkaappteleportera.logging;

public interface EventLogger {
    void info(String message);

    void error(String error);
}
