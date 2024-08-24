package mil.teng.q2024.sedsvc.emu.via.kafka.config;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;

@Configuration
@Slf4j
public class KafkaConfig implements InitializingBean {
    private final KafkaProperties kafkaProperties;
    private final KafkaAdmin kafkaAdmin;

    @Value("${sedSvc.topic-rcpt}")
    private String topicRcpt;

    @Value("${sedSvc.topic-from}")
    private String topicFrom;

    public KafkaConfig(KafkaProperties kafkaProperties, KafkaAdmin kafkaAdmin) {
        log.debug("KafkaConfig: .ctor. kProps={} admin={}", kafkaProperties, kafkaAdmin);
        this.kafkaProperties = kafkaProperties;
        this.kafkaAdmin = kafkaAdmin;
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
        Map<String, Object> props = producerFactory.getConfigurationProperties();
        log.debug("kafkaTemplate. producerFactory.props({})=[", props.size());
        props.forEach((key, val) -> log.debug("- ent: {} -> {}", key, val));
        log.debug("]");
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * check (Kafka topic names are case-sensitive) topics exists
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("afterPropertiesSet-beg. topicFrom={} topicRcpt={}", this.topicFrom, this.topicRcpt);
        if (!StringUtils.hasText(this.topicFrom) || !StringUtils.hasText(this.topicRcpt)) {
            throw new IllegalStateException("All topics must be non-empty. "
                    + "topicFrom=[" + topicFrom + "] topicRcpt=[" + topicRcpt + "]");
        }
        try (AdminClient admin = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            ListTopicsOptions options = new ListTopicsOptions();
            ListTopicsResult topicsResult = admin.listTopics(options);
            Set<String> topics = topicsResult.names().get();
            if (topics.stream().filter(it -> it.equals(this.topicFrom) || it.equals(this.topicRcpt)).count() != 2) {
                throw new IllegalStateException("some topic not found (topic names are case-sensitive)."
                        + " topicRcpt=[" + this.topicRcpt + "]" + " topicFrom=[" + this.topicFrom + "]"
                        + " on servers="
                        + kafkaAdmin.getConfigurationProperties().get(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG)
                        + " current topics=" + topics
                );
            }
        }

        log.debug("afterPropertiesSet-end");
    }
}
