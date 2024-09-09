/*
 *
 * @author DrTengu. 2024/09
 */

package mil.teng.q2024.sedsvc.emu.via.kafka.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * simple tests for BrokerMessageEnvelope.Builder
 * <p>
 * костыль для:
 * WARNING: Discovered 2 'junit-platform.properties' configuration files in the classpath; only the first will be used.
 * https://issues.apache.org/jira/browse/KAFKA-17121, Fix Version/s: 3.9.0
 */
class BrokerMessageEnvelopeTest {

    @Test
    void regExpA_pass() {
        Assertions.assertNull(NameValidatorUtils.validateTopicName("abc"));
    }

    @Test
    void regExpB_fail() {
        Assertions.assertNotNull(NameValidatorUtils.validateTopicName("ab"));
    }

    @Test
    void regExpC_fail() {
        Assertions.assertNotNull(NameValidatorUtils.validateTopicName("юцкх"));
    }

    @Test
    void regExpD_fail() {
        String src = """
                abcdetfgldjeovmdfk-daskdwamsawndslkdsad-wdaskljasdaksdjal-abcdetfgld
                jeovmdfk-daskdwamsawndslkdsad-wdaskljasdaksdjal-abcdetfgldjeovmdfk-d
                askdwamsawndslkdsad-wdaskljasdaksdjal-abcdetfgldjeovmdfk-daskdwamsaw
                ndslkdsad-wdaskljasdaksdjal-123214234235435345"""; // strLen=253
        Assertions.assertNotNull(NameValidatorUtils.validateTopicName(src));
    }

    @Test
    void emptyTopicTo_mustThrow() {
        BrokerMessageEnvelope.Builder envB = BrokerMessageEnvelope.builder();
        //envB.topicTo("b-to");
        envB.messageId("c-id");
        envB.contentType("d-content-type/sub-type");

        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
            envB.build();
        });

        Assertions.assertTrue(ex.getMessage().contains("topicTo error"), "unexpected message: [" + ex.getMessage() + "]");
    }

    @Test
    void emptyMessageId_mustThrow() {
        BrokerMessageEnvelope.Builder envB = BrokerMessageEnvelope.builder();
        envB.topicTo("b-to");

        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
            envB.build();
        });

        Assertions.assertTrue(ex.getMessage().contains("messageId invalid"), "unexpected message: [" + ex.getMessage() + "]");
    }

    @Test
    void emptyContentType_mustThrow() {
        BrokerMessageEnvelope.Builder envB = BrokerMessageEnvelope.builder();
        envB.topicTo("b-to");
        envB.messageId("c-id");
        //envB.contentType("d-content-type/sub-type");

        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
            envB.build();
        });

        Assertions.assertTrue(ex.getMessage().contains("contentType error:must not be null or empty"), "unexpected message: [" + ex.getMessage() + "]");
    }

    @Test
    void emptyContentTypePlain_mustThrow() {
        BrokerMessageEnvelope.Builder envB = BrokerMessageEnvelope.builder();
        envB.topicTo("b-to");
        envB.messageId("c-id");
        envB.contentType("d-content-type.sub-type");

        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
            envB.build();
        });

        Assertions.assertTrue(ex.getMessage().contains("contentType error:must contain '/'"), "unexpected message: [" + ex.getMessage() + "]");
    }

    @Test
    void emptyContentTypeCorrect_mustPass() {
        final String contentType = "application/vnd.intertrust.cm.sedsvc+json;type=svcRegSignStamp-request";
        BrokerMessageEnvelope.Builder envB = BrokerMessageEnvelope.builder();
        envB.topicTo("b-to");
        envB.messageId("c-id");
        envB.contentType(contentType);

        BrokerMessageEnvelope env = envB.build();
        Assertions.assertEquals(env.getContentType(), contentType);
    }
}