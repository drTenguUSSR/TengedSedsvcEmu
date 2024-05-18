package mil.teng.sedSvcEmuBackEnd.rest

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.servlet.http.HttpServletResponse
import mil.teng.sedSvcEmuBackEnd.loggerTop
import java.io.File
import java.util.*

/**
 * @author DrTengu, 2024/04
 */

//Root response info
@ContentType("application/vnd.intertrust.cm.collection+json;type=link")
class RestLinkCollection(
    val title: String,
    val entry: List<RelationLink>
) : CommonResourceResponse

class RelationLink(val rel: String, val href: String, val title: String = "no-title")

// Word2pdf request-response

@ContentType("application/vnd.intertrust.cm.sedsvc+json;type=word2pdf-response")
class Word2PdfResponse(
    val nameConversion: Map<String, String>,
    @JsonIgnore
    override val attachments: List<UniFileTransfer>,
    override val fileHrefLink: List<CommonResourceResponse.Name2Href>?
) : CommonResourceResponse, CommonResourceResponse.HasAttachments

// svcCheckPdf request-response
@ContentType("application/vnd.intertrust.cm.sedsvc+json;type=svcCheckPdf-request")
data class CheckPdfRequest(
    val markers: List<String>,
    val skipPDFA1check: Boolean,
    val useFakeLoad: Boolean,
    override val doZip: Boolean = false
) : CommonResourceRequest

@ContentType("application/vnd.intertrust.cm.sedsvc+json;type=svcCheckPdf-response")
data class CheckPdfResponse(
    val checkStampInfos: List<CheckStampInfo>
) : CommonResourceResponse {
    data class CheckStampInfo(
        val fileName: String,
        val valid: Boolean,
        val errorMessage: String?,
        val regStamp: Info?,
        val signStamps: List<Info>,
        val stampInfos: List<Info>
    )

    class Info(val markerText: String, pageNum: Int, topLeft_x: Int, topLeft_y: Int, height: Int, width: Int) :
        StampInfo(pageNum, topLeft_x, topLeft_y, height, width) {
        override fun toString(): String {
            return "Info(markerText='$markerText',"+super.toString()+")"
        }
    }
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
class RegSignStampResponse(
    val errors: List<Errors>?,
    @JsonIgnore
    override val attachments: List<UniFileTransfer>?
) : CommonResourceResponse, CommonResourceResponse.HasAttachments {

    data class Errors(val id: String, val errorMessage: String)
}

// common request-response info
/**
 * Информация о всех командных бинах в приложении
 * @param mapBeans - отображение command на конкретный bean (для маршрутизации запросов)
 * @param mapRelations - отображение command на конкретный relation (для построения корневого ответа)
 */
data class CommandsBeanInfo(val mapBeans: Map<String, AbstractCommandProcessor>, val mapRelations: Map<String, String>)

open class StampInfo(val pageNum: Int, val topLeft_x: Int, val topLeft_y: Int, val height: Int, val width: Int) {
    override fun toString(): String {
        return "StampInfo(pageNum=$pageNum, topLeft_x=$topLeft_x, topLeft_y=$topLeft_y, height=$height, width=$width)"
    }
}

/**
 * @param logicalName - логическое имея. Могут присутствовать некорректные символы
 * @param localFolder - локальная папка на сервере, где хранится файл (полный путь к папке)
 * @param localName - фактическое имя файла на диске
 */
data class UniFileTransfer(val logicalName: String, val localFolder: String, val localName: String)

/**
 * Тип, который должны имплементировать все бины, чтобы отображаться в entryPoint
 */
interface AbstractCommandProcessor {
    /**
     * Человеко-читаемое имя бина. Отображается в entry-point
     */
    val commandName: String

    /**
     * Суффикс relation-ссылки. Отображается в entry-point
     */
    val commandRelationSuffix: String

    /**
     * Главная функция командного бина, который выполняет задачу
     */
    fun execute(request: CommonResourceRequest, requestAttachments: List<UniFileTransfer>): CommonResourceResponse
}

interface CommonResourceResponse : CommonResource {
    interface HasAttachments {
        val attachments: List<UniFileTransfer>?
        val fileHrefLink: List<Name2Href>? get() = convertAttachments(attachments)

        private fun convertAttachments(attachments: List<UniFileTransfer>?): List<Name2Href>? {
            loggerTop.debug { "convertAttachments-beg" }
            if (attachments == null) {
                return null
            }
            val res = mutableListOf<Name2Href>()

            attachments.forEachIndexed { index, attach ->
                loggerTop.debug { "iterate index=$index. ${attach.logicalName}" }
                val fileInfo = Name2Href(attach.logicalName, attach.localName)
                res.add(fileInfo)
            }
            loggerTop.debug { "convertAttachments-end" }
            return res.toList()
        }

    }

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
    fun writeResponse(dataResp: CommonResourceResponse, response: HttpServletResponse)
}

fun getTempFolder(): File = File(System.getProperty("java.io.tmpdir"))

/**
 * Создает временную папку и возвращает её.
 */
fun makeTempSubfolder(folderSuffix: String): File {
    //TODO: use kotlin.io.path.createTempDirectory() ?
    val folder = File(getTempFolder(), folderSuffix + System.currentTimeMillis())
    if (folder.exists()) {
        throw RuntimeException("folder ${folder.absolutePath} must not exist")
    }
    val chkFolder = folder.mkdirs()
    if (!chkFolder) {
        throw RuntimeException("failed to create ${folder.absolutePath}")
    }
    loggerTop.debug { "temp folder ${folder.absolutePath} created" }
    return folder
}