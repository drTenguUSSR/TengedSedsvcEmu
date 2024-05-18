package mil.teng.sedSvcEmuBackEnd.direct

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import mil.teng.sedSvcEmuBackEnd.svcCheckPdf.SvcCheckPdf
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Не используется общий подход для получения objectMapper
 * @author DrTengu, 2024/05
 */

class JacksonLibraryCheck {
    private val logger = KotlinLogging.logger {}

    @Test
    fun serializeBasicA() {
        val obj = SvcCheckPdf.FakeStampInfo("[МЕСТО ДЛЯ ПОДПИСИ]", 1, 40, 180, 20, 80)
        val res = jacksonObjectMapper().writeValueAsString(obj)
        val expected = """
            {"marker":"[МЕСТО ДЛЯ ПОДПИСИ]","pageNum":1,"topLeft_x":40,"topLeft_y":180,"height":20,"width":80}
        """.trimIndent()
        assertEquals(expected, res)
    }

    @Test
    fun serializeListA() {
        val objA = SvcCheckPdf.FakeStampInfo("[МЕСТО ДЛЯ ПОДПИСИ1]", 1, 41, 180, 20, 80)
        val objB = SvcCheckPdf.FakeStampInfo("[МЕСТО ДЛЯ ПОДПИСИ2]", 2, 42, 180, 20, 80)
        val objC = SvcCheckPdf.FakeStampInfo("[МЕСТО ДЛЯ ПОДПИСИ3]", 3, 43, 180, 20, 80)
        val objD = SvcCheckPdf.FakeStampInfo("[МЕСТО ДЛЯ ПОДПИСИ4]", 4, 44, 180, 20, 80)
        val lstObj = listOf<SvcCheckPdf.FakeStampInfo>(objA, objB, objC, objD)
        val res = jacksonObjectMapper().writeValueAsString(lstObj)
        println("serializeListA=$res")
        assertTrue { "[МЕСТО ДЛЯ ПОДПИСИ1]" in res }
        assertTrue { "[МЕСТО ДЛЯ ПОДПИСИ2]" in res }
        assertTrue { "[МЕСТО ДЛЯ ПОДПИСИ3]" in res }
        assertTrue { "[МЕСТО ДЛЯ ПОДПИСИ4]" in res }
        assertTrue { "\"topLeft_x\":41" in res }
        assertTrue { "\"topLeft_x\":42" in res }
        assertTrue { "\"topLeft_x\":43" in res }
        assertTrue { "\"topLeft_x\":44" in res }
    }

    /**
     * Тест, использующий неправильный ObjectMapper
     */
    @Test
    fun decodeDataClassFailed_errObjectMapper() {
        data class TestDataClass(val foo: String)

        val jsonString = """{ "foo": "bar" }"""
        val mapper = ObjectMapper().readerFor(TestDataClass::class.java)

        assertFailsWith<MismatchedInputException> {
            val deserializedValue = mapper.readValue<TestDataClass>(jsonString)
            logger.error { "must not work: $deserializedValue" }
        }
    }

    @Test
    fun decodeDataClassCorrect_jacksonObjectMapper() {
        data class TestDataClass2(val foo: String)

        val jsonString = """{ "foo": "bar" }"""
        val obj = jacksonObjectMapper().readerFor(TestDataClass2::class.java).readValue<TestDataClass2>(jsonString)
        assertEquals("TestDataClass2(foo=bar)",obj.toString())
    }

}