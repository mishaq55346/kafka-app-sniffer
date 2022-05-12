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
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
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
        if (monitoringFolder == null || !monitoringFolder.isDirectory()) {
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
                for (ChangedFile file : changedFiles.getFiles()) {
                    if (file.getType() == ChangedFile.Type.ADD) {
                        log.info("Teleporting file [" + file.getRelativeName() + "]");
                        try {
                            for (int i = 0; i < kafkaSender.getPartitionsCount(); i++) {
                                ListenableFuture<SendResult<Long, FileDTO>> sendResult = kafkaSender.send(
                                        new FileDTO(file.getRelativeName(),
                                                Files.readAllBytes(file.getFile().toPath())), i);
                                sendResult.addCallback(this::successSendHandler, log::error);
                            }
                        } catch (IOException e) {
                            log.error("Error occurred while reading file.");
                        }
                    }
                }
            }
        });
        fileSystemWatcher.start();
        log.info("File Watcher started");
    }

    private void successSendHandler(SendResult<Long, FileDTO> fileDTOSendResult) {
        if (!monitoringFolderPath.endsWith("/")) {
            monitoringFolderPath += "/";
        }
        File file = new File(monitoringFolderPath + fileDTOSendResult.getProducerRecord().value().getName());
        file.delete();
        log.info("File ["
                + fileDTOSendResult.getProducerRecord().value().getName()
                + "] teleported to partition "
                + fileDTOSendResult.getRecordMetadata().partition()
                + ".");
    }
}
