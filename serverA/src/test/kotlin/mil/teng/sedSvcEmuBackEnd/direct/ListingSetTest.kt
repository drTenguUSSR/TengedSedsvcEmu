package mil.teng.sedSvcEmuBackEnd.direct

import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Test

/**
 * @author DrTengu, 2024/05
 */
class ListingSetTest {
    companion object {
        private val logger = KotlinLogging.logger {}
        const val LABEL_REG = "[МЕСТО ДЛЯ ШТАМПА]"
        const val LABEL_SIG = "[МЕСТО ДЛЯ ПОДПИСИ]"
    }

    @Test
    fun simpleList() {
        logger.debug { "simpleList:beg" }
        val markers = listOf<String>("specA", "specB", "specC")
        val lookupLabels = hashSetOf(LABEL_SIG, LABEL_REG, *markers.toTypedArray())
        logger.debug { "lookupLabels:$lookupLabels" }
        logger.debug { "simpleList:end" }
    }


}