/*
 *
 * @author DrTengu. 2024/09
 */

package mil.teng.q2024.sedsvc.emu.via.kafka.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class MessageSender {
    private static final String TOPIC_HEADER_ID = "id";
    private static final String TOPIC_REPLY_TO = "reply-to";
    private static final String TOPIC_IN_REPLY_TO = "in-reply-to";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public MessageSender(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * отправка объекта в топик с обратным адресом (опционально)
     *
     * @param topicRcpt топик "получатель", куда направляется сообщение
     * @param topicReplyTo ответный топик - в него ожидается ответное сообщение (при наличии) от адресата
     * @param messageId идентификатор сообщения (используется в ответном сообщении в заголовке in-reply-to
     * @param msg       объект-сообщение
     * @return информация об отправке
     */
    public String send(@NonNull String topicRcpt, String topicReplyTo, @NonNull String messageId, @NonNull Object msg)
            throws ExecutionException, InterruptedException {
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(topicRcpt, msg);
        producerRecord.headers().add(TOPIC_HEADER_ID, messageId.getBytes(StandardCharsets.UTF_8));
        producerRecord.headers().add(TOPIC_REPLY_TO, topicReplyTo.getBytes(StandardCharsets.UTF_8));
        CompletableFuture<SendResult<String, Object>> sendRes = kafkaTemplate.send(producerRecord);
        sendRes
                .thenAccept(it -> log.debug("send: async-sended-pass={}", it))
                .exceptionally(it -> {
                    log.error("send: async-sended-err:", it);
                    return null;
                });

        //TODO: forced sync send
        SendResult<String, Object> res = sendRes.get();
        RecordMetadata meta = res.getRecordMetadata();
        log.debug("send: result: key={} meta={}", res.getProducerRecord().key(), meta.toString());
        return meta.toString();
    }
}
