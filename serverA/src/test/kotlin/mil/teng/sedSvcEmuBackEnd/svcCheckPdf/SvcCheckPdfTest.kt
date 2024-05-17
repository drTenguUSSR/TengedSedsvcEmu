package mil.teng.sedSvcEmuBackEnd.svcCheckPdf

import io.github.oshai.kotlinlogging.KotlinLogging
import mil.teng.sedSvcEmuBackEnd.rest.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import kotlin.test.assertEquals


/**
 * Простой тест, без контекста
 * @author DrTengu, 2024/05
 */

class SvcCheckPdfTest {
    private val logger = KotlinLogging.logger {}
    private val clazz = this::class.java

    @Test
    @DisplayName("pdf/basic file with no label. no error with no check")
    fun checkSimplePdfNL() {
        val killList = mutableListOf<File>()
        val srcName = "/svcCheckPdf/simple-nl.pdf"
        val srcCheck = "md5:687B96D2E1E9C39B53ECBB39CE9FCB01,sha1:22E1EC5C8DEFCA3C8955F6F5AB4E545FFF13D528"
        val srcDirName = "src-" + UUID.randomUUID().toString()
        logger.debug { "checkSimplePdfNL: dir=$srcDirName srcName=$srcName" }
        val srcFolder = makeTempSubfolder(srcDirName)
        killList.add(srcFolder)
        val uuKey = UUID.randomUUID().toString()

        val originExt = File(srcName).extension
        val localFile = File(srcFolder, "$uuKey.$originExt")

        clazz.getResourceAsStream(srcName).use { fis ->
            localFile.outputStream().use { fos ->
                fis.transferTo(fos)
            }
        }
        killList.add(localFile)
        assertEquals(srcCheck, FileControlInfo(localFile).sums)

        val srcFile = UniFileTransfer("fake-1$originExt", srcFolder.absolutePath, localFile.name)
        val requestDats = CheckPdfRequest(listOf("[ПРИМЕР ДОПОЛНИТЕЛЬНОГО ШТАМПА]"), true)
        val requestAttachments = listOf<UniFileTransfer>(srcFile)
        val cmdResult = SvcCheckPdf().execute(requestDats, requestAttachments) as CheckPdfResponse

        logger.debug { "result-class:${cmdResult::class.java}" }
        logger.debug { "result-info:$cmdResult" }

        logger.debug { "clean-up-after:${SharedData.getKillListInfo(killList)}" }
        for (dat in killList.reversed()) {
            val m1 = SharedData.checkedFileDelete(dat)
            logger.debug { m1 }
        }
    }
}