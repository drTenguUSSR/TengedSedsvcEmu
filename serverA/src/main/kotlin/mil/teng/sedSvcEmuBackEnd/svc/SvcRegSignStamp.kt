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

    override fun execute(request: CommonResourceRequest): UnifiedResult {
        logger.warn { "executed. called for request.class=${request::class.java.canonicalName}" }
        var objResult = RegSignStampResponse(null, null)
        val files = mutableListOf<UniFileTransfer>()
        val resultFolder = makeTempSubfolder("reg-result-")
        val fileReg="reg.png"
        fakeCopy(fileReg, "example-reg-sign-stamp", resultFolder)
        val fileSig="reg.png"
        fakeCopy(fileSig, "example-reg-sign-stamp", resultFolder)

        val resFileReg = UniFileTransfer(fileReg, resultFolder.getName(), fileReg)
        files.add(resFileReg)
        val resFileSig = UniFileTransfer(fileSig, resultFolder.getName(), fileSig)
        files.add(resFileSig)

        return UnifiedResult(objResult, files)
    }

    private fun fakeCopy(fileName: String, srcFolder: String, resultFolder: File) {
        FileOutputStream(File(resultFolder, fileName)).use { fos ->
            clazz.getResourceAsStream("/$srcFolder/$fileName")!!.copyTo(fos)
        }
        val dat1 = File(resultFolder, fileName)
        logger.debug { "fakeCopy: $fileName -> ${dat1.absolutePath}, size=${dat1.length()}" }
    }

    private fun makeTempSubfolder(folderSuffix: String): File {
        val folder = File(getTempFolder(), folderSuffix + System.currentTimeMillis())
        if (folder.exists()) {
            throw RuntimeException("folder ${folder.absolutePath} must not exist")
        }
        val chkFolder = folder.mkdirs()
        if (!chkFolder) {
            throw RuntimeException("failed to create ${folder.absolutePath}")
        }
        logger.debug { "temp folder ${folder.absolutePath} created" }
        return folder
    }

    private fun getTempFolder(): File {
        val res = File(System.getProperty("java.io.tmpdir"))
        logger.debug { "using temp=${res.absolutePath}" }
        return res
    }
}