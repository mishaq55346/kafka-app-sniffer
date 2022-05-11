package ru.mikhail.kafkaappteleporterb2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class KafkaAppTeleporterB2Application {
    public static void main(String[] args) {
        SpringApplication.run(KafkaAppTeleporterB2Application.class, args);
    }
}
