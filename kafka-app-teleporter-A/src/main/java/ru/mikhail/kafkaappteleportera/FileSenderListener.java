package ru.mikhail.kafkaappteleportera;

import commons.FileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.filewatch.ChangedFile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.mikhail.kafkaappteleportera.logging.EventLogger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@PropertySource("classpath:application.properties")
public class FileSenderListener {
    @Value("${teleporter.monitoring-folder}")
    private String monitoringFolderPath;
    @Autowired
    @Qualifier("fileEventLogger")
    EventLogger logger;
    private int sentCount;
    private int totalCount;

    public FileSenderListener() {
        sentCount = 0;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public void logFilesAdded(List<ChangedFile> addedFiles) {
        logger.info(String.format("Found %d files in folder: {%s}", addedFiles.size(), addedFiles.stream().map(ChangedFile::getRelativeName).collect(Collectors.joining(","))));
    }

    public void successSend(SendResult<Long, FileDTO> sendResult) {
        sentCount++;
        File fileToDelete = new File(monitoringFolderPath + sendResult.getProducerRecord().value().getName());
        String fileName = fileToDelete.getName();
        fileToDelete.delete();
        logger.info(String.format("File [%s] sent successfully and deleted locally. %d of %d files are sent", fileName, sentCount, totalCount));
    }

    public void failSend(Throwable error) {
        logger.info(String.format("File can not be sent due to error: [%s].", error.getMessage()));
    }

    public void logError(String error) {
        logger.error(error);
    }

    public void logInfo(String message) {
        logger.info(message);
    }
}