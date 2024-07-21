package mil.teng.sedSvcEmuBackEnd.temp

import mil.teng.sedSvcEmuBackEnd.rest.CommonResourceResponse
import mil.teng.sedSvcEmuBackEnd.rest.UniFileTransfer
import mil.teng.sedSvcEmuBackEnd.temp.RestResp.SomeResponseDto
import kotlin.random.Random

/**
 * unused for prod code
 * @author DrTengu, 2024/04
 */

fun main() {
    xlog("beg")
    val atts = mutableListOf<UniFileTransfer>()
    atts.add(UniFileTransfer("logi-1", "folder-1", "local-1"))
    atts.add(UniFileTransfer("logi-2", "folder-1", "local-2"))
    atts.add(UniFileTransfer("logi-3", "folder-1", "local-3"))
    atts.add(UniFileTransfer("logi-4", "folder-1", "local-4"))
    val dataA = SomeResponseDto("xA", "xB", atts)
    xlog("dataA=$dataA")
    xlog("a=${dataA.memberA}")
    xlog("files-1=${dataA.fileHrefLink}")
    xlog("files-2=${dataA.fileHrefLink}")
    xlog("end")
}

fun xlog(msg: String) {
    println(msg)
}

class RestResp {

    interface Response_HasAttachments {
        val attachments: List<UniFileTransfer>?
        val fileHrefLink: List<CommonResourceResponse.Name2Href>? get() = convertAttachments(attachments)

        fun convertAttachments(attachments: List<UniFileTransfer>?): List<CommonResourceResponse.Name2Href>? {
            val rand = Random.nextInt(100, 200)
            xlog("get-convertor called($rand). attachments=" + (attachments?.let { it.size } ?: "null-array"))
            if (attachments == null) {
                return null
            }
            val res = mutableListOf<CommonResourceResponse.Name2Href>()

            attachments.forEachIndexed { index, attach ->
                xlog("iterate index=$index. ${attach.logicalName}")
                val fileInfo =
                    CommonResourceResponse.Name2Href(attach.logicalName, "folder-$rand/${attach.localFolder}/${attach.localName}")
                res.add(fileInfo)
            }

            return res.toList()
        }
    }

    data class SomeResponseDto(
        val infoA: String, val infoB: String,
        override val attachments: List<UniFileTransfer>?,
    ) : Response_HasAttachments {
        val memberA: String = "memberA"
    }
}

interface DataOne {
    val dataOne: String
    val dataTwo: String
        get() = "1"
    val dataThree: String
        get() = dataOne + "!!!" + convert(dataTwo)

    val attachs: List<String>
    val hrefs: MutableList<CommonResourceResponse.Name2Href>

    fun dataHrefInit() {
        for (att in attachs) {
            var dat = CommonResourceResponse.Name2Href("logical-$att", "href-$att")
            hrefs.add(dat)
        }

    }

    fun convert(s1: String): String {
        return "converted!$s1!"
    }
}
