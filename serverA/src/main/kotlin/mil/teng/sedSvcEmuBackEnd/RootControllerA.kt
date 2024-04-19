package mil.teng.sedSvcEmuBackEnd

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletResponse
import mil.teng.sedSvcEmuBackEnd.rest.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author DrTengu, 2024/04
 */

@RestController
@RequestMapping("/api-a")
class RootControllerA(private val contentTypeDataService: ContentTypeDataService) : CommonController() {

    val logger = KotlinLogging.logger {}

    @GetMapping
    fun getRoot(response: HttpServletResponse): ResponseEntity<CommonResource> {
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

        return contentTypeDataService.makeSedSvcResponse(res)
    }
}