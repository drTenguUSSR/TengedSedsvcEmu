/*
 *
 * @author DrTengu. 2024/09
 */

package mil.teng.q2024.sedsvc.emu.via.kafka.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EnableKafka
public class MessageConsumer {
    private final String recvTopics = "${sedSvc.topic-rcpt}";
    private final String recvGroupId = "${spring.kafka.consumer.group-id}";

    public MessageConsumer() {
        log.debug(".ctor");
    }

    @KafkaListener(topics = recvTopics, groupId = recvGroupId)
    public void msgListener(String msg) {
        log.warn("recv={}", msg);
    }
}
