package mil.teng.sedSvcEmuBackEnd.svcCheckPdf

import io.github.oshai.kotlinlogging.KotlinLogging
import mil.teng.sedSvcEmuBackEnd.rest.*
import org.apache.pdfbox.preflight.Format
import org.apache.pdfbox.preflight.PreflightDocument
import org.apache.pdfbox.preflight.exception.SyntaxValidationException
import org.apache.pdfbox.preflight.exception.ValidationException
import org.apache.pdfbox.preflight.parser.PreflightParser
import org.springframework.stereotype.Service
import java.io.File
import kotlin.math.log

/**
 * @author DrTengu, 2024/04
 */

@Service
class SvcCheckPdf(
    override val commandName: String = "svcCheckPdf",
    override val commandRelationSuffix: String = "#checkPdf"
) : AbstractCommandProcessor {

    val logger = KotlinLogging.logger {}

    override fun execute(request: CommonResourceRequest, requestAttachments: List<UniFileTransfer>): CommonResourceResponse {
        logger.warn { "executed. called for request.class=${request::class.java.canonicalName}" }
        logger.debug { "got-attachments:$requestAttachments" }
        val req = request as CheckPdfRequest

        if (requestAttachments.isEmpty()) {
            throw IllegalArgumentException("no source files")
        }

        val allStamps = mutableListOf<CheckPdfResponse.CheckStampInfo>()

        for (attach in requestAttachments) {
            if (!req.skipPDFA1check) {
//TODO: избежать двойного парсинга
            }
            val stamps = processFile(attach)
            allStamps.add(stamps)
        }

        return CheckPdfResponse(allStamps)
    }

    private fun processFile(attach: UniFileTransfer): CheckPdfResponse.CheckStampInfo {
        logger.debug { "working on ${attach.localName} from ${attach.logicalName}" }
        val res = mutableListOf<CheckPdfResponse.CheckStampInfo>()


        val pdfFile = File(attach.localFolder, attach.localName)
        val parser = PreflightParser(pdfFile)

        try {
            parser.parse(Format.PDF_A1B).use {
                val preflight = it as PreflightDocument
                if (preflight.isEncrypted) {
                    return CheckPdfResponse.CheckStampInfo(
                        attach.logicalName, false,
                        "file encrypted", null, null, null
                    )
                }
                if (preflight.currentAccessPermission != null && !preflight.currentAccessPermission.canModify()) {
                    return CheckPdfResponse.CheckStampInfo(
                        attach.logicalName, false,
                        "can't modify", null, null, null
                    )
                }

                try {
                    val validateResult = preflight.validate()
                    if (!validateResult.isValid) {
                        validateResult.errorsList?.let { errLst ->
                            logger.error { "found errors for file ${attach.localName} from ${attach.logicalName}. count=${errLst.size} " }
                            errLst.forEach { errOne ->
                                logger.error { "- ${errOne.pageNumber}: ${errOne.details}" }
                            }
                        }
                        return CheckPdfResponse.CheckStampInfo(
                            attach.logicalName, false, "validation error - PDF/A error. see server log",
                            null, null, null
                        )
                    }
                } catch (ex: ValidationException) {
                    return CheckPdfResponse.CheckStampInfo(
                        attach.logicalName, false, "validation error exception: ${ex.message}",
                        null, null, null
                    )
                }

                val docInfo = preflight.documentInformation
                logger.debug { "title=${docInfo.title}"}
                logger.debug { "keywords=${docInfo.keywords}" }
                logger.debug { "subj=${docInfo.subject}" }
                logger.debug { "keys=${docInfo.metadataKeys}" }
                

            }
        } catch (ex: SyntaxValidationException) {
            val message = "page=${ex.pageNumber} ${ex.cause?.message}. (${ex::class.java.canonicalName})"
            logger.error { "SyntaxValidationException: $message" }
            return CheckPdfResponse.CheckStampInfo(
                attach.logicalName, false, "SyntaxValidationException: $message",
                null, null, null
            )
        }


        val stampReg = CheckPdfResponse.Info(
            "[МЕСТО ДЛЯ ШТАМПА]", 1, 15, 15, 5, 66
        )
        val signA = CheckPdfResponse.Info("[МЕСТО ДЛЯ ПОДПИСИ]", 1, 40, 150, 20, 80)
        val signB = CheckPdfResponse.Info("[МЕСТО ДЛЯ ПОДПИСИ]", 1, 40, 180, 20, 80)
        val fileInfo = CheckPdfResponse.CheckStampInfo(
            attach.logicalName, true, null, stampReg, listOf(signA, signB), null
        )

        return fileInfo
    }
}