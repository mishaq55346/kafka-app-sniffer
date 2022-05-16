package ru.mikhail.kafkaappteleportera;

import commons.FileDTO;
import org.springframework.kafka.support.SendResult;

import java.util.ArrayList;
import java.util.List;

public class SendResultsHandler {
    private final int filesToSendCount;
    private int sentFilesCount;
    private final List<String> sendFileNames;

    public SendResultsHandler(int filesToSendCount) {
        this.filesToSendCount = filesToSendCount;
        sendFileNames = new ArrayList<>();
        sentFilesCount = 0;
    }

    public void handleSuccess(SendResult<Long, FileDTO> longFileDTOSendResult) {
        sendFileNames.add(longFileDTOSendResult.getProducerRecord().value().getName());
        sentFilesCount++;
    }

    public boolean successfulDeparture() {
        return sentFilesCount == filesToSendCount;
    }

    public List<String> getFileNames() {
        return sendFileNames;
    }
}
