package mil.teng.q2024.sedsvc.emu.via.kafka.dto;

import lombok.Value;

@Value(staticConstructor = "of")
public class KafkaMessage {
    private String msgA;
    private String msgB;
}
