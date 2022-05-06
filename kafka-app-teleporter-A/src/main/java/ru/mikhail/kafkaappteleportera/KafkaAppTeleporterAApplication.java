package ru.mikhail.kafkaappteleportera;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableKafka
@EnableAsync
@SpringBootApplication
public class KafkaAppTeleporterAApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(KafkaAppTeleporterAApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
