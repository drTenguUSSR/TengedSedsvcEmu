package mil.teng.sedSvcEmuBackEnd.rest

import java.time.Instant
import java.time.LocalDateTime

/**
 * @author DrTengu, 2024/05
 */


// test data
@ContentType("application/json-simple-text")
data class SimpleText(val textA: String, val textB: String, val textC: String?, val dateTime: Instant?, val localDtm: LocalDateTime?)
