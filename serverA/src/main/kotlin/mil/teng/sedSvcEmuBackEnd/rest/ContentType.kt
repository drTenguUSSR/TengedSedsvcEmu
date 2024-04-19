package mil.teng.sedSvcEmuBackEnd.rest

import java.lang.annotation.Inherited

/**
 * @author DrTengu, 2024/04
 */

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class ContentType(val value: String)
