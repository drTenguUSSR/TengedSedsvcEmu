package mil.teng.q2024.sedsvc.emu.via.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SedsvcEmuViaKafkaApplication {

    private static final DateTimeFormatter DTM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnn'X'xx");

    public static void main(String[] args) throws ExecutionException, InterruptedException, JsonProcessingException {

        xlog("SedsvcEmuViaKafkaApplication/01 run");
        //TODO: use SpringBoot
        //SpringApplication.run(SedsvcEmuViaKafkaApplication.class, args);

    }

    private static void xlog(String msg) {
        System.out.println(msg);
    }

}
