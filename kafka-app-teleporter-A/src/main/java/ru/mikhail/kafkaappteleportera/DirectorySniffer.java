package ru.mikhail.kafkaappteleportera;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.filewatch.ChangedFile;
import org.springframework.boot.devtools.filewatch.ChangedFiles;
import org.springframework.boot.devtools.filewatch.FileChangeListener;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
@Log4j2
@PropertySource("classpath:application.properties")
public class DirectorySniffer {
    @Value("${sniffer.base-folder}")
    private String projectPath;
    @Value("${sniffer.monitoring-folder}")
    private String folder;

    @Autowired
    private KafkaSender kafkaSender;

    @PostConstruct
    private void runScan() {
        FileSystemWatcher fileSystemWatcher = new FileSystemWatcher();
        fileSystemWatcher.addSourceDirectory(new File(projectPath + folder));
        fileSystemWatcher.addListener(changeSet -> {
            for (ChangedFiles changedFiles : changeSet) {
                for (ChangedFile file : changedFiles.getFiles()) {
                    if (file.getType() == ChangedFile.Type.ADD) {
                        try {
                            kafkaSender.send(
                                    new FileDTO(file.getRelativeName(),
                                            Files.readAllBytes(file.getFile().toPath())));
                            file.getFile().delete();
                        } catch (IOException e) {
                            log.error("Error occurred while reading file.");
                        }
                    }
                }
            }
        });
        fileSystemWatcher.start();
    }


}
