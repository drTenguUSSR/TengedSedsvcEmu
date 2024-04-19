package mil.teng.sedSvcEmuBackEnd.java4refactor

import jakarta.mail.internet.MimeUtility
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.http.HttpServletResponse
import java.io.IOException
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * @author DrTengu, 2024/04
 */
class MultipartResponse(var res: HttpServletResponse) {
    private val boundary: String
    var out: ServletOutputStream = res.outputStream
    var endedLastPart: Boolean = true

    /**
     * начинает выводить в исходищий поток
     * заголовки, начинающие multipart-ответ.
     * каждая часть начинается с вызова
     * и заканчивается вызовом [.partEnd]
     * все сообщение заканчивается вызовом [.finish]
     *
     * @param response ответ
     * @throws IOException исключение
     */
    init {
        boundary = generateBoundary()
        res.contentType = "multipart/mixed;boundary=$boundary"
        out.print("$CRLF--$boundary$CRLF")
    }

    /**
     * начало записи данных в виде части MultipartResponse
     *
     * @param contentType - тип передаваемого контента (если не указан - запишется "application/octet-stream")
     * @param attachName - (опционально) заполняется именем файла, если передается файл
     * @param contentLength (для пропуска опции - указать '-1') длина контента
     */
    fun partBegin(contentType: String, attachName: String?, contentLength: Long) {
        if (!endedLastPart) {
            partEnd()
        }

        var ct = contentType
        if (ct.isEmpty()) {
            ct = BINARY_DATA
        }
        out.print("Content-Type: $ct$CRLF")

        if (attachName!=null) {
            out.print("Content-Disposition: attachment; filename=\"${MimeUtility.encodeWord(attachName)}\"$CRLF")
        }

        if (contentLength >= 0) {
            val str = "Content-Length: $contentLength"
            out.print(str + CRLF)
        }
        out.print(CRLF)
        endedLastPart = false
    }

    /**
     * записывает в поток данные об окончании текущей части
     */
    fun partEnd() {
        out.print("$CRLF--$boundary$CRLF")
        out.flush()
        endedLastPart = true
    }

    /**
     * завершение работы с данным MultipartResponse
     *
     * @throws IOException исключение
     */
    @Throws(IOException::class)
    fun finish() {
        out.print("--$boundary--$CRLF")
        out.flush()
    }

    private fun generateBoundary(): String {
        val boundaryMaxLen = 69 // https://tools.ietf.org/html/rfc2046#section-5.1.1, Appendix A
        val prefixLen = "NextPart-".length
        val countMax = boundaryMaxLen - prefixLen
        val buffer = StringBuilder(prefixLen + countMax + 10)
        buffer.append("NextPart-")
        val rand: Random = ThreadLocalRandom.current()
        // final int count = rand.nextInt(11) + 30; // a random size from 30 to 40
        for (i in 0 until countMax) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.size)])
        }
        return buffer.toString()
    }

    companion object {
        private const val CRLF = "\u000D\u000A"
        private val MULTIPART_CHARS = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
        const val BINARY_DATA: String = "application/octet-stream"
    }
}
