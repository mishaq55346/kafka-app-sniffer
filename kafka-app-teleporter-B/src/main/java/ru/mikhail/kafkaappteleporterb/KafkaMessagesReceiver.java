package ru.mikhail.kafkaappteleporterb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.FileDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
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
public class KafkaMessagesReceiver {
    @Value("${teleporter.copy-folder}")
    private String folder;

    @KafkaListener(topicPartitions = @TopicPartition(topic = "file-topic", partitions = {"0"}),
            groupId = "group1")
    private void msgListener(ConsumerRecord<Long, String> record) throws JsonProcessingException {
        if (!folder.endsWith("/")) {
            folder += "/";
        }
        File folderFile = new File(folder);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }
        OpenOption openOption = StandardOpenOption.CREATE;
        FileDTO file = new ObjectMapper().readValue(record.value(), FileDTO.class);
        try {
            Files.write(new File(folder + file.getName()).toPath(),
                    file.getContent(),
                    openOption);
        } catch (IOException e) {
            log.error("Failed to write to file");
        }
    }
}
