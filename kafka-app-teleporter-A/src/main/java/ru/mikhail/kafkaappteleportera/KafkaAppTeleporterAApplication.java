package ru.mikhail.kafkaappteleportera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class KafkaAppTeleporterAApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaAppTeleporterAApplication.class, args);
    }
}
