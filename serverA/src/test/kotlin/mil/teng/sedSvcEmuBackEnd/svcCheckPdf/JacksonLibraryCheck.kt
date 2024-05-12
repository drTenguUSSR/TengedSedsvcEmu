package mil.teng.sedSvcEmuBackEnd.svcCheckPdf

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

/**
 * @author DrTengu, 2024/05
 */
class JacksonLibraryCheck {

    @Test
    fun serializeBasicA() {
        val obj = FakeStampInfo("[МЕСТО ДЛЯ ПОДПИСИ]", 1, 40, 180, 20, 80)
        val res = jacksonObjectMapper().writeValueAsString(obj)
        println("res=$res")
    }

    @Test
    fun decodeDataClassFailed_ObjectMapper() {
        data class TestDataClass(val foo: String)

        val jsonString = """{ "foo": "bar" }"""
        val mapper = ObjectMapper().readerFor(TestDataClass::class.java)

        assertFailsWith<MismatchedInputException> {
            val deserializedValue = mapper.readValue<TestDataClass>(jsonString)
            println(deserializedValue)
        }
    }

    @Test
    fun decodeDataClassCorrect_jacksonObjectMapper() {
        data class TestDataClass2(val foo: String)

        val jsonString = """{ "foo": "bar" }"""
        val obj = jacksonObjectMapper().readerFor(TestDataClass2::class.java).readValue<TestDataClass2>(jsonString)
        println(obj)
    }

}