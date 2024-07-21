package mil.teng.sedSvcEmuBackEnd

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import mil.teng.sedSvcEmuBackEnd.rest.ContentTypeDataService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.http.MediaType

/**
 * @author DrTengu, 2024/04
 */


@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
class ContentTypeDataServiceTests(@Autowired private val svcContentType: ContentTypeDataService) {
    companion object {
        private val logger = KotlinLogging.logger {}

        @JvmStatic
        @BeforeAll
        fun testInit(): Unit {
            logger.debug { "testInit-beforeAll" }
        }
    }

    @PostConstruct
    fun postConstruct() {
        logger.debug { "postConstruct: this=$this svc=$svcContentType" }
    }

    @Test
    @DisplayName("""parse mediaType=application/json""")
    fun mediaApplicationJson() {
        val srcVal = "app/json2"
        val parsed = svcContentType.mediaType2contentType(MediaType.parseMediaType(srcVal))
        assertEquals("app/json2", parsed)
    }

    @Test
    @DisplayName("""parse mediaType=app/json2;type2=x1""")
    fun mediaApplicationJsonWithType2() {
        val srcVal = "app/json2;type2=x1"
        val parsed = svcContentType.mediaType2contentType(MediaType.parseMediaType(srcVal))
        assertEquals("app/json2", parsed)
    }

    @Test
    @DisplayName("""parse mediaType=app/json2;type=x1""")
    fun mediaApplicationJsonWithType() {
        val srcVal = "app/json2;type=x1"
        val parsed = svcContentType.mediaType2contentType(MediaType.parseMediaType(srcVal))
        assertEquals("app/json2;type=x1", parsed)
    }

    @Test
    @DisplayName("drop type param if param name not 'type'")
    fun mediaTypeDrop() {
        val srcVal = "application/json-special;charset=UTF-8"
        val parsed = svcContentType.mediaType2contentType(MediaType.parseMediaType(srcVal))
        assertEquals("application/json-special", parsed)
    }

    @Test
    @DisplayName("keep type param if param name 'type'")
    fun mediaTypeKeep() {
        val srcVal = "application/json-special;charset=UTF-8;type=alter"
        val parsed = svcContentType.mediaType2contentType(MediaType.parseMediaType(srcVal))
        assertEquals("application/json-special;type=alter", parsed)
    }
}