package ru.mikhail.kafkaappteleporterb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

@EnableKafka
@EnableAsync
@Log4j2
@SpringBootApplication
public class KafkaAppTeleporterBApplication {
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
    public static void main(String[] args) {
        SpringApplication.run(KafkaAppTeleporterBApplication.class, args);
    }

}
