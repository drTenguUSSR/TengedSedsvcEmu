package mil.teng.q2024.sedsvc.emu.via.kafka;

import java.time.Instant;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.teng.q2024.sedsvc.emu.via.kafka.dto.SimpleText;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author DrTengu, 2024/08
 */

@RestController
@Slf4j
public class SimpleController {
    public SimpleController() {
        log.warn("ctor called");
    }

    @GetMapping(value = "/testOne", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getTestOne() {
        val res = new SimpleText("textA1 123 123 123 123 123 123 123 123 123 123 123 123 123 123 123 123 123 endA",
                "textB1 123 123 123 123 123 123 123 123 123 123 123 123 123 123 123 123 123 endB", null, Instant.now(),
                LocalDateTime.now());
        log.debug("getTestOne: return res=[{}]", res);
        return "{\"h1\":\"v1\"}";
    }

    @GetMapping(value = "/testTwo/{paramCommand}/", produces = MediaType.APPLICATION_JSON_VALUE)
    public SimpleText getTestTwo(@PathVariable String paramCommand) {
        log.debug("getTestTwo: got request-param={}", paramCommand);
        val res = new SimpleText("textA", "textB", null, Instant.now(), LocalDateTime.now());
        res.setTextC("param=["+paramCommand+"]");
        log.debug("getTestTwo: return res=[{}]", res);
        return res;
    }
}
