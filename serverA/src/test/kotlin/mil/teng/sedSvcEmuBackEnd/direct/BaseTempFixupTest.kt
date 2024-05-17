package mil.teng.sedSvcEmuBackEnd.direct

import io.github.oshai.kotlinlogging.KotlinLogging
import mil.teng.sedSvcEmuBackEnd.rest.SharedData
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File
import kotlin.test.assertEquals

/**
 * @author DrTengu, 2024/05
 */
class BaseTempFixupTest {
    private val logger = KotlinLogging.logger {}

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun checkOneWindows() {
        val valA = File("r:\\uko")
        val valR = SharedData.baseTempFixup(valA)
        logger.debug { "checkOne: !$valA! -> !$valR!" }
        assertEquals("r:\\uko\\", valR)
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    fun checkOneLinux() {
        val valA = File("/tmp/tmp2")
        val valR = SharedData.baseTempFixup(valA)
        logger.debug { "checkOne: !$valA! -> !$valR!" }
        assertEquals("/tmp/tmp2/", valR)
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun replaceBaseB() {
        val base = "r:\\foo\\barSome\\"
        val srcA = File("r:\\foo\\barSome\\folder\\file01.txt")

        val resA = SharedData.makeShort(srcA, base)
        logger.debug { "replaceBaseB:resB=$resA" }
        assertEquals("folder\\file01.txt", resA)
    }

    @ParameterizedTest
    @CsvSource(
        "r:\\foo\\barSome\\folder\\file01.txt|folder\\file01.txt",
        "r:\\foo\\barSome\\folder\\file02-1.txt|folder\\file02-1.txt",
        "r:\\foo\\barSome\\folder\\file03.other.txt|folder\\file03.other.txt",
        "r:\\foo\\barSome\\folder\\file04.dots.txt.|folder\\file04.dots.txt.",
        delimiterString = "|"
    )
    fun replaceOneSimple(src: String, expected: String) {
        val baseTemp = "r:\\foo\\barSome\\"
        assertEquals(expected, SharedData.makeShort(File(src), baseTemp), "with baseTemp=$baseTemp")
    }

    @ParameterizedTest
    @CsvSource(
        "r:\\foo\\bar.some\\folder\\file01.txt|folder\\file01.txt",
        "r:\\foo\\bar.some\\folder\\file02-1.txt|folder\\file02-1.txt",
        "r:\\foo\\bar.some\\folder\\file03.other.txt|folder\\file03.other.txt",
        "r:\\foo\\bar.some\\folder\\file04.dots.txt.|folder\\file04.dots.txt.",
        delimiterString = "|"
    )
    fun replaceOneDotted(src: String, expected: String) {
        val baseTemp = "r:\\foo\\bar.some\\"
        assertEquals(expected, SharedData.makeShort(File(src), baseTemp), "with baseTemp=$baseTemp")
    }

}