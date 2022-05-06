package ru.mikhail.kafkaappteleporterb2;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class KafkaAppTeleporterB2Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder(KafkaAppTeleporterB2Application.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

}
