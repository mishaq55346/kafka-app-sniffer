package ru.mikhail.kafkaappteleportera;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Component
@Log4j2
public class KafkaSender {
    @Autowired
    private KafkaTemplate<Long, FileDTO> kafkaTemplate;

    public ListenableFuture<SendResult<Long, FileDTO>> send(FileDTO file) {
        return kafkaTemplate.send("file-topic", 0L, file);
    }
}
