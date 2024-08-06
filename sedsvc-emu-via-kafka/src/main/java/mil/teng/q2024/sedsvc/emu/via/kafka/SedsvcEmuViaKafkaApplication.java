package mil.teng.q2024.sedsvc.emu.via.kafka;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
@Slf4j
public class SedsvcEmuViaKafkaApplication {

    public static void main(String[] args) {
        log.debug("SedsvcEmuViaKafkaApplication/01 run");
        SpringApplication.run(SedsvcEmuViaKafkaApplication.class, args);
    }

}
