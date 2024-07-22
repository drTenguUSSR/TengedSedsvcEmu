package mil.teng.sedSvcEmuBackEnd.temp

import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

/**
 * unused for prod code
 * @author DrTengu, 2024/04
 */

@SpringBootTest
class KotlinLoggingITest {
    /*
    using https://github.com/oshai/kotlin-logging
     */
    companion object {
        val logger = KotlinLogging.logger {}

        @JvmStatic
        @BeforeAll
        fun testInit(): Unit {
            logger.debug { "testInit" }
        }
    }

    @Test
    fun contextLoads() {
        logger.error { "contextLoads: log-error. contextLoads" }
    }

    @Test
    fun `check logging is working`() {
        logger.error { "check logging: beg" }
        logger.debug { "debug-1" }
        logger.debug { "debug with calc-func => ${calcMsg("1debug1")}" }
        logger.debug { "debug-2" }
        logger.trace { "some-trace-1" }
        logger.trace { "debug with calc-func => ${calcMsg("1trace1")}" }
        logger.trace { "some-trace-2" }
        logger.info { "This is an info message" }
        logger.warn { "This is a warn message" }
        logger.error { "check logging: end" }
    }

    fun calcMsg(msg: String): String {
        logger.error { "calcMsg with [${msg}]" }
        return "return-$msg-data"
    }

}
