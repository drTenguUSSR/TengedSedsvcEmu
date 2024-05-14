package mil.teng.sedSvcEmuBackEnd.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.http.HttpServletResponse
import mil.teng.sedSvcEmuBackEnd.java4refactor.MultipartResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.stereotype.Service
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*


/**
 * @author DrTengu, 2024/04
 */

@Service
class DtoConverter(
    //TODO: миграция на единый ObjectMapper
    mapper: ObjectMapper,
    private val svcContentType: ContentTypeDataService,
    @Value("\${sedSvc.simpleDebug:false}") private val svcSimpleDebug: Boolean
) : HttpMessageConverter<Any>, MainDataConvertor {
    val logger = KotlinLogging.logger {}
    val mapper: ObjectMapper = mapper.copy()

    @PostConstruct
    fun initComp() {
        logger.debug { "initComp called" }
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        // только для обратной совместимости
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    override fun canRead(clazz: Class<*>, mediaType: MediaType?): Boolean {
        logger.debug { "canRead. called for [$clazz] with mt.origin=[$mediaType]" }
        if (mediaType == null) {
            return false
        }
        val ct2 = svcContentType.mediaType2contentType(mediaType)
        val res = svcContentType.contentType2Class(ct2)
        logger.debug { "canRead. ct2=[$ct2], res.class=$res" }
        return res != null
    }

    override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean {
        val wMediaType = svcContentType.class2mediaType(clazz.kotlin)
        logger.debug { "canWrite. called for [$clazz] with mt.origin=[$mediaType]  -> $wMediaType" }
        return wMediaType != null
    }

    override fun getSupportedMediaTypes() = svcContentType.getAllMedias().toMutableList()

    override fun write(obj: Any, mediaType: MediaType?, outputMessage: HttpOutputMessage) {
        val wMediaType = svcContentType.class2mediaType(obj::class) ?: ContentTypeDataService.DEFAULT_MEDIA_TYPE
        logger.debug { "write. called for obj.class=${obj::class} mediaType=$mediaType -> mt=$wMediaType" }
        val dataObjStream = serialize(obj)
        val curHeaders = outputMessage.headers
        curHeaders.contentType = wMediaType
        curHeaders.contentLength = dataObjStream.size().toLong()
        val dataOutStream = outputMessage.body
        dataObjStream.writeTo(dataOutStream)
        dataOutStream.flush()
    }

    fun serialize(obj: Any): ByteArrayOutputStream {
        val dataObjStream = ByteArrayOutputStream(BUFFER_SIZE)
        try {
            mapper.factory.createGenerator(dataObjStream, JsonEncoding.UTF8).use { jgen -> mapper.writeValue(jgen, obj) }
        } catch (ex: Exception) {
            throw RuntimeException("json write error", ex)
        }
        return dataObjStream
    }

    override fun read(clazz: Class<out Any>, inputMessage: HttpInputMessage): Any {
        logger.debug { "read. called for class=${clazz.canonicalName}" }

        val mt = inputMessage.headers.contentType ?: throw RuntimeException("contentType is empty")
        val ct2 = svcContentType.mediaType2contentType(mt)
        val wClazz = svcContentType.contentType2Class(ct2) ?: throw RuntimeException("unexpected ct2=$ct2 for mt=$mt")
        logger.debug { "read. mt=[$mt] -> ct2=[$ct2] -> $wClazz" }
        val dataObjStream = inputMessage.body
        try {
            val res = mapper.factory.createParser(dataObjStream).use { jparser -> mapper.readValue(jparser, wClazz) }
            logger.debug { "read. res.class=${res::class.java.canonicalName} data: $res" }
            return res
        } catch (ex: Exception) {
            throw RuntimeException("json write error", ex)
        }
    }

    companion object {
        private const val BUFFER_SIZE = 4 * 1024
    }

    override fun readExt(mainData: String, contentType: String): CommonResourceRequest {
        logger.debug { "readExt. called for contentType=$contentType" }
        if (mainData.isEmpty()) {
            throw IllegalArgumentException("mainData must be not empty")
        }
        if (contentType.isEmpty()) {
            throw IllegalArgumentException("contentType must be not empty")
        }
        if (svcSimpleDebug) {
            val dataBt = mainData.toByteArray(StandardCharsets.UTF_8)
            val dataEnc = Base64.getEncoder().encodeToString(dataBt)
            logger.warn { "request.mainData:[\n$dataEnc\n]" }
        }
        val clazz = svcContentType.contentType2Class(contentType) ?: throw IllegalArgumentException("can't find class for ct=$contentType")

        val res = mapper.factory.createParser(mainData).use { jparser -> mapper.readValue(jparser, clazz) }
        logger.debug { "readExt. res.class=${res::class.java.canonicalName} data: $res" }
        return res as CommonResourceRequest
    }


    override fun writeResponse(dataResp: CommonResourceResponse, response: HttpServletResponse) {
        logger.debug { "writeResponse beg. dataResp=$dataResp" }
        val mpr = MultipartResponse(response)
        val out = response.outputStream

        val contentStream: ByteArrayOutputStream = serialize(dataResp)

        val ctMain = svcContentType.class2contentType(dataResp::class)
        mpr.partBegin(ctMain, null, contentStream.size().toLong())
        contentStream.writeTo(out)
        mpr.partEnd()
        if (dataResp is CommonResourceResponse.HasAttachments && dataResp.attachments != null && dataResp.attachments!!.isNotEmpty()) {

            for (attachment in dataResp.attachments!!) {
                val ctFile = "application/octet-stream"
                val dataFile = File(getTempFolder(), attachment.localFolder + "/" + attachment.localName)
                if (!dataFile.exists()) {
                    throw IllegalStateException("failed file ${dataFile.absolutePath} not exits")
                }
                val fileLen = dataFile.length()
                logger.debug { "file: ${dataFile.absolutePath} len=$fileLen" }
                mpr.partBegin(ctFile, attachment.localName, fileLen)
                BufferedInputStream(FileInputStream(dataFile)).use { fis -> fis.copyTo(out) }
                mpr.partEnd()
            }
        }
        mpr.finish()
    }
}