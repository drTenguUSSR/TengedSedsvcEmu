package mil.teng.q2024.sedsvc.emu.via.kafka.services;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * класс-конверт с билдером и валидатором
 * альтернатива без Lombok - https://habr.com/ru/articles/244521/
 */

@Slf4j
@Value
@Builder(builderClassName = "Builder")
public class BrokerMessageEnvelope {
    String topicTo;
    String topicReplyTo;
    String messageId;
    String contentType;

    public static class Builder {
        public BrokerMessageEnvelope build() {
            String msg;

            if ((msg = NameValidatorUtils.validateTopicName(topicTo)) != null) {
                throw new IllegalStateException("topicTo error:" + msg);
            }
            if (StringUtils.hasText(topicReplyTo)) {
                if ((msg = NameValidatorUtils.validateTopicName(topicReplyTo)) != null) {
                    throw new IllegalStateException("topicReplyTo error:" + msg);
                }
            }
            if (!NameValidatorUtils.safeName(messageId)) {
                throw new IllegalStateException("messageId invalid. id=["+messageId+"]");
            }
            if ((msg=NameValidatorUtils.safeContentType(contentType))!=null) {
                throw new IllegalStateException("contentType error:"+msg);
            }

            return new BrokerMessageEnvelope(topicTo, topicReplyTo, messageId, contentType);
        }
    }
}
