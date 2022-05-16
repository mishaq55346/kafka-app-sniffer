package ru.mikhail.kafkaappteleportera;

import commons.FileDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DirectorySniffer {
    @Value("${teleporter.monitoring-folder}")
    private String monitoringFolderPath;

    @Autowired
    private KafkaSender kafkaSender;

    @PostConstruct
    private void runScan() {
        File monitoringFolder = new File(monitoringFolderPath);
        if (!monitoringFolder.isDirectory()) {
            log.error("Can't instantiate directory. Check path");
            System.exit(-1);
        }
        if (!monitoringFolder.exists()) {
            log.info("No directory found. Creating new one");
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
                SendResultsHandler handler = new SendResultsHandler(addedFiles.size());
                for (ChangedFile file : addedFiles) {
                    log.info("Teleporting file [" + file.getRelativeName() + "]");
                    try {
                        byte[] fileContent = Files.readAllBytes(file.getFile().toPath());
                        ListenableFuture<SendResult<Long, FileDTO>> sendResult = kafkaSender.send(
                                new FileDTO(file.getRelativeName(),
                                        fileContent));
                        sendResult.addCallback(handler::handleSuccess, log::error);
                    } catch (IOException e) {
                        log.error("Error occurred while reading file.");
                    }
                }
                if (handler.successfulDeparture()) {
                    deleteFiles(handler.getFileNames());
                }
            }
        });
        fileSystemWatcher.start();
        log.info("File Watcher started");
    }

    private void deleteFiles(List<String> fileNames) {
        if (!monitoringFolderPath.endsWith("/")) {
            monitoringFolderPath += "/";
        }

        for (String fileName : fileNames) {
            File file = new File(monitoringFolderPath + fileName);
            file.delete();
            log.info("File ["
                    + fileName
                    + "] deleted locally.");
        }

    }
}
