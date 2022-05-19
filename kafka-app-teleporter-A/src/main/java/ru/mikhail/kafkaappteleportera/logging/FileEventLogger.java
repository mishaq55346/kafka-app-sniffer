package ru.mikhail.kafkaappteleportera.logging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@PropertySource("classpath:application.properties")
public class FileEventLogger implements EventLogger{
    @Value("${logger.file.path}")
    String file;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SS");
    @Override
    public void info(String message) {
        write(String.format("%s INFO : %s", dateFormat.format(new Date()), message));
    }

    @Override
    public void error(String error) {
        write(String.format("%s ERROR : %s", dateFormat.format(new Date()), error));
    }

    private void write(String message){
        File loggingFile = new File(this.file);
        OpenOption openOption = StandardOpenOption.APPEND;
        try {
            Files.writeString(loggingFile.toPath(),
                    message, openOption);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
