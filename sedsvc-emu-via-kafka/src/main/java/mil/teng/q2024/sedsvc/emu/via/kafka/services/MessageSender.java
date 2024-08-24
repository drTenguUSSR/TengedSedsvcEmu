package mil.teng.q2024.sedsvc.emu.via.kafka.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import mil.teng.q2024.sedsvc.emu.via.kafka.dto.KafkaMessage;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class MessageSender {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${sedSvc.topic-rcpt}")
    private String rcptTopicName;

    public MessageSender(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String send(KafkaMessage msg) throws JsonProcessingException, ExecutionException, InterruptedException {
        CompletableFuture<SendResult<String, Object>> sendRes = kafkaTemplate.send(rcptTopicName, msg);
        //TODO: forced sync send
        SendResult<String, Object> res = sendRes.get();
        RecordMetadata meta = res.getRecordMetadata();
        log.debug("sended: key={} meta={}", res.getProducerRecord().key(), meta.toString());
        return meta.toString();
    }
}
