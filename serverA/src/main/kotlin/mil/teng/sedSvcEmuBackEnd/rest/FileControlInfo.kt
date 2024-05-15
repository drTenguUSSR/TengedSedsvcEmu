package mil.teng.sedSvcEmuBackEnd.rest

import java.io.File
import java.io.FileInputStream
import java.lang.IllegalArgumentException
import java.security.MessageDigest

/**
 * @author DrTengu, 2024/05
 */
class FileControlInfo(localFile: File) {
    //TODO: make test for FileControlInfo
    //        val sx1= FileControlInfo(File("wrong1"))
    //        logger.error { "sx1=$sx1" }
    private val buffer: ByteArray = ByteArray(FILE_BUFFER_LEN)
    private val sumMd5: MessageDigest = MessageDigest.getInstance("MD5")
    private val sumExt: MessageDigest = MessageDigest.getInstance("SHA1")

    init {
        if (!localFile.exists() || !localFile.isFile || !localFile.canRead()) {
            throw IllegalArgumentException("problem with file=${localFile.absolutePath}")
        }
    }

    val short: String by lazy {
        "len:$localFile.length(),modif:${localFile.lastModified()}"
    }

    @OptIn(ExperimentalStdlibApi::class)
    val sums:String by lazy {
        FileInputStream(localFile).use { fis ->
            var count = fis.read(buffer, 0, FILE_BUFFER_LEN)
            while (count != -1) {
                sumMd5.update(buffer, 0, count)
                sumExt.update(buffer, 0, count)
                count = fis.read(buffer, 0, FILE_BUFFER_LEN)
            }
            val resMd5 = sumMd5.digest().toHexString(HexFormat.UpperCase)
            val resExt = sumExt.digest().toHexString(HexFormat.UpperCase)
            "md5:$resMd5,sha1:$resExt"
        }
    }

    val full:String by lazy {
        "$short,$sums"
    }

    companion object {
        private const val FILE_BUFFER_LEN = 4096
    }
}