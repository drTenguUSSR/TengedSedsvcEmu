package mil.teng.q2024.sedsvc.emu.via.kafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import mil.teng.q2024.sedsvc.emu.via.kafka.dto.KafkaMessage;
import mil.teng.q2024.sedsvc.emu.via.kafka.dto.SimpleText;
import mil.teng.q2024.sedsvc.emu.via.kafka.services.MessageSender;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class MakeMessageController {
    private final MessageSender messageSender;

    public MakeMessageController(MessageSender messageSender) {
        log.warn("ctor called");
        this.messageSender = messageSender;
    }

    @GetMapping(value = "/makeMessage", produces = MediaType.APPLICATION_JSON_VALUE)
    public SimpleText makeMessage() throws ExecutionException, JsonProcessingException, InterruptedException {
        final String markA = LocalDateTime.now().toString();
        final String markB = UUID.randomUUID().toString();
        log.debug("makeMessage: markA={}, markB={}", markA, markB);
        KafkaMessage kMsg = KafkaMessage.of(markA, markB);
        String resCall = messageSender.send(kMsg);
        SimpleText resSim = new SimpleText(markA, markB, resCall, null, null);
        log.debug("getTestOne: resSim={}", resSim);
        return resSim;
    }
}
