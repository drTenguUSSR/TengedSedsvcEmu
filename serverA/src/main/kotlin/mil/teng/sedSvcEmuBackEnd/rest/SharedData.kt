package mil.teng.sedSvcEmuBackEnd.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging


/**
 * @author DrTengu, 2024/05
 */
object SharedData {
    val logger = KotlinLogging.logger {}
    val objMapper: ObjectMapper

    inline fun <reified T> parseOne(param: String): T? {
        val res = objMapper.factory.createParser(param.byteInputStream())
            .use { jparser -> objMapper.readValue<T>(jparser) }
        return res
    }

    fun <T> parseList(param: String): List<T> {
        val res = objMapper.factory.createParser(param.byteInputStream())
            .use { jparser -> objMapper.readValue<List<T>>(jparser) }
        return res
    }

    init {
        logger.warn { "SharedData-init called" }
        objMapper = jacksonObjectMapper().apply {
            setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
            enable(SerializationFeature.INDENT_OUTPUT)
            // только для обратной совместимости
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }

    }
}