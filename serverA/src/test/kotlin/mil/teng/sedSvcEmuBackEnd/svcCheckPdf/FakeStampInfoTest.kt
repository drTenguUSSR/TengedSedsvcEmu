package mil.teng.sedSvcEmuBackEnd.svcCheckPdf

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import mil.teng.sedSvcEmuBackEnd.rest.SharedData
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


/**
 * @author DrTengu, 2024/05
 */


class FakeStampInfoTest {

    private val logger = KotlinLogging.logger {}

    @Test
    fun decodeEmptyJson() {
        val param = "{}"
        assertFailsWith<MismatchedInputException> {
            val res = SharedData.parseOne<SvcCheckPdf.FakeStampInfo>(param)
            logger.error { "must not work: $res" }
        }
    }

    @Test
    @DisplayName("decode.Пропущена запятая в перечислении")
    fun decodeBrokenJasonA() {
        val param = """
            {"marker":"[МЕСТО ДЛЯ ПОДПИСИ]","pageNum":1"topLeft_x":40,"topLeft_y":180,"height":20,"width":80}
        """.trimIndent()

        assertFailsWith<JsonParseException> {
            val res = SharedData.parseOne<SvcCheckPdf.FakeStampInfo>(param)
            logger.error { "must not work: $res" }
        }
    }

    @Test
    @DisplayName("decode.неправильный тип pageNum")
    fun decodeBrokenJasonB() {
        val param = """
            {"marker":"[МЕСТО ДЛЯ ПОДПИСИ]","pageNum":"x","topLeft_x":40,"topLeft_y":180,"height":20,"width":80}
        """.trimIndent()

        assertFailsWith<MismatchedInputException> {
            val res = SharedData.parseOne<SvcCheckPdf.FakeStampInfo>(param)
            logger.error { "must not work: $res" }
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

        val res = SharedData.parseOne<SvcCheckPdf.FakeStampInfo>(param)
        assertEquals("[МЕСТО ДЛЯ ПОДПИСИ]", res?.marker)
        assertEquals(1, res?.pageNum)
        assertEquals(40, res?.topLeft_x)
        assertEquals(180, res?.topLeft_y)
        assertEquals(20, res?.height)
        assertEquals(80, res?.width)
    }

    private fun makeListA_asString() = """
            [
                {
                    "marker": "[МЕСТО ДЛЯ ПОДПИСИ1]",
                    "pageNum": 1,
                    "topLeft_x": 41,
                    "topLeft_y": 180,
                    "height": 20,
                    "width": 80
                },
                {
                    "marker": "[МЕСТО ДЛЯ ПОДПИСИ2]",
                    "pageNum": 2,
                    "topLeft_x": 42,
                    "topLeft_y": 180,
                    "height": 20,
                    "width": 80
                },
                {
                    "marker": "[МЕСТО ДЛЯ ПОДПИСИ3]",
                    "pageNum": 3,
                    "topLeft_x": 43,
                    "topLeft_y": 180,
                    "height": 20,
                    "width": 80
                },
                {
                    "marker": "[МЕСТО ДЛЯ ПОДПИСИ4]",
                    "pageNum": 4,
                    "topLeft_x": 44,
                    "topLeft_y": 184,
                    "height": 24,
                    "width": 84
                }
            ]
        """.trimIndent()

    @Test
    fun decodeListCorrectA() {
        val param = makeListA_asString()
        logger.debug { "src=[\n$param\n]" }

        val res = SharedData.objMapper.readValue<List<SvcCheckPdf.FakeStampInfo>>(param)
        assertEquals(4, res.size)

        assertEquals("[МЕСТО ДЛЯ ПОДПИСИ1]", res[0].marker)
        assertEquals(2, res[1].pageNum)
        assertEquals(3, res[2].pageNum)
        assertEquals(43, res[2].topLeft_x)
        assertEquals(180, res[2].topLeft_y)
        assertEquals(20, res[2].height)
        assertEquals(80, res[2].width)

        assertEquals("[МЕСТО ДЛЯ ПОДПИСИ4]", res[3].marker)
        assertEquals(4, res[3].pageNum)
        assertEquals(44, res[3].topLeft_x)
        assertEquals(184, res[3].topLeft_y)
        assertEquals(24, res[3].height)
        assertEquals(84, res[3].width)
        logger.debug { res }
    }

}