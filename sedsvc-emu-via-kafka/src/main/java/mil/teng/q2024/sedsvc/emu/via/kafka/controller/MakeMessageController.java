package mil.teng.q2024.sedsvc.emu.via.kafka.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import mil.teng.q2024.sedsvc.emu.via.kafka.dto.KafkaMessage;
import mil.teng.q2024.sedsvc.emu.via.kafka.dto.SimpleText;
import mil.teng.q2024.sedsvc.emu.via.kafka.services.MessageSender;
import mil.teng.q2024.sedsvc.emu.via.kafka.services.TengUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class MakeMessageController {
    public static final String HEADER_CONTENT_TYPE = "_header-content-type";
    private static final String SRC_REG_REQUEST_01PASS = "reg-sign-stamp.01.json";
    private static final String SRC_REG_REQUEST_01ERR = "reg-sign-stamp.02.err.json";
    private final MessageSender messageSender;
    private final ObjectMapper objectMapper;
    @Value("${sedsvc.cache-dir}")
    String baseFolder;

    @Value("${sedSvc.topic-rcpt}")
    private String topicRcpt;

    @Value("${sedSvc.topic-reply-to}")
    private String topicReplyTo;

    public MakeMessageController(MessageSender messageSender, ObjectMapper objectMapper) {
        log.warn("ctor called");
        this.messageSender = messageSender;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/makeMessage", produces = MediaType.APPLICATION_JSON_VALUE)
    public SimpleText makeMessage() throws ExecutionException, InterruptedException {
        final String markA = LocalDateTime.now().toString();
        final String markB = UUID.randomUUID().toString();
        log.debug("makeMessage: markA={}, markB={}", markA, markB);
        KafkaMessage kMsg = KafkaMessage.of(markA, markB);
        String resCall = messageSender.send(topicRcpt, topicReplyTo, markA, kMsg);
        SimpleText resSim = new SimpleText(markA, markB, resCall, null, null);
        log.debug("makeMessage: resSim={}", resSim);
        return resSim;
    }

    @GetMapping(value = "/makeRegSignStampA/{fileCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SimpleText makeRegSignStampA(@PathVariable String fileCode) throws IOException, ExecutionException, InterruptedException {
        final String messageId = UUID.randomUUID().toString();

        Path tempFolder = TengUtils.makeTempSubfolder(baseFolder, "makeRegStampReq");
        final String srcStrFile;
        if ("01PASS".equalsIgnoreCase(fileCode)) {
            srcStrFile = SRC_REG_REQUEST_01PASS;
        } else if ("01ERR".equalsIgnoreCase(fileCode)) {
            srcStrFile = SRC_REG_REQUEST_01ERR;
        } else {
            throw new IllegalStateException("wrong argument 'fileCode'. support only '01PASS' and '01ERR'");
        }
        log.debug("using file '{}' for code='{}'", srcStrFile, fileCode);

        TengUtils.copyResourceTo(srcStrFile, "svc-example-requests", tempFolder.toFile());

        String strReaded = Files.readString(tempFolder.resolve(srcStrFile), StandardCharsets.UTF_8);
        log.debug("strReaded:[\n{}\n", strReaded);
        String resCall;
        try {
            ObjectNode node = (ObjectNode) objectMapper.readTree(strReaded);
            JsonNode ct = node.get(HEADER_CONTENT_TYPE);
            if (ct == null) {
                throw new IllegalStateException("wrong format of [" + srcStrFile + "] header [" + HEADER_CONTENT_TYPE + "] not found");
            }
            String ct2 = ct.asText();
            log.debug("ct2={}", ct2);
            node.remove(HEADER_CONTENT_TYPE);
            //TODO: refactor 4 BrokerMessageEnvelope
            resCall = messageSender.send(topicRcpt, topicReplyTo, messageId, node);
        } catch (JsonParseException ex) {
            throw new IllegalArgumentException("failed to JSON-parse object of [\n" + strReaded + "\n]"
                    + " from " + srcStrFile);
        }

        SimpleText resSim = new SimpleText("markA", "markB", resCall, null, null);
        log.debug("makeRegSignStampA: resSim={}", resSim);
        return resSim;
    }
}
