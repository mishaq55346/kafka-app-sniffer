package ru.mikhail.kafkaappteleportera;

import commons.FileDTO;
import lombok.SneakyThrows;
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
import java.util.concurrent.*;
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
                FileSendListenerThread thread = new FileSendListenerThread(addedFiles
                        .stream()
                        .map(ChangedFile::getFile)
                        .collect(Collectors.toList()));
                thread.start();

                for (ChangedFile file : addedFiles) {
                    log.info("Teleporting file [" + file.getRelativeName() + "]");
                    try {
                        byte[] fileContent = Files.readAllBytes(file.getFile().toPath());
                        ListenableFuture<SendResult<Long, FileDTO>> sendResult = kafkaSender.send(
                                new FileDTO(file.getRelativeName(),
                                        fileContent));
                        sendResult.addCallback(result -> thread.confirmSend(), log::error);
                    } catch (IOException e) {
                        log.error("Error occurred while reading file.");
                    }
                }
            }
        });
        fileSystemWatcher.start();
        log.info("File Watcher started");
    }

    class FileSendListenerThread extends Thread {
        private int sentCount;
        private final int totalCount;
        private final List<File> filesToDelete;

        public FileSendListenerThread(List<File> filesToDelete) {
            this.sentCount = 0;
            this.totalCount = filesToDelete.size();
            this.filesToDelete = filesToDelete;
        }

        public void confirmSend(){
            sentCount++;
        }

        @SneakyThrows
        @Override
        public void run() {
            ExecutorService service = Executors.newSingleThreadExecutor();

            try {
                Runnable r = () -> {
                    while (sentCount != totalCount){
                        log.info(sentCount + " of " + totalCount + " are sent");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            log.error(e);
                        }
                    }
                    log.info(sentCount + " of " + totalCount + " are sent");
                };

                Future<?> f = service.submit(r);

                f.get(5, TimeUnit.SECONDS);     // attempt the task for two minutes
            }
            catch (final InterruptedException | ExecutionException e) {
                log.error(e);
            }
            catch (final TimeoutException e) {
                log.error("Send took too long. Aborting deletion.");
                service.shutdown();
            }
            finally {
                service.shutdown();
                for (File f : filesToDelete){
                    f.delete();
                }
            }

        }


    }
}
