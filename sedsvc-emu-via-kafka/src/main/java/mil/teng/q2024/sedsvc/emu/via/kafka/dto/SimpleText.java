package mil.teng.q2024.sedsvc.emu.via.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * @author DrTengu, 2024/08
 */

@AllArgsConstructor
@Setter
@Getter
@ToString
final public class SimpleText {
    private String textA;
    private String textB;
    private String textC;
    private Instant dateTime;
    private LocalDateTime localDtm;
}