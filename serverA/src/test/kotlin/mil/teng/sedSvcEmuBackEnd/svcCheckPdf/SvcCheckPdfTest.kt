package mil.teng.sedSvcEmuBackEnd.svcCheckPdf

import io.github.oshai.kotlinlogging.KotlinLogging
import mil.teng.sedSvcEmuBackEnd.rest.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import kotlin.test.*


/**
 * Простой тест, без контекста
 * @author DrTengu, 2024/05
 */

class SvcCheckPdfTest {

    @Test
    @DisplayName("04.pdf/basic file with no label. error on pdf/a validation")
    fun simplePDFNoLabel() {
        val killList = mutableListOf<File>()
        val srcDirName = "src-" + UUID.randomUUID().toString()
        val resList = listOf<UniFileInfo>(
            UniFileInfo("просто-0157.pdf", "/svcCheckPdf/main2-simple.pdf", PDF_MAIN_2_SUM)
        )

        val uniInfo = prepareResourceTestFiles(srcDirName, resList, killList)

        val requestAttachments = mutableListOf<UniFileTransfer>()
        uniInfo.forEach { (_, uni) -> requestAttachments.add(uni) }
        val requestDats = CheckPdfRequest(listOf("[ПРИМЕР ДОПОЛНИТЕЛЬНОГО ШТАМПА]"), skipPDFA1check = false, useFakeLoad = true)
        val cmdResult = SvcCheckPdf().execute(requestDats, requestAttachments) as CheckPdfResponse

        val resOne = cmdResult.checkStampInfos[0]
        assertEquals("просто-0157.pdf", resOne.fileName)
        assertFalse(resOne.valid)
        logger.debug { "errorMessage:${resOne.errorMessage}" }
        assertContains(resOne.errorMessage ?: "", "validation error - PDF/A error")

        cleanupKillList(killList)
    }

    @Test
    @DisplayName("05.pdf/a file with no label. empty result")
    fun pdfANoLabel() {
        val killList = mutableListOf<File>()
        val srcDirName = "src-" + UUID.randomUUID().toString()
        val resList = listOf<UniFileInfo>(
            UniFileInfo("валидный-0214.pdf", "/svcCheckPdf/main1-pdfA.pdf", PDF_MAIN_1_SUM)
        )

        val uniInfo = prepareResourceTestFiles(srcDirName, resList, killList)

        val requestAttachments = mutableListOf<UniFileTransfer>()
        uniInfo.forEach { (_, uni) -> requestAttachments.add(uni) }
        val requestDats = CheckPdfRequest(listOf("[ПРИМЕР ДОПОЛНИТЕЛЬНОГО ШТАМПА]"), skipPDFA1check = false, useFakeLoad = true)
        val cmdResult = SvcCheckPdf().execute(requestDats, requestAttachments) as CheckPdfResponse

        val resOne = cmdResult.checkStampInfos[0]
        assertEquals("валидный-0214.pdf", resOne.fileName)
        assertTrue(resOne.valid)
        assertNull(resOne.errorMessage)
        assertNull(resOne.regStamp)
        assertTrue { resOne.signStamps.isEmpty() }
        assertTrue { resOne.stampInfos.isEmpty() }

        cleanupKillList(killList)
    }

