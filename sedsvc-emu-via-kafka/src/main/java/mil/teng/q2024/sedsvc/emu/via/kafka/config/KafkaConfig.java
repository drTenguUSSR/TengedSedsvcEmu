package mil.teng.q2024.sedsvc.emu.via.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mil.teng.q2024.sedsvc.emu.via.kafka.services.TengUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Configuration
@Slf4j
public class KafkaConfig implements InitializingBean {
    private final KafkaProperties kafkaProperties;
    private final KafkaAdmin kafkaAdmin;

    @Value("${sedSvc.topic-rcpt}")
    private String topicRcpt;

    @Value("${sedSvc.topic-reply-to}")
    private String topicReplyTo;

    public KafkaConfig(KafkaProperties kafkaProperties, KafkaAdmin kafkaAdmin) {
        log.debug("KafkaConfig: .ctor");
        this.kafkaProperties = kafkaProperties;
        this.kafkaAdmin = kafkaAdmin;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(ObjectMapper objectMapper) {
        log.debug("producerFactory: called. kafkaProperties={}", kafkaProperties);
        SslBundles sslBundles = new DefaultSslBundleRegistry(); //TODO check!
        Map<String, Object> props = kafkaProperties.buildProducerProperties(sslBundles);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // set to remove '__TypeId__' Kafka header with full class name
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        DefaultKafkaProducerFactory<String, Object> kFactory = new DefaultKafkaProducerFactory<>(props);
        kFactory.setValueSerializer(new JsonSerializer<>(objectMapper));
        return kFactory;
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory(ObjectMapper objectMapper) {
        log.debug("consumerFactory: called. kafkaProperties={}", kafkaProperties);
        SslBundles sslBundles = new DefaultSslBundleRegistry(); //TODO check!
        Map<String, Object> props = kafkaProperties.buildConsumerProperties(sslBundles);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        DefaultKafkaConsumerFactory<String, Object> kFactory = new DefaultKafkaConsumerFactory<>(props);
        kFactory.setValueDeserializer(new JsonDeserializer<>(objectMapper));
        return kFactory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
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
     */
    @Override
    public void afterPropertiesSet() throws ExecutionException, InterruptedException {
        log.debug("afterPropertiesSet-beg");
        log.debug("env: tempFolder={}", TengUtils.getTempFolder());

        log.debug("topicReplyTo={} topicRcpt={}", this.topicReplyTo, this.topicRcpt);
        if (!StringUtils.hasText(this.topicReplyTo) || !StringUtils.hasText(this.topicRcpt)) {
            throw new IllegalStateException("All topics must be non-empty. "
                    + "topicFrom=[" + topicReplyTo + "] topicRcpt=[" + topicRcpt + "]");
        }
        try (AdminClient admin = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            ListTopicsOptions options = new ListTopicsOptions();
            ListTopicsResult topicsResult = admin.listTopics(options);
            Set<String> topics = topicsResult.names().get();
            if (topics.stream().filter(it -> it.equals(this.topicReplyTo) || it.equals(this.topicRcpt)).count() != 2) {
                throw new IllegalStateException("some topic not found (topic names are case-sensitive)."
                        + " topicRcpt=[" + this.topicRcpt + "]" + " topicFrom=[" + this.topicReplyTo + "]"
                        + " on servers="
                        + kafkaAdmin.getConfigurationProperties().get(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG)
                        + " current topics=" + topics
                );
            }
        }

        log.debug("afterPropertiesSet-end");
    }
}
