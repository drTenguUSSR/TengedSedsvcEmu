package mil.teng.sedSvcEmuBackEnd

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.Resource
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mil.teng.sedSvcEmuBackEnd.rest.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.time.Instant
import java.util.*
import kotlin.text.HexFormat

@RestController
@RequestMapping("/api")
class RootController(
    private val mainDataConvertor: MainDataConvertor,
    @Resource private val commandsBeanInfo: CommandsBeanInfo
) : CommonController() {

    val logger = KotlinLogging.logger {}

    @GetMapping("/text-a")
    fun getTextA(): String {
        val msg = Instant.now().toString()
        logger.debug { "getTextA. called for $msg" }
        return "hello $msg"
    }

    @GetMapping
    fun getRoot(request: HttpServletRequest, response: HttpServletResponse): RestLinkCollection {
        val baseUrl = request.requestURL.toString()
        logger.debug { "getRoot called. request.url=${baseUrl}" }
        val links = mutableListOf<RelationLink>()

        commandsBeanInfo.mapRelations.forEach { (cmdKey, cmdVal) ->
            logger.debug { "- iterate: key=!$cmdKey! val=!$cmdVal!" }
            val relSuffix = commandsBeanInfo.mapBeans[cmdKey]?.commandRelationSuffix
                ?: throw IllegalStateException("mapBeans for key=$cmdKey not found")
            val relFull = CommonResource.BASIC_NAME_RELATION_BASE + relSuffix
            val href = "$baseUrl/execRequest?command=$cmdKey"
            val title = "title for $cmdKey $cmdVal"
            val link = RelationLink(relFull, href, title)
            links.add(link)

        }

        //TODO: use special-annotation for CORS
        response.addHeader("Access-Control-Allow-Origin", "*")
        logger.debug { "getRoot end" }

        val res = RestLinkCollection("sedSvcEmu root title", links)
        return res
    }

    @PostMapping("/execRequest")
    fun execRequest(
        @RequestParam("command") paramCommand: String,
        @RequestParam contentType: String,
        @RequestParam mainData: String,
        @RequestParam("file") files: List<MultipartFile>?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        logger.debug { "execRequest-beg" }
        logger.debug { "execRequest-params [" }
        logger.debug { "- command=$paramCommand" }
        logger.debug { "- ct=$contentType" }
        logger.debug { "- mainData=[" }
        logger.debug { "$mainData" }
        logger.debug { "- ]" }
        logger.debug { "files size=${files?.size} [" }
        val attachments = mutableListOf<UniFileTransfer>()

        files?.let {
            val srcFolder = makeTempSubfolder("src-")
            logger.debug { "created src folder as '$srcFolder'" }
            val buffer = ByteArray(FILE_BUFFER_LEN)
            val sumMd5 = MessageDigest.getInstance("MD5")
            val sumExt = MessageDigest.getInstance("SHA1")
            for (itemF in files) {
                val uuKey = UUID.randomUUID().toString()

                logger.debug { "- ${itemF.name}, len=${itemF.size}, type=${itemF.contentType}, origin='${itemF.originalFilename}'" }
                val originFileName = itemF.originalFilename ?: "data$uuKey.bin"
                val originExt = File(originFileName).extension

                val localFile = File(srcFolder, "$uuKey.$originExt")
                logger.debug { "- - use localFile=$localFile" }

                itemF.transferTo(localFile)

                logger.debug { "- - ${calcFileCheck(localFile, buffer, sumMd5, sumExt)}" }

                val uniDat = UniFileTransfer(originFileName, srcFolder.absolutePath, localFile.name)
                attachments.add(uniDat)
            }
        }
        logger.debug { "]" }

        val mainDataObj = mainDataConvertor.readExt(mainData, contentType)
        logger.debug { "mainDataObj: $mainDataObj" }

        logger.debug { "selected-bean:${commandsBeanInfo.mapBeans[paramCommand.lowercase()]}" }
        val result = commandsBeanInfo.mapBeans[paramCommand.lowercase()]?.let { it.execute(mainDataObj, attachments.toList()) } ?: run {
            val msg = "unexpected command=$paramCommand. commandList=${commandsBeanInfo.mapBeans.keys}"
            logger.error { msg }
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, msg)
        }
        mainDataConvertor.writeResponse(result, response)
        logger.debug { "execRequest-end" }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun calcFileCheck(localFile: File, buffer: ByteArray, sumMd5: MessageDigest, sumExt: MessageDigest): String {
        sumMd5.reset()
        sumExt.reset()
        FileInputStream(localFile).use { fis ->
            var count = fis.read(buffer, 0, FILE_BUFFER_LEN)
            while (count != -1) {
                sumMd5.update(buffer, 0, count)
                sumExt.update(buffer, 0, count)
                count = fis.read(buffer, 0, FILE_BUFFER_LEN)
            }
            val resMd5 = sumMd5.digest().toHexString(HexFormat.UpperCase)
            val resExt = sumExt.digest().toHexString(HexFormat.UpperCase)
            return "md5:$resMd5,sha1:$resExt"
        }
    }

    companion object {
        private const val FILE_BUFFER_LEN = 4096
    }

}

