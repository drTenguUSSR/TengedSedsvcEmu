package mil.teng.sedSvcEmuBackEnd.svcCheckPdf

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import mil.teng.sedSvcEmuBackEnd.rest.*
import org.apache.pdfbox.Loader
import org.apache.pdfbox.preflight.Format
import org.apache.pdfbox.preflight.PreflightContext
import org.apache.pdfbox.preflight.PreflightDocument
import org.apache.pdfbox.preflight.exception.ValidationException
import org.springframework.stereotype.Service
import java.io.File


/**
 * @author DrTengu, 2024/04
 */

@Service
class SvcCheckPdf(
    override val commandName: String = "svcCheckPdf",
    override val commandRelationSuffix: String = "#checkPdf"
) : AbstractCommandProcessor {
    data class FakeStampInfo(val marker: String, val pageNum: Int, val topLeft_x: Int, val topLeft_y: Int, val height: Int, val width: Int)
    companion object {
        const val LABEL_REG = "[МЕСТО ДЛЯ ШТАМПА]"
        const val LABEL_SIG = "[МЕСТО ДЛЯ ПОДПИСИ]"
    }

    val logger = KotlinLogging.logger {}

    override fun execute(request: CommonResourceRequest, requestAttachments: List<UniFileTransfer>): CommonResourceResponse {
        logger.debug { "executed. called for request.class=${request::class.java.canonicalName} got-attachments:$requestAttachments" }
        val req = request as CheckPdfRequest

        if (requestAttachments.isEmpty()) {
            throw IllegalArgumentException("no source files")
        }

        val allStamps = mutableListOf<CheckPdfResponse.CheckStampInfo>()

        for (attach in requestAttachments) {
            val stamps = processFile(attach, req)
            allStamps.add(stamps)
        }

        return CheckPdfResponse(allStamps)
    }

    private fun processFile(attach: UniFileTransfer, req: CheckPdfRequest): CheckPdfResponse.CheckStampInfo {
        logger.debug { "working on ${attach.localName} from ${attach.logicalName}" }

        val pdfFile = File(attach.localFolder, attach.localName)
        Loader.loadPDF(pdfFile).use { doc ->
            val preflight = PreflightDocument(doc.document, Format.PDF_A1B)
            preflight.context = PreflightContext()
            preflight.context.document = preflight
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
            logger.debug { if (req.skipPDFA1check) "pdf/a-validating:skip" else "pdf/a-validating:exec" }
            if (!req.skipPDFA1check) {
                val checkResult = execPDFA1validating(preflight, attach)
                if (checkResult != null) {
                    return checkResult
                }
            }
            val docSubj = doc.documentInformation.subject
            logger.debug { "subj=$docSubj" }
            return decodeToStamps(attach.logicalName, docSubj, req)
        }

//        val stampReg = CheckPdfResponse.Info(
//            "[МЕСТО ДЛЯ ШТАМПА]", 1, 15, 15, 5, 66
//        )
//        val signA = CheckPdfResponse.Info("[МЕСТО ДЛЯ ПОДПИСИ]", 1, 40, 150, 20, 80)
//        val signB = CheckPdfResponse.Info("[МЕСТО ДЛЯ ПОДПИСИ]", 1, 40, 180, 20, 80)
//        val fileInfo = CheckPdfResponse.CheckStampInfo(
//            attach.logicalName, true, null, stampReg, listOf(signA, signB), null
//        )
//        return fileInfo
    }

    private fun decodeToStamps(logicalName: String, docSubj: String?, req: CheckPdfRequest): CheckPdfResponse.CheckStampInfo {
        if (docSubj.isNullOrEmpty()) {
            val fileInfo = CheckPdfResponse.CheckStampInfo(logicalName, !req.skipPDFA1check, null, null, emptyList(), null)
            return fileInfo
        }

        var stamps: List<FakeStampInfo>
        try {
            stamps = SharedData.objMapper.readValue<List<FakeStampInfo>>(docSubj)
        } catch (ex: JsonParseException) {
            logger.debug { "decode error: ${ex.cause} ${ex.message}" }
            val fileInfo = CheckPdfResponse.CheckStampInfo(logicalName, false, ex.message, null, emptyList(), null)
            return fileInfo
        }

        val lookupLabels = hashSetOf(LABEL_SIG, LABEL_REG, *req.markers.toTypedArray())
        logger.debug { "lookupLabels:$lookupLabels" }

        var labelReg: CheckPdfResponse.Info? = null
        val labelsSig = mutableListOf<CheckPdfResponse.Info>()
        val labelsOther = mutableListOf<CheckPdfResponse.Info>()
        stamps.stream().filter { lookupLabels.contains(it.marker) }.forEach { fake ->
            val stampInfo = CheckPdfResponse.Info(fake.marker, fake.pageNum, fake.topLeft_x, fake.topLeft_y, fake.height, fake.width)
            when (fake.marker) {
                LABEL_REG -> labelReg = stampInfo
                LABEL_SIG -> labelsSig.add(stampInfo)
                else -> labelsOther.add(stampInfo)
            }
        }
//        for (fake in stamps) {
//            if (lookupLabels.contains(fake.marker)) {
//                val stampInfo = CheckPdfResponse.Info(fake.marker, fake.pageNum, fake.topLeft_x, fake.topLeft_y, fake.height, fake.width)
//                when (fake.marker) {
//                    LABEL_REG -> label_reg = stampInfo
//                    LABEL_SIG -> labels_sig.add(stampInfo)
//                    else -> labels_other.add(stampInfo)
//                }
//            }
//        }
        val fileInfo = CheckPdfResponse.CheckStampInfo(logicalName, !req.skipPDFA1check, null, labelReg, labelsSig, labelsOther)
        return fileInfo
    }

    /**
     * Выполняет проверку переданного распарсенного PDF/A-1 документа
     * @return null, если нет ошибок или ошибку (в CheckPdfResponse.CheckStampInfo)
     */
    private fun execPDFA1validating(preflight: PreflightDocument, attach: UniFileTransfer): CheckPdfResponse.CheckStampInfo? {
        try {
            val validateResult = preflight.validate()
            if (!validateResult.isValid) {
                validateResult.errorsList?.let { errLst ->
                    logger.error { "found errors for file ${attach.localName} from ${attach.logicalName}. count=${errLst.size} " }
                    errLst.forEach { errOne ->
                        logger.error { "- page:${errOne.pageNumber}. code:${errOne.errorCode}. " +
                                "detail: ${errOne.details}. cause: ${errOne.cause}" }
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
        return null
    }

}