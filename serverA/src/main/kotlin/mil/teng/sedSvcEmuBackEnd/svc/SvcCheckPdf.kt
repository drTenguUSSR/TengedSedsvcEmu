package mil.teng.sedSvcEmuBackEnd.svc

import io.github.oshai.kotlinlogging.KotlinLogging
import mil.teng.sedSvcEmuBackEnd.rest.AbstractCommandProcessor
import mil.teng.sedSvcEmuBackEnd.rest.CheckPdfResponse
import mil.teng.sedSvcEmuBackEnd.rest.CommonResourceRequest
import mil.teng.sedSvcEmuBackEnd.rest.UnifiedResult
import org.springframework.stereotype.Service

/**
 * @author DrTengu, 2024/04
 */

@Service
class SvcCheckPdf(
    override val commandName: String = "svcCheckPdf",
    override val commandRelationSuffix: String = "#checkPdf"
) : AbstractCommandProcessor {

    val logger = KotlinLogging.logger {}

    override fun execute(request: CommonResourceRequest): UnifiedResult {
        logger.warn { "executed. called for request.class=${request::class.java.canonicalName}" }
        val stamps = mutableListOf<CheckPdfResponse.CheckStampInfo>()

        val stampReg = CheckPdfResponse.Info(
            "[МЕСТО ДЛЯ ШТАМПА]", 1, 15, 15, 5, 66
        )
        val signA = CheckPdfResponse.Info("[МЕСТО ДЛЯ ПОДПИСИ]", 1, 40, 150, 20, 80)
        val signB = CheckPdfResponse.Info("[МЕСТО ДЛЯ ПОДПИСИ]", 1, 40, 180, 20, 80)

        var fileResult = CheckPdfResponse.CheckStampInfo("example-svcCheckPdf.pdf", true, null, stampReg, listOf(signA, signB), null)
        val objResult = CheckPdfResponse(listOf(fileResult))
        return UnifiedResult(objResult, null)
    }
}