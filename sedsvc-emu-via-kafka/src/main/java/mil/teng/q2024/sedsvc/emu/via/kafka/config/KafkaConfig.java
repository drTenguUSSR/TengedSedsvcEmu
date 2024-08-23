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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
@Slf4j
public class KafkaConfig implements InitializingBean {
    private final KafkaProperties kafkaProperties;

    @Value("${sedSvc.topic-rcpt}")
    private String topicRcpt;

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String producerBootstrapServers;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        log.debug("KafkaConfig: .ctor. kProps={}", kafkaProperties);
        this.kafkaProperties = kafkaProperties;
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


    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("afterPropertiesSet-beg. servers={}, topic={}", this.producerBootstrapServers, this.topicRcpt);

        Map<String, Object> conf = new HashMap<>();
        conf.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, producerBootstrapServers);
        conf.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000"); //TODO: fix hardcoded
        try (AdminClient admin = AdminClient.create(conf)) {
            ListTopicsOptions options = new ListTopicsOptions();
            ListTopicsResult topicsResult = admin.listTopics(options);
            Set<String> topics = topicsResult.names().get();
            if (!topics.stream().anyMatch(it -> it.equals(this.topicRcpt))) {
                throw new IllegalStateException("topic [" + this.topicRcpt + "] not found on broker "
                        + producerBootstrapServers);
            }
        }
        log.debug("afterPropertiesSet-end");
    }
}
