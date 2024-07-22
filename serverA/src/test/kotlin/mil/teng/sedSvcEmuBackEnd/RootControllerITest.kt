package mil.teng.sedSvcEmuBackEnd

import io.github.oshai.kotlinlogging.KotlinLogging
import org.hamcrest.CoreMatchers
import org.hamcrest.collection.IsCollectionWithSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


/**
 * Интеграционный текст RootController. Контекст поднимается
 * @author DrTengu, 2024/07
 */

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class RootControllerITest {

    val logger = KotlinLogging.logger {}

    @Autowired
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun getRoot() {
        logger.debug { "getRoot-called." }

        val requestBuilder = MockMvcRequestBuilders.get("/api")
        this.mockMvc.perform(requestBuilder)
            .andExpectAll(
                MockMvcResultMatchers.status().isOk,
                MockMvcResultMatchers.content().contentType("application/vnd.intertrust.cm.collection+json;type=link"),
                MockMvcResultMatchers.header().exists("Access-Control-Allow-Origin"),
                MockMvcResultMatchers.header().stringValues("Access-Control-Allow-Origin", "*"),
                MockMvcResultMatchers.jsonPath("$.title").value("sedSvcEmu root title"),
                MockMvcResultMatchers.jsonPath("$.entry", CoreMatchers.`is`(CoreMatchers.notNullValue())),
                MockMvcResultMatchers.jsonPath("$.entry").isArray,
                MockMvcResultMatchers.jsonPath("$.entry", IsCollectionWithSize.hasSize<List<Any>>(2))
            )
    }
}