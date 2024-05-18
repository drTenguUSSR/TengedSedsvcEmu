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

        requestAttachments.stream().filter { it.logicalName.lowercase().endsWith(".pdf") }.forEach { att ->
            val stamps = processFile(att, req, requestAttachments)
            allStamps.add(stamps)
        }

        return CheckPdfResponse(allStamps)
    }

    private fun processFile(
        attach: UniFileTransfer,
        req: CheckPdfRequest,
        requestAttachments: List<UniFileTransfer>
    ): CheckPdfResponse.CheckStampInfo {
        logger.debug { "working on ${attach.localName}: $attach" }

        val pdfFile = File(attach.localFolder, attach.localName)
        Loader.loadPDF(pdfFile).use { doc ->
            val preflight = PreflightDocument(doc.document, Format.PDF_A1B)
            preflight.context = PreflightContext()
            preflight.context.document = preflight
            if (preflight.isEncrypted) {
                return CheckPdfResponse.CheckStampInfo(
                    attach.logicalName, false,
                    "file encrypted", null, emptyList(), emptyList()
                )
            }
            if (preflight.currentAccessPermission != null && !preflight.currentAccessPermission.canModify()) {
                return CheckPdfResponse.CheckStampInfo(
                    attach.logicalName, false,
                    "can't modify", null, emptyList(), emptyList()
                )
            }
            logger.debug { if (req.skipPDFA1check) "pdf/a-validating:skip" else "pdf/a-validating:exec" }
            if (!req.skipPDFA1check) {
                val checkResult = execPDFA1validating(preflight, attach)
                if (checkResult != null) {
                    return checkResult
                }
            }
            logger.debug { "title=${doc.documentInformation.title}" }
            return decodeToStamps(attach.logicalName, req, requestAttachments)
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

    private fun decodeToStamps(
        logicalName: String,
        req: CheckPdfRequest,
        requestAttachments: List<UniFileTransfer>
    ): CheckPdfResponse.CheckStampInfo {
        if (!req.useFakeLoad) {
            throw IllegalStateException("mode useFakeLoad=false not supported")
        }
        val fakeSrcName = logicalName.substringBeforeLast(".") + ".json"
        val fakeSrcFile =
            requestAttachments.stream().filter { it.logicalName.equals(fakeSrcName, true) }
                .map { File(it.localFolder + File.separator + it.localName) }
                .findAny().orElseThrow()

        val strStamps = fakeSrcFile.readText(Charsets.UTF_8)

        if (strStamps.isNullOrEmpty()) {
            val fileInfo =
                CheckPdfResponse.CheckStampInfo(logicalName, !req.skipPDFA1check, "fake source is empty", null, emptyList(), emptyList())
            return fileInfo
        }
        logger.debug { "fake source ${fakeSrcFile.absolutePath} is loaded" }

        var stamps: List<FakeStampInfo>
        try {
            stamps = SharedData.objMapper.readValue<List<FakeStampInfo>>(strStamps)
        } catch (ex: JsonParseException) {
            logger.debug { "decode error: ${ex.cause} ${ex.message}" }
            val fileInfo = CheckPdfResponse.CheckStampInfo(logicalName, false, ex.message, null, emptyList(), emptyList())
            return fileInfo
        }

        val lookupLabels = hashSetOf(LABEL_SIG, LABEL_REG, *req.markers.toTypedArray())
        logger.debug { "lookupLabels:$lookupLabels" }

        var labelReg: CheckPdfResponse.Info? = null
        val labelsSig = mutableListOf<CheckPdfResponse.Info>()
        val labelsOther = mutableListOf<CheckPdfResponse.Info>()
        logger.debug { "labels-lookup-beg [" }
        stamps.stream().filter { lookupLabels.contains(it.marker) }.forEach { fake ->
            val stampInfo = CheckPdfResponse.Info(fake.marker, fake.pageNum, fake.topLeft_x, fake.topLeft_y, fake.height, fake.width)
            when (fake.marker) {
                LABEL_REG -> labelReg = stampInfo
                LABEL_SIG -> {
                    labelsSig.add(stampInfo)
                    logger.debug { "- labelSig: $stampInfo" }
                }
                else -> labelsOther.add(stampInfo)
            }
        }
        logger.debug { "]" }
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
                        logger.error {
                            "- page:${errOne.pageNumber}. code:${errOne.errorCode}. " +
                                    "detail: ${errOne.details}. cause: ${errOne.cause}"
                        }
                    }
                }
                return CheckPdfResponse.CheckStampInfo(
                    attach.logicalName, false, "validation error - PDF/A error. see server log",
                    null, emptyList(), emptyList()
                )
            }
        } catch (ex: ValidationException) {
            return CheckPdfResponse.CheckStampInfo(
                attach.logicalName, false, "validation error exception: ${ex.message}",
                null, emptyList(), emptyList()
            )
        }
        return null
    }

}