package ru.mikhail.kafkaappteleportera;

import commons.FileDTO;
import lombok.extern.log4j.Log4j2;
import org.apache.catalina.manager.StatusManagerServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.filewatch.ChangedFile;
import org.springframework.boot.devtools.filewatch.ChangedFiles;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Component
@Log4j2
@PropertySource("classpath:application.properties")
public class FileSender {
    @Value("${teleporter.monitoring-folder}")
    private String monitoringFolderPath;
    private final FileSenderListener fileSenderListener;
    private final KafkaSender kafkaSender;

    public FileSender(FileSenderListener fileSenderListener, KafkaSender kafkaSender) {
        this.fileSenderListener = fileSenderListener;
        this.kafkaSender = kafkaSender;
    }


    @PostConstruct
    private void runScan() {
        if (!monitoringFolderPath.endsWith("/")) {
            monitoringFolderPath += "/";
        }
        File monitoringFolder = new File(monitoringFolderPath);
        if (monitoringFolder.isFile()){
            log.error("This path is a file. Change to folder");
            System.exit(-1);
        }
        if (!monitoringFolder.exists()) {
            log.error("No directory found. Creating new one");
            monitoringFolder.mkdir();
        }
        FileSystemWatcher fileSystemWatcher = new FileSystemWatcher();
        fileSystemWatcher.addSourceDirectory(monitoringFolder);
        fileSystemWatcher.addListener(changeSet -> {
            for (ChangedFiles changedFiles : changeSet) {
                List<ChangedFile> addedFiles = changedFiles.getFiles()
                        .stream()
                        .filter(f -> f.getType() == ChangedFile.Type.ADD)
                        .toList();
                if (addedFiles.isEmpty()) {
                    continue;
                }
                fileSenderListener.setTotalCount(addedFiles.size());
                fileSenderListener.logFilesAdded(addedFiles);
                for (ChangedFile file : addedFiles) {
                    try {
                        byte[] fileContent = Files.readAllBytes(file.getFile().toPath());
                        ListenableFuture<SendResult<Long, FileDTO>> sendResult = kafkaSender.send(
                                new FileDTO(file.getRelativeName(),
                                        fileContent));
                        sendResult.addCallback(fileSenderListener::successSend, fileSenderListener::failSend);
                    } catch (IOException e) {
                        log.error("Error occurred while reading file.");
                    }
                }
            }
        });
        fileSystemWatcher.start();
        log.info("Directory Scanner started");
    }


}
