package mil.teng.sedSvcEmuBackEnd.svcCheckPdf

import io.github.oshai.kotlinlogging.KotlinLogging
import mil.teng.sedSvcEmuBackEnd.rest.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


/**
 * Простой тест, без контекста
 * @author DrTengu, 2024/05
 */

class SvcCheckPdfTest {
    private val logger = KotlinLogging.logger {}
    private val clazz = this::class.java

    @Test
    @DisplayName("04.pdf/basic file with no label. error on field decode")
    fun simpleNoLabel() {
        val killList = mutableListOf<File>()
        val srcDirName = "src-" + UUID.randomUUID().toString()
        val resList =
           mapOf("/svcCheckPdf/04.z-text1-z.pdf" to "md5:687B96D2E1E9C39B53ECBB39CE9FCB01,sha1:22E1EC5C8DEFCA3C8955F6F5AB4E545FFF13D528")


        val uniInfo = prepareResourceTestFiles(srcDirName, resList, killList)

        val requestAttachments = mutableListOf<UniFileTransfer>()
        uniInfo.forEach { (_, uni) -> requestAttachments.add(uni) }

        val requestDats = CheckPdfRequest(listOf("[ПРИМЕР ДОПОЛНИТЕЛЬНОГО ШТАМПА]"), true)

        val cmdResult = SvcCheckPdf().execute(requestDats, requestAttachments) as CheckPdfResponse

        val resOne = cmdResult.checkStampInfos[0]
        assertEquals("fake-04.z-text1-z.pdf",resOne.fileName)
        assertFalse(resOne.valid )
        assertContains(resOne.errorMessage?:"","'subj'")
        assertContains(resOne.errorMessage?:"","JSON String, Number, Array")

        logger.debug { "clean-up-after:${SharedData.getKillListInfo(killList)}" }
        assertTrue { killList[0].exists() && killList[0].isDirectory }
        for (dat in killList.reversed()) {
            val m1 = SharedData.checkedFileDelete(dat)
            logger.debug { m1 }
        }
        assertFalse { killList[0].exists() }
    }

    @Test
    @DisplayName("10.pdf/a file with full set of labels")
    fun pdfaFullCorrectLabels() {
        val killList = mutableListOf<File>()
        val srcDirName = "src-" + UUID.randomUUID().toString()
        val resList =            mapOf(
            "/svcCheckPdf/10.a-rss-mver.pdf" to "md5:733AAEA4B394C996A139B6974D62365B,sha1:A0789F101D1BC41B69597B86E412BF8FB17012D9",
            //"/svcCheckPdf/10.a-rss-mver.json" to "md5:BF2584A8744122B6CA6F9EC9F1044A76,sha1:FF16F37347BBD326E058D3BE74B7B42D8330E92A"
        )

        val uniInfo = prepareResourceTestFiles(srcDirName, resList, killList)

        val requestAttachments = mutableListOf<UniFileTransfer>()
        uniInfo.forEach { (_, uni) -> requestAttachments.add(uni) }

        val requestDats = CheckPdfRequest(listOf("[ПРИМЕР ДОПОЛНИТЕЛЬНОГО ШТАМПА]"), false)

        val cmdResult = SvcCheckPdf().execute(requestDats, requestAttachments) as CheckPdfResponse

        val resOne = cmdResult.checkStampInfos[0]
        logger.debug { "resOne: ${resOne}" }
        assertEquals("fake-10.a-rss-mver.pdf",resOne.fileName)
        assertFalse(resOne.valid )

//        assertContains(resOne.errorMessage?:"","'subj'", message = resOne.errorMessage)
//        assertContains(resOne.errorMessage?:"","JSON String, Number, Array", message=resOne.errorMessage)

        logger.debug { "clean-up-after:${SharedData.getKillListInfo(killList)}" }
        assertTrue { killList[0].exists() && killList[0].isDirectory }
        for (dat in killList.reversed()) {
            val m1 = SharedData.checkedFileDelete(dat)
            logger.debug { m1 }
        }
        assertFalse { killList[0].exists() }
    }


    /**
     * Подготовливает тестоые файлы из тест-ресурсов
     */
    fun prepareResourceTestFiles(
        srcDirName: String,
        resFiles: Map<String, String>,
        modifiedKillList: MutableList<File>
    ): Map<String, UniFileTransfer> {
        val resultInfo = mutableMapOf<String, UniFileTransfer>()
        val srcFolder = makeTempSubfolder(srcDirName)
        modifiedKillList.add(srcFolder)

        for (resOne in resFiles) {
            val originName = resOne.key
            val originSums = resOne.value

            val uuKey = UUID.randomUUID().toString()

            val origin = File(originName)
            val localFile = File(srcFolder, "$uuKey.${origin.extension}")

            clazz.getResourceAsStream(originName).use { fis ->
                localFile.outputStream().use { fos ->
                    fis.transferTo(fos)
                }
            }
            modifiedKillList.add(localFile)
            assertEquals(originSums, FileControlInfo(localFile).sums, "fail on copy $originName to ${localFile.absolutePath}")
            val datFile = UniFileTransfer("fake-${origin.name}", srcFolder.absolutePath, localFile.name)
            resultInfo[originName] = datFile
        }

        return resultInfo
    }
}