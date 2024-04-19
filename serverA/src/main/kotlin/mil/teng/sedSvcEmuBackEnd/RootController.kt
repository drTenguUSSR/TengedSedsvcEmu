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
import java.time.Instant
import java.time.LocalDateTime

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
    fun getRoot(response: HttpServletResponse): RestLinkCollection {
        logger.debug { "getRoot called" }
        val links = mutableListOf<RelationLink>()
        val res = RestLinkCollection("sedSvcEmu root title", links)
        for (i1 in 1..10) {
            val rel = CommonResource.BASIC_NAME_RELATION_BASE + "relations#word2pdf"
            val href = "http://localhost:8070/ext-sedsvc/upload-m/?command=$i1"
            val title = "title-$i1"
            val link = RelationLink(rel, href, title)
            links.add(link)
        }
        //TODO: use special-annotation for CORS
        response.addHeader("Access-Control-Allow-Origin", "*")
        logger.debug { "getRoot end" }

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
    )  {
        logger.debug { "execRequest-beg" }
        logger.debug { "execRequest-params [" }
        logger.debug { "- command=$paramCommand" }
        logger.debug { "- ct=$contentType" }
        logger.debug { "- mainData=[" }
        logger.debug { "$mainData" }
        logger.debug { "- ]" }
        logger.debug { "files size=${files?.size} [" }
        files?.let {
            for (itemF in files) {
                logger.debug { "- ${itemF.name}, len=${itemF.size}, type=${itemF.contentType}, origin=$itemF" }
            }
        }
        logger.debug { "]" }

        val attachments = listOf<UniFileTransfer>()
        //TODO: пропускаем файлы. переделать на выгрузку во временнную папку как List<UniFileTransfer> attachments

        val mainDataObj = mainDataConvertor.readExt(mainData, contentType)
        logger.debug { "mainDataObj: $mainDataObj" }

        logger.debug { "selected-bean:${commandsBeanInfo.mapBeans[paramCommand.lowercase()]}" }
        val result = commandsBeanInfo.mapBeans[paramCommand.lowercase()]?.let { it.execute(mainDataObj) } ?: run {
            val msg = "unexpected command=$paramCommand. commandList=${commandsBeanInfo.mapBeans.keys}"
            logger.error { msg }
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, msg)
        }
        mainDataConvertor.writeResponse(result,response)
        logger.debug { "execRequest-end" }
    }

    @GetMapping("/test-a")
    fun getTestA(): SimpleText {
        val res = SimpleText(
            "textA1 123 123 123 123 123 123 123 123 123 123 123 123 123 123 123 123 123 endA",
            "textB1 123 123 123 123 123 123 123 123 123 123 123 123 123 123 123 123 123 endB",
            null,
            Instant.now(),
            LocalDateTime.now()
        )
        logger.debug { "getTestA: return res=[$res]" }
        return res
    }

    @PostMapping("/test-a")
    fun postTestA(@RequestBody requestData: SimpleText): SimpleText {
        logger.debug { "postTestA:!$requestData!" }
        val res = SimpleText("txtA", "txtB", "txtC", null, null)
        return res;
    }
}