    @Test
    @DisplayName("10.pdf/a file with full set of labels")
    fun pdfaFullCorrectLabels() {
        val killList = mutableListOf<File>()
        val srcDirName = "src-" + UUID.randomUUID().toString()
        val resList = listOf<UniFileInfo>(
            UniFileInfo(
                "некий-файл.pdf",
                "/svcCheckPdf/main1-pdfA.pdf",
                PDF_MAIN_1_SUM
            ),
            UniFileInfo(
                "некий-файл.json",
                "/svcCheckPdf/10.rss-mver.json",
                "md5:BF2584A8744122B6CA6F9EC9F1044A76,sha1:FF16F37347BBD326E058D3BE74B7B42D8330E92A"
            )
        )

        val uniInfo = prepareResourceTestFiles(srcDirName, resList, killList)

        val requestAttachments = mutableListOf<UniFileTransfer>()
        uniInfo.forEach { (_, uni) -> requestAttachments.add(uni) }
        val requestDats = CheckPdfRequest(listOf("[ПРИМЕР ДОПОЛНИТЕЛЬНОГО ШТАМПА]"), skipPDFA1check = false, useFakeLoad = true)
        val cmdResult = SvcCheckPdf().execute(requestDats, requestAttachments) as CheckPdfResponse

        val resOne = cmdResult.checkStampInfos[0]
        logger.debug { "resOne: $resOne" }
        assertEquals("некий-файл.pdf", resOne.fileName)
        assertTrue(resOne.valid)

        resOne.regStamp.let {
            assertNotNull(it)
            assertEquals("[МЕСТО ДЛЯ ШТАМПА]", it.markerText)
            assertEquals(1, it.pageNum)
            assertEquals(41, it.topLeft_x)
            assertEquals(181, it.topLeft_y)
            assertEquals(21, it.height)
            assertEquals(81, it.width)
        }

        assertNotNull(resOne.signStamps)
        assertEquals(2, resOne.signStamps.size)
        resOne.signStamps[0].let {
            assertEquals("[МЕСТО ДЛЯ ПОДПИСИ]", it.markerText)
            assertEquals(2, it.pageNum)
            assertEquals(42, it.topLeft_x)
            assertEquals(182, it.topLeft_y)
            assertEquals(22, it.height)
            assertEquals(82, it.width)
        }
        resOne.signStamps[1].let {
            assertEquals("[МЕСТО ДЛЯ ПОДПИСИ]", it.markerText)
            assertEquals(3, it.pageNum)
            assertEquals(43, it.topLeft_x)
            assertEquals(183, it.topLeft_y)
            assertEquals(23, it.height)
            assertEquals(83, it.width)
        }

        assertNotNull(resOne.stampInfos)
        assertTrue { resOne.stampInfos.isEmpty() }

        cleanupKillList(killList)
    }

//        assertContains(resOne.errorMessage?:"","'subj'", message = resOne.errorMessage)
//        assertContains(resOne.errorMessage?:"","JSON String, Number, Array", message=resOne.errorMessage)
    /**
     * Подготавливает тестовые файлы из тест-ресурсов
     * @return map<логичесое имя,UniFileTransfer>
     */
    private fun prepareResourceTestFiles(
        srcDirName: String,
        resFiles: List<UniFileInfo>,
        modifiedKillList: MutableList<File>
    ): Map<String, UniFileTransfer> {
        val resultInfo = mutableMapOf<String, UniFileTransfer>()
        val srcFolder = makeTempSubfolder(srcDirName)
        modifiedKillList.add(srcFolder)

        for (resOne in resFiles) {
            val uuKey = UUID.randomUUID().toString()

            val origin = File(resOne.origin)
            val localFile = File(srcFolder, "$uuKey.${origin.extension}")

            clazz.getResourceAsStream(resOne.resourceName).use { fis ->
                localFile.outputStream().use { fos ->
                    fis?.transferTo(fos)
                        ?: throw IllegalStateException("failed to read ${resOne.resourceName} for write ${localFile.absolutePath}")
                }
            }
            modifiedKillList.add(localFile)
            assertEquals(
                resOne.sum, FileControlInfo(localFile).sums, "fail on copy ${resOne.origin} " +
                        "from ${resOne.resourceName} to ${localFile.absolutePath}"
            )
            val datFile = UniFileTransfer(origin.name, srcFolder.absolutePath, localFile.name)
            resultInfo[resOne.origin] = datFile
        }

        return resultInfo
    }

    /**
     * Данные для подготовки файлов к "загрузке" (переформатирование в List<UniFileTransfer>)
     * параметры [origin], [resourceName], [sum]
     *     @param origin - логическое имя файла
     *     @param resourceName - физическое имя ресурса от "src\test\resources",
     *          например "/svcCheckPdf/10.a-rss-mver.pdf"
     *     @param sum - контрольные суммы. например "md5:73...5B,sha1:A07...D9"
     */
    data class UniFileInfo(val origin: String, val resourceName: String, val sum: String)

    companion object {
        private val logger = KotlinLogging.logger {}
        private val clazz = this::class.java

        const val PDF_MAIN_1_SUM = "md5:733AAEA4B394C996A139B6974D62365B,sha1:A0789F101D1BC41B69597B86E412BF8FB17012D9"
        const val PDF_MAIN_2_SUM = "md5:687B96D2E1E9C39B53ECBB39CE9FCB01,sha1:22E1EC5C8DEFCA3C8955F6F5AB4E545FFF13D528"

        private fun cleanupKillList(killList: MutableList<File>) {
            logger.debug { "clean-up-after:${SharedData.getKillListInfo(killList)}" }
            assertTrue { killList[0].exists() && killList[0].isDirectory }
            for (dat in killList.reversed()) {
                val m1 = SharedData.checkedFileDelete(dat)
                logger.debug { m1 }
            }
            assertFalse { killList[0].exists() }
        }

    }
}