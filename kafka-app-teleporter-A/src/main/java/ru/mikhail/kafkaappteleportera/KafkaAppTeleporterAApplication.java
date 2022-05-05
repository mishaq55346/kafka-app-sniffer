package ru.mikhail.kafkaappteleportera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafka
@EnableAsync
@SpringBootApplication
public class KafkaAppTeleporterAApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaAppTeleporterAApplication.class, args);
    }
}
