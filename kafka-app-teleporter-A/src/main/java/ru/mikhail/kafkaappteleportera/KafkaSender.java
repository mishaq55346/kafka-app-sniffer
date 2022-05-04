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

    public void send(FileDTO file) {
        ListenableFuture<SendResult<Long, FileDTO>> sendResult
                = kafkaTemplate.send("file-topic", 0L, file);
        sendResult.addCallback(log::debug, log::error);
    }
}
