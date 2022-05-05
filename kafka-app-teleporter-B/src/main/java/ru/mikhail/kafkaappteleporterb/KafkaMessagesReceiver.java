package ru.mikhail.kafkaappteleporterb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

@Log4j2
@EnableKafka
public class KafkaMessagesReceiver {
    @Value("${teleporter.copy-folder}")
    private String folder;

    @KafkaListener(topics = "file-topic")
    private void msgListener(ConsumerRecord<Long, String> record) throws JsonProcessingException {
        if (!folder.endsWith("/")) {
            folder += "/";
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
