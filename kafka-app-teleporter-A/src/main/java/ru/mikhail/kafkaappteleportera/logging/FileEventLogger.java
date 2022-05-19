package ru.mikhail.kafkaappteleportera.logging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@PropertySource("classpath:application.properties")
public class FileEventLogger implements EventLogger{
    @Value("${logger.file.path}")
    private String filePath;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SS");
    private File loggingFile;
    @PostConstruct
    public void init() throws IOException {
        loggingFile = new File(filePath);
        if (!loggingFile.exists()){
            loggingFile.createNewFile();
        }
    }
    @Override
    public void info(String message) {
        write(String.format("%s INFO %d : %s\n",
                dateFormat.format(new Date()), ProcessHandle.current().pid(), message));
    }

    @Override
    public void error(String error) {
        write(String.format("%s ERROR : %s\n", dateFormat.format(new Date()), error));
    }

    private void write(String message){

        try {
            Files.writeString(loggingFile.toPath(),
                    message, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
