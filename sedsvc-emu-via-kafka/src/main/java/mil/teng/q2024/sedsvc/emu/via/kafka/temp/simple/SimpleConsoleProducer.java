package mil.teng.q2024.sedsvc.emu.via.kafka.temp.simple;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author DrTengu, 2024/07
 */
public class SimpleConsoleProducer {

    private static final Logger logger = LoggerFactory.getLogger(SimpleConsoleProducer.class);

    private static final DateTimeFormatter DTM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnn'X'xx");

    public static void main(String[] args) throws ExecutionException, InterruptedException, JsonProcessingException {
        logger.warn("SimpleConsoleProducer/01 run");

        var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "client.app.demo.01");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //props.put(ProducerConfig.ACKS_CONFIG,)

        try (final KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            final String topic = "top-reg";
            final String uuid = UUID.randomUUID().toString();
            final String dtmStampStr = ZonedDateTime.now().format(DTM_FORMAT);
            final String message = makeMessage(dtmStampStr);
            xlog("message(" + uuid + ") ->" + message);

            final ProducerRecord<String, String> dataRecord = new ProducerRecord<>(topic, uuid, message);
            dataRecord.headers().add("stamp", dtmStampStr.getBytes(StandardCharsets.UTF_8));
            dataRecord.headers().add("reply-topic", "top-reg-reply-client01".getBytes(StandardCharsets.UTF_8));
            dataRecord.headers().add("content-type",
                    "application/vnd.intertrust.cm.sedsvc+json;type=svcRegSignStamp-request".getBytes(StandardCharsets.UTF_8));
            final RecordMetadata ft = producer.send(dataRecord).get();
            xlog("ft.string=" + ft);
        }
        xlog("app-end");

    }

    private static String makeMessage(String dtmStampStr) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        final ObjectNode objA = createRegSignStampRequestA(objectMapper,dtmStampStr);

        return objectMapper.writeValueAsString(objA);
    }

    /**
     * example request
     * {
     * "regStampDatas":[
     * {
     * "id":"test-reg",
     * "date":"2016-05-10",
     * "number":"3333-333-34"
     * }
     * ],
     * "signStampDatas":[
     * {
     * "id":"test-sign",
     * "certificate":"12000F8147B11ED99B090F61450000000F8147",
     * "signer":"Иванов Иван Иванович",
     * "validFrom":"2016-01-05",
     * "validTo":"2017-01-05"
     * }
     * <p>
     * ]
     * }
     */

    private static ObjectNode createRegSignStampRequestA(ObjectMapper objectMapper,String dtmStampStr) {
        final ObjectNode objA = objectMapper.createObjectNode();
        objA.put("stmStamp", dtmStampStr);

        final ArrayNode arrRegs = objectMapper.createArrayNode();
        final ObjectNode regOne = objectMapper.createObjectNode();
        regOne.put("id", "test-reg");
        regOne.put("date", "2016-05-10");
        regOne.put("number", "3333-333-34");
        arrRegs.add(regOne);

        final ArrayNode arrSigns = objectMapper.createArrayNode();
        final ObjectNode sigOne = objectMapper.createObjectNode();
        sigOne.put("id", "test-sign");
        sigOne.put("certificate", "12000F8147B11ED99B090F61450000000F8147");
        sigOne.put("signer", "Иванов Иван Иванович");
        sigOne.put("validFrom", "2016-01-05");
        sigOne.put("validTo", "2017-01-05");
        arrSigns.add(sigOne);

        objA.set("regStampDatas", arrRegs);
        objA.set("signStampDatas", arrSigns);
        return objA;
    }

    private static void xlog(String msg) {
        System.out.println(msg);
    }

}

