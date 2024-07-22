package mil.teng.sedSvcEmuBackEnd

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mil.teng.sedSvcEmuBackEnd.rest.AbstractCommandProcessor
import mil.teng.sedSvcEmuBackEnd.rest.CommandsBeanInfo
import mil.teng.sedSvcEmuBackEnd.rest.MainDataConvertor
import mil.teng.sedSvcEmuBackEnd.rest.RelationLink
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

/**
 * Unit-тест RootController. Контекст не поднимается
 * @author DrTengu, 2024/07
 */

@ExtendWith(MockitoExtension::class)
class RootControllerTest {

    val logger = KotlinLogging.logger {}

    private var BASIC_NAME_RELATION_BASE = "http://intertrust.ru/cmj/sedsvc/relations"

    @Mock
    private lateinit var commandsBeanInfo: CommandsBeanInfo

    @Mock
    private lateinit var mainDataConvertor: MainDataConvertor

    @InjectMocks
    private lateinit var rootController: RootController

    @BeforeEach
    fun init() {
        logger.debug { "init-called. two relation init" }
        val cmdCheckPdf = Mockito.mock(AbstractCommandProcessor::class.java)
        Mockito.`when`(cmdCheckPdf.commandName).thenReturn("svcCheckPdf")
        Mockito.`when`(cmdCheckPdf.commandRelationSuffix).thenReturn("#checkPdf")

        val cmdRegSignStamp = Mockito.mock(AbstractCommandProcessor::class.java)
        Mockito.`when`(cmdRegSignStamp.commandName).thenReturn("svcRegSignStamp")
        Mockito.`when`(cmdRegSignStamp.commandRelationSuffix).thenReturn("#makeRegSignStamp")

        val mapRel = mapOf(
            cmdCheckPdf.commandName.lowercase() to BASIC_NAME_RELATION_BASE + cmdCheckPdf.commandRelationSuffix,
            cmdRegSignStamp.commandName.lowercase() to BASIC_NAME_RELATION_BASE + cmdRegSignStamp.commandRelationSuffix
        )
        Mockito.`when`(commandsBeanInfo.mapRelations).thenReturn(mapRel)

        //@formatter:off
        val mapBeans = mapOf(
            cmdCheckPdf.commandName.lowercase() to cmdCheckPdf
            , cmdRegSignStamp.commandName.lowercase() to cmdRegSignStamp
        )
        //@formatter:on
        Mockito.`when`(commandsBeanInfo.mapBeans).thenReturn(mapBeans)
    }

    @Test
    fun getRoot_returnValidRelList() {
        logger.debug { "getRoot-called" }
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.requestURL).thenReturn(StringBuffer("http://localhost:8071/api"))
        val response = Mockito.mock(HttpServletResponse::class.java)

        val resp = rootController.getRoot(request, response)

        Assertions.assertEquals(2, resp.entry.size)
        rootEntityCheck(
            resp.entry[0],
            "http://intertrust.ru/cmj/sedsvc/relations#checkPdf",
            "http://localhost:8071/api/execRequest?command=svccheckpdf"
        )
        rootEntityCheck(
            resp.entry[1],
            "http://intertrust.ru/cmj/sedsvc/relations#makeRegSignStamp",
            "http://localhost:8071/api/execRequest?command=svcregsignstamp"
        )

    }

    private fun rootEntityCheck(relationLink: RelationLink, rel: String, href: String) {
        Assertions.assertEquals(relationLink.rel, rel)
        Assertions.assertEquals(relationLink.href, href)
    }

}
