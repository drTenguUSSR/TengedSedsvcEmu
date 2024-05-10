package mil.teng.sedSvcEmuBackEnd.svc

import io.github.oshai.kotlinlogging.KotlinLogging
import mil.teng.sedSvcEmuBackEnd.rest.*
import org.springframework.stereotype.Service
import java.io.*

/**
 * @author DrTengu, 2024/04
 */

@Service
class SvcRegSignStamp(
    override val commandName: String = "svcRegSignStamp",
    override val commandRelationSuffix: String = "#makeRegSignStamp"
) : AbstractCommandProcessor {

    val logger = KotlinLogging.logger {}
    val clazz = this::class.java

    override fun execute(request: CommonResourceRequest, requestAttachments: List<UniFileTransfer>): CommonResourceResponse {
        logger.warn { "executed. called for request.class=${request::class.java.canonicalName}" }
        logger.warn { "creating images files, based on request parameters. fake for now" }

        val resultFolder = makeTempSubfolder("reg-result-")
        val fileReg = "reg.png"
        fakeCopy(fileReg, "example-reg-sign-stamp", resultFolder)
        val fileSig = "sign.png"
        fakeCopy(fileSig, "example-reg-sign-stamp", resultFolder)

        var attachments = mutableListOf<UniFileTransfer>()
        attachments.add(UniFileTransfer(fileReg, resultFolder.name, fileReg))
        attachments.add(UniFileTransfer(fileSig, resultFolder.name, fileSig))
        var objResult = RegSignStampResponse(null, attachments)
        logger.debug { "execute. files=$attachments" }
        return objResult
    }

    private fun fakeCopy(fileName: String, srcFolder: String, resultFolder: File) {
        FileOutputStream(File(resultFolder, fileName)).use { fos ->
            clazz.getResourceAsStream("/$srcFolder/$fileName")!!.copyTo(fos)
        }
        val dat1 = File(resultFolder, fileName)
        logger.debug { "fakeCopy: $fileName -> ${dat1.absolutePath}, size=${dat1.length()}" }
    }

}