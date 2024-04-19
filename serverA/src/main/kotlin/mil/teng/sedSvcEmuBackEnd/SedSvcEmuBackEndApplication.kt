package mil.teng.sedSvcEmuBackEnd

import io.github.oshai.kotlinlogging.KotlinLogging
import mil.teng.sedSvcEmuBackEnd.rest.AbstractCommandProcessor
import mil.teng.sedSvcEmuBackEnd.rest.CommandsBeanInfo
import mil.teng.sedSvcEmuBackEnd.rest.CommonResource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.web.filter.CharacterEncodingFilter


@SpringBootApplication
class SedSvcEmuBackEndApplication {
    @Bean
    fun commandProcessors(applicationContext: ApplicationContext): CommandsBeanInfo {
        loggerTop.debug { "commandProcessors-beg" }
        var allBeans = applicationContext.getBeansOfType(AbstractCommandProcessor::class.java)
        val mapBeans = HashMap<String, AbstractCommandProcessor>(allBeans.size)
        val mapRelations = HashMap<String, String>(allBeans.size)
        val relationsCheck = HashSet<String>(allBeans.size)
        for (bean in allBeans) {
            val beanKey=bean.value.commandName.lowercase()
            loggerTop.debug { "- command=${beanKey}, beanName=${bean.key}, beanClass=${bean.value::class.java.canonicalName}" }
            mapBeans.put(beanKey, bean.value)?.let { throw IllegalStateException("duplicate bean name=${bean.value.commandName}") }
            mapRelations[beanKey] = CommonResource.Companion.BASIC_NAME_RELATION_BASE+bean.value.commandRelationSuffix
            if (!relationsCheck.add(bean.value.commandRelationSuffix)) {
                throw IllegalStateException("dublicate relation=${bean.value.commandRelationSuffix}")
            }
        }
        loggerTop.debug { "commandProcessors-end" }
        return CommandsBeanInfo(mapBeans, mapRelations)
    }


}

val loggerTop = KotlinLogging.logger {}
const val showBeanListOnBoot = false

fun main(args: Array<String>) {
    loggerTop.error { "ERR-main-start" }
    var ctx = runApplication<SedSvcEmuBackEndApplication>(*args)
    if (showBeanListOnBoot) {
        showBeanListOnBoot(ctx)
    }
    loggerTop.error { "проверка консоли" }
    loggerTop.error { "ERR-main-end" }
}

fun showBeanListOnBoot(ctx: ConfigurableApplicationContext) {
    var beanNames = ctx.beanDefinitionNames
    loggerTop.debug { "bean-list-beg" }
    for (beanName in beanNames) {
        var beanData = ctx.getBean(beanName)
        loggerTop.debug { "- bean: $beanName as ${beanData::class.java.canonicalName}" }
    }
    loggerTop.debug { "bean-list-end" }
}
