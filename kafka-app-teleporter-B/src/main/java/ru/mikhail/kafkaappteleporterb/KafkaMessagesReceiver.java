package ru.mikhail.kafkaappteleporterb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.FileDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

@Slf4j
@Component
@PropertySource("classpath:application.properties")
public class KafkaMessagesReceiver {
    @Value("${teleporter.copy-folder}")
    private String folder;

    @KafkaListener(topics = "file-topic")
    private void msgListener(ConsumerRecord<Long, String> record) throws JsonProcessingException {
        if (!folder.endsWith("/")) {
            folder += "/";
        }
        File folderFile = new File(folder);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }
        OpenOption openOption = StandardOpenOption.CREATE;
        FileDTO receivedFile = new ObjectMapper().readValue(record.value(), FileDTO.class);
        log.info("Received file " + receivedFile.getName());
        try {
            Files.write(new File(folder + receivedFile.getName()).toPath(),
                    receivedFile.getContent(),
                    openOption);
            log.info("Teleported successfully");
        } catch (IOException e) {
            log.error("Teleportation fail");
        }
    }
}
