/*
 *
 * @author DrTengu. 2024/09
 */

package mil.teng.q2024.sedsvc.emu.via.kafka.temp.simple;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringDeserializer;

public class SimpleConsoleConsumer {
    public static void main(String[] args) {
        xlog("SimpleConsoleConsumer/beg");
        var props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "app-simple");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of("top-reg", "fake-A", "fake-B"));

        while (true) {
            final ConsumerRecords<String, String> recs = consumer.poll(Duration.ofSeconds(10));
            recs.forEach(data -> {
                final Headers allHeaders = data.headers();
                StringBuilder sbHeaders = new StringBuilder();
                sbHeaders.append("headers=[");
                allHeaders.forEach(hdr -> {
                    final String key = hdr.key();
                    final String val = new String(hdr.value(), StandardCharsets.UTF_8);
                    sbHeaders.append(key).append("->").append(val).append(",");
                });
                sbHeaders.append("]");
                xlog("got!\n\tkey:" + data.key() + "\n\theaders:" + sbHeaders.toString() + "\nval:[" + data.value() + "\n]");
            });
        }
        //xlog("end");

    }

    private static void xlog(String msg) {
        System.out.println(msg);
    }
}
