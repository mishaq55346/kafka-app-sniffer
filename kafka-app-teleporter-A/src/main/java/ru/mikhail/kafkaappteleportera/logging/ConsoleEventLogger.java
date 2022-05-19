package ru.mikhail.kafkaappteleportera.logging;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class ConsoleEventLogger implements EventLogger {
    @Override
    public void info(String message) {
        log.info(message);
    }

    @Override
    public void error(String error) {
        log.error(error);
    }
}
