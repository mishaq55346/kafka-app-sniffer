package ru.mikhail.kafkaappteleporterb;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class KafkaAppTeleporterBApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(KafkaAppTeleporterBApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
