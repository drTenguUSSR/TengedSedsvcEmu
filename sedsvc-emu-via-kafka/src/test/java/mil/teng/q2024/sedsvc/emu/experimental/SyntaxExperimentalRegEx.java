/*
 *
 * @author DrTengu. 2024/09
 */

package mil.teng.q2024.sedsvc.emu.experimental;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.regex.Pattern;

public class SyntaxExperimentalRegEx {
    private static final String PAT_COMMA_CHECK = "^[a-z.]+$";

    /*
    ',' (запятая) - в параметрах теста не работает
     */
    @ParameterizedTest
    @CsvSource({
            "adbc",
            //"ad~bc", //fail test
            "ad.bc",
            //"ad!bc", //fail test
            "ab,cd" //run as example="ab"
    })
    void commaCheckA(String example) {
        System.out.println("checkDots - beg. example=[" + example + "]");
        Pattern pat = Pattern.compile(PAT_COMMA_CHECK);
        Assertions.assertTrue(pat.matcher(example).matches());
        System.out.println("checkDots - end");
    }

    @Test
    void commaCheckB() {
        String example = "ad,bc";
        System.out.println("commaCheckB: checkDots - beg [" + example + "]");
        Pattern pat = Pattern.compile(PAT_COMMA_CHECK);
        Assertions.assertFalse(pat.matcher(example).matches());
        System.out.println("checkDots - end");
    }

    /**
     * проверка regExp без ограничителей \\^ и \\$
     * в выражении
     * - корректная работа - patB.matcher(example).matches()
     *      требует полного совпадения образци и паттерна
     * - некорректная работа - patB.matcher(example).find()
     *      для получения true достаточно что в образце
     *      есть подстрока, соответствующая патерну
     */
    @ParameterizedTest
    @CsvSource({
            "abc",
            "abc7",
            "7abc",
    })
    void textLimit(String example) {
        String patBase = "[a-z.]+";
        Pattern patA = Pattern.compile("^" + patBase + "$");
        Pattern patB = Pattern.compile(patBase);
        StringBuilder sb = new StringBuilder();
        sb.append("example=[").append(example).append("]");
        sb.append(" a.find=").append(patA.matcher(example).find());
        sb.append(" a.match=").append(patA.matcher(example).matches());
        sb.append(" b.find=").append(patB.matcher(example).find());
        sb.append(" b.match=").append(patB.matcher(example).matches());
        System.out.println(sb);
    }
}
