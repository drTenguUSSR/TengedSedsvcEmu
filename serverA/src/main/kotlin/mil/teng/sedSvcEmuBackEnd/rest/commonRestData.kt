package mil.teng.sedSvcEmuBackEnd.rest

import jakarta.servlet.http.HttpServletResponse
import java.time.Instant
import java.time.LocalDateTime
import java.util.Date


/**
 * @author DrTengu, 2024/04
 */

//Root response info
@ContentType(RestLinkCollection.contentType)
class RestLinkCollection(
    val title: String,
    val entry: List<RelationLink>,
    override val fileLinks: List<CommonResourceResponse.Name2Href>? = null
) : CommonResourceResponse {
    companion object {
        const val contentType = "application/vnd.intertrust.cm.collection+json;type=link"
    }
}

class RelationLink(val rel: String, val href: String, val title: String = "no-title")

// Word2pdf request-response

@ContentType("application/vnd.intertrust.cm.sedsvc+json;type=word2pdf-response")
class Word2PdfResponse(
    val nameConversion: Map<String, String>,
    //? @JsonIgnore
    val attachments: List<UniFileTransfer>, //?
    override val fileLinks: List<CommonResourceResponse.Name2Href>?
) : CommonResourceResponse

// svcCheckPdf request-response
@ContentType("application/vnd.intertrust.cm.sedsvc+json;type=svcCheckPdf-request")
class CheckPdfRequest(val markers: List<String>, val skipPDFA1check: Boolean, override val doZip: Boolean = false) : CommonResourceRequest

@ContentType("application/vnd.intertrust.cm.sedsvc+json;type=svcCheckPdf-response")
class CheckPdfResponse(
    val checkStampInfos: List<CheckStampInfo>,
    override val fileLinks: List<CommonResourceResponse.Name2Href>? = null
) : CommonResourceResponse {
    data class CheckStampInfo(
        val fileName: String,
        val valid: Boolean,
        val errorMessage: String?,
        val regStamp: Info?,
        val signStamps: List<Info>?,
        val stampInfos: List<Info>?
    )

    class Info(val markerText: String, pageNum: Int, topLeft_x: Int, topLeft_y: Int, height: Int, width: Int) :
        StampInfo(pageNum, topLeft_x, topLeft_y, height, width)
}

// svcRegSignStamp request-response
@ContentType("application/vnd.intertrust.cm.sedsvc+json;type=svcRegSignStamp-request")
class RegSignStampRequest(
    val regStampDatas: List<RegStamp>?, val signStampDatas: List<SignStamp>?,
    val simpleSignStampDatas: List<SimpleSignStamp>?, val simpleRegStampDatas: List<SimpleRegStamp>?,
    override val doZip: Boolean = false
) : CommonResourceRequest {

    data class RegStamp(
        val id: String, val height: Int, val width: Int, val date: Date, val number: String,
        val font: String?, val fontSize: Int?
    )

    data class SignStamp(
        val id: String, val height: Int, val width: Int, val certificate: String, val signer: String,
        val validFrom: Date, val validTo: Date
    )

    data class SimpleSignStamp(val id: String, val height: Int, val width: Int, val signer: String, val date: Date)

    data class SimpleRegStamp(val id: String, val height: Int, val width: Int, val text: String)
}

@ContentType("application/vnd.intertrust.cm.sedsvc+json;type=svcMakeStamp-response")
class RegSignStampResponse(val errors: List<Errors>?, override val fileLinks: List<CommonResourceResponse.Name2Href>?) :
    CommonResourceResponse {

    data class Errors(val id: String, val errorMessage: String)
}

// test data
@ContentType("application/json-simple-text")
data class SimpleText(val textA: String, val textB: String, val textC: String?, val dateTime: Instant?, val localDtm: LocalDateTime?)

// common request-response info
/**
 * информация о всех командных бинах в приложении
 * @param mapBeans - отображение command на конкретный bean (для маршрутизации запросов)
 * @param mapRelations - отображение command на конкретный relation (для построения корневого ответа)
 */
data class CommandsBeanInfo(val mapBeans: Map<String,AbstractCommandProcessor>, val mapRelations: Map<String,String>)

/**
 * общий тип данных, который возвращают все командные бины
 */
data class UnifiedResult(val mainData: CommonResourceResponse, val attachments: List<UniFileTransfer>?)

open class StampInfo(val pageNum: Int, val topLeft_x: Int, val topLeft_y: Int, val height: Int, val width: Int)

/**
 * @param logicalName - логическое имея. могут присутствовать некоректные символы
 * @param localFolder - локальная папка на сервере, где хранится файл
 * @param localName - фактическое имя файла на диске
 */
data class UniFileTransfer(val logicalName: String, val localFolder: String, val localName: String) //?? localFolder

interface AbstractCommandProcessor {
    val commandName: String
    val commandRelationSuffix: String
    fun execute(request: CommonResourceRequest): UnifiedResult
}

interface CommonResourceResponse : CommonResource {
    val fileLinks: List<Name2Href>?

    data class Name2Href(val logicalName: String, val href: String)
}

interface CommonResourceRequest : CommonResource {
    val doZip: Boolean
}

interface CommonResource {
    companion object {
        const val BASIC_NAME_RELATION_BASE = "http://intertrust.ru/cmj/sedsvc/relations"
    }
}

interface MainDataConvertor {
    fun readExt(mainData: String, contentType: String): CommonResourceRequest
    fun writeResponse(domObj: UnifiedResult, response: HttpServletResponse)
}
