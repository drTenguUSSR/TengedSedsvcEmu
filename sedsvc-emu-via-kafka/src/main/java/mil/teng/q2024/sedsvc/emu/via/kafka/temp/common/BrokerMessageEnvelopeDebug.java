package mil.teng.q2024.sedsvc.emu.via.kafka.temp.common;

import mil.teng.q2024.sedsvc.emu.via.kafka.services.BrokerMessageEnvelope;

public class BrokerMessageEnvelopeDebug {
    public static void main(String[] args) {
        xlog("main/beg-01");
        BrokerMessageEnvelope.Builder envB = BrokerMessageEnvelope.builder();
        //envB.topicTo("b-to");
        envB.messageId("c-id");
        envB.contentType("d-content-type");
        BrokerMessageEnvelope env = envB.build();
        xlog("env=" + env.toString());
        xlog("end");
    }

    public static void xlog(String msg) {
        System.out.println(msg);
    }
}
