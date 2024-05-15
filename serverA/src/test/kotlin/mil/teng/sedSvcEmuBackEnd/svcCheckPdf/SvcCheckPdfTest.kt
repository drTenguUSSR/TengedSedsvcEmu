package mil.teng.sedSvcEmuBackEnd.svcCheckPdf

import io.github.oshai.kotlinlogging.KotlinLogging
import mil.teng.sedSvcEmuBackEnd.rest.FileControlInfo
import mil.teng.sedSvcEmuBackEnd.rest.makeTempSubfolder
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.DisplayName
import java.io.File
import java.io.FileInputStream
import java.lang.IllegalArgumentException
import java.security.MessageDigest
import java.util.*
import kotlin.test.assertEquals
import kotlin.text.HexFormat

/**
 * Простой тест, без контекста
 * @author DrTengu, 2024/05
 */

class SvcCheckPdfTest {
    val logger = KotlinLogging.logger {}
    val clazz = this::class.java

    @Test
    @DisplayName("pdf/basic file with no label. no error with no check")
    fun checkSimplePdfNL() {
        val srcName = "/svcCheckPdf/simple-nl.pdf"
        val srcCheck="md5:687B96D2E1E9C39B53ECBB39CE9FCB01,sha1:22E1EC5C8DEFCA3C8955F6F5AB4E545FFF13D528"
        val srcDir = "src-" + UUID.randomUUID().toString()
        logger.debug { "checkSimplePdfNL: dir=$srcDir srcName=$srcName" }
        val srcFolder = makeTempSubfolder(srcDir)
        val uuKey = UUID.randomUUID().toString()

        val originExt = File(srcName).extension
        val localFile = File(srcFolder, "$uuKey.$originExt")

        clazz.getResourceAsStream(srcName).use { fis ->
            localFile.outputStream().use { fos ->
                fis.transferTo(fos)
            }
        }
        assertEquals(srcCheck,FileControlInfo(localFile).sums)
//TODO: exec fake query
    }
}