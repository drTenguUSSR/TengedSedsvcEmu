package mil.teng.sedSvcEmuBackEnd.svcCheckPdf

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith


/**
 * @author DrTengu, 2024/05
 */

class FakeStampInfoTest {

    @Test
    fun decodeEmptyJson() {
        val param = "{}"
        assertFailsWith<MismatchedInputException> {
            val res = FakeStampInfo.parse(param)
            println(res)
        }
    }

    @Test
    @DisplayName("decode.Пропущена запятая в перечислении")
    fun decodeBrokenJasonA() {
        val param = """
            {"marker":"[МЕСТО ДЛЯ ПОДПИСИ]","pageNum":1"topLeft_x":40,"topLeft_y":180,"height":20,"width":80}
        """.trimIndent()

        assertFailsWith<JsonParseException> {
            val res = FakeStampInfo.parse(param)
            println(res)
        }
    }

    @Test
    @DisplayName("decode.неправильный тип pageNum")
    fun decodeBrokenJasonB() {
        val param = """
            {"marker":"[МЕСТО ДЛЯ ПОДПИСИ]","pageNum":"x","topLeft_x":40,"topLeft_y":180,"height":20,"width":80}
        """.trimIndent()

        assertFailsWith<MismatchedInputException> {
            val res = FakeStampInfo.parse(param)
            println(res)
        }
    }

    @Test
    fun decodeFullCorrect() {
        val param = """|{
            |"marker":"[МЕСТО ДЛЯ ПОДПИСИ]",
            |"pageNum":1,
            |"topLeft_x":40,
            |"topLeft_y":180,
            |"height":20,
            |"width":80
            |}""".trimMargin()

        val res = FakeStampInfo.parse(param)
        println(res)
    }
}