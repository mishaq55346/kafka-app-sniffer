package ru.mikhail.kafkaappteleportera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
@ComponentScan("commons")
public class KafkaAppTeleporterAApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaAppTeleporterAApplication.class, args);
    }
}
