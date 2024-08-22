package mil.teng.q2024.sedsvc.emu.via.kafka.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
@Slf4j
public class KafkaConfig {
    private final KafkaProperties kafkaProperties;
    private final String topicRcpt;

    public KafkaConfig(KafkaProperties kafkaProperties, @Value("${sedSvc.topic-rcpt}") String topicRcpt) {
        log.debug("KafkaConfig: .ctor. kProps={}, topicRcpt={}", kafkaProperties, topicRcpt);
        this.kafkaProperties = kafkaProperties;
        this.topicRcpt = topicRcpt;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        log.debug("producerFactory: called. kafkaProperties={}", kafkaProperties);
        SslBundles sslBundles = new DefaultSslBundleRegistry(); //TODO check!
        Map<String, Object> props = kafkaProperties.buildProducerProperties(sslBundles);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        log.debug("kafkaTemplate: called. producerFactory={}", producerFactory);
        return new KafkaTemplate<>(producerFactory);
    }
}
