package ru.mikhail.kafkaappteleporterb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class KafkaAppTeleporterBApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaAppTeleporterBApplication.class, args);
    }
}
