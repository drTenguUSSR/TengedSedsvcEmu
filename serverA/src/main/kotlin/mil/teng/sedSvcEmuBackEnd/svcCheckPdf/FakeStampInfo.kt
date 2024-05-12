package mil.teng.sedSvcEmuBackEnd.svcCheckPdf

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * @author DrTengu, 2024/05
 */
//val signB = CheckPdfResponse.Info("[МЕСТО ДЛЯ ПОДПИСИ]", 1, 40, 180, 20, 80)

//@JsonCreator
data class FakeStampInfo(val marker: String, val pageNum: Int, val topLeft_x: Int, val topLeft_y: Int, val height: Int, val width: Int) {
    companion object {
        val objMapper: ObjectMapper

        fun parse(param: String): FakeStampInfo? {
            val res = FakeStampInfo.objMapper.factory.createParser(param.byteInputStream())
                .use { jparser -> FakeStampInfo.objMapper.readValue(jparser, FakeStampInfo::class.java) }
            return res
        }

        init {
            println("FakeStampInfo-init called")
            objMapper = jacksonObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }
}

