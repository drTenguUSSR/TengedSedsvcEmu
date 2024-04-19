package mil.teng.sedSvcEmuBackEnd.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

/**
 * @author DrTengu, 2024/04
 */

@Service
class ContentTypeDataService(@Value("\${sedSvc.simpleDebug:false}") private val svcSimpleDebug: Boolean) {
    val logger = KotlinLogging.logger {}
    private lateinit var class2media: Map<KClass<*>, MediaType>
    private lateinit var medias: Set<MediaType>
    private lateinit var contentTypes2class: Map<String, Class<*>>

    @PostConstruct
    fun init() {
        logger.debug { "ContentTypeDataService - beg. svcSimpleDebug=$svcSimpleDebug" }
        val class2media = mutableMapOf<KClass<*>, MediaType>()
        val contentTypes2class = mutableMapOf<String, Class<*>>()
        val medias = mutableSetOf<MediaType>()
        val classScan = ClassPathScanningCandidateComponentProvider(false)
        classScan.addIncludeFilter(AnnotationTypeFilter(ContentType::class.java))
        for (beanDefinition in classScan.findCandidateComponents(BASE_TYPE_PACKAGE)) {
            val curClass = Class.forName(beanDefinition.beanClassName)
            val currContentType = curClass.getAnnotation(ContentType::class.java)
            if (currContentType.value.isEmpty()) continue
            val mt = MediaType.parseMediaType(currContentType.value)
            logger.debug { "for class [${curClass.canonicalName}] key=[${curClass.kotlin}] found media-type as [$mt]" }
            class2media[curClass.kotlin] = mt
            contentTypes2class[currContentType.value] = curClass
            medias.add(mt)
        }
        this.class2media = class2media.toMap()
        this.medias = medias.toSet()
        this.contentTypes2class = contentTypes2class.toMap()
        logger.debug { "ContentTypeDataService - end" }
    }

    fun makeSedSvcResponse(res: CommonResource): ResponseEntity<CommonResource> {
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(class2media[res::class] ?: DEFAULT_MEDIA_TYPE)
            .body(res)
    }

    /**
     * возвращение полного списка поддерживаемых MediaType
     */
    fun getAllMedias(): Set<MediaType> = medias

    /**
     * возвращает конкретный MediaType для переданного класса
     */
    fun class2mediaType(clazz: KClass<*>): MediaType? = class2media[clazz]

    /**
     * перепаковка MediaType с выбрасыванием всех параметров, кроме type
     * например из "application/json-special;charset=UTF-8" будет получено
     * "application/json-special"
     */
    fun mediaType2contentType(mt: MediaType): String = "${mt.type}/${mt.subtype}" + (mt.getParameter("type")?.let { ";type=$it" } ?: "")

    fun class2contentType(clazz: KClass<*>): String {
        var mt = class2media[clazz] ?: DEFAULT_MEDIA_TYPE
        return mediaType2contentType(mt)
    }

    /**
     * возвращает class для конкретного ContentType
     */
    fun contentType2Class(ct: String): Class<*>? = contentTypes2class[ct]

    companion object {
        private const val BASE_TYPE_PACKAGE = "mil.teng.sedSvcEmuBackEnd.rest"
        const val defaultContentType = "application/json"
        val DEFAULT_MEDIA_TYPE = MediaType.parseMediaType(defaultContentType)
    }
}