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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Log4j2
@PropertySource("classpath:application.properties")
public class DirectorySniffer {
    @Value("${teleporter.monitoring-folder}")
    private String monitoringFolderPath;

    @Autowired
    private KafkaSender kafkaSender;

    int operatedFiles = 0;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SS");

    @PostConstruct
    private void runScan() {
        if (!monitoringFolderPath.endsWith("/")) {
            monitoringFolderPath += "/";
        }
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
                FileSenderListener senderListener = new FileSenderListener(addedFiles.size());
                senderListener.logFilesAdded(addedFiles);
                for (ChangedFile file : addedFiles) {
                    try {
                        byte[] fileContent = Files.readAllBytes(file.getFile().toPath());
                        ListenableFuture<SendResult<Long, FileDTO>> sendResult = kafkaSender.send(
                                new FileDTO(file.getRelativeName(),
                                        fileContent));
                        sendResult.addCallback(senderListener::successSend, senderListener::failSend);
                    } catch (IOException e) {
                        log.error("Error occurred while reading file.");
                    }
                }
            }
        });
        fileSystemWatcher.start();
        log.info("File Watcher started");
    }

    class FileSenderListener {
        private int sentCount;
        private final int totalCount;

        public FileSenderListener(int totalCount) {
            this.totalCount = totalCount;
            sentCount = 0;
        }

        public void logFilesAdded(List<ChangedFile> addedFiles){
            log.info(String.format("[%s] Found %d files in folder: {%s}",
                    dateFormat.format(new Date()),
                    addedFiles.size(),
                    addedFiles
                            .stream()
                            .map(ChangedFile::getRelativeName)
                            .collect(Collectors.joining(","))
                    ));

        }

        public void successSend(SendResult<Long, FileDTO> sendResult) {
            sentCount++;
            File fileToDelete = new File(monitoringFolderPath + sendResult.getProducerRecord().value().getName());
            String fileName = fileToDelete.getName();
            fileToDelete.delete();
            log.info(String.format("[%s] File [%s] sent successfully and deleted locally. %d of %d files are sent",
                    dateFormat.format(new Date()), fileName, sentCount, totalCount));
        }

        public void failSend(Throwable error) {
            log.info(String.format("[%s] File can not be sent due to error: [%s].",
                    dateFormat.format(new Date()), error.getMessage()));
        }
    }
}
