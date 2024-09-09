/*
 *
 * @author DrTengu. 2024/09
 */

package mil.teng.q2024.sedsvc.emu.experimental;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.regex.Pattern;


/**
 * <a href="https://www.rfc-editor.org/rfc/rfc5321.txt">rfc5321</a>
 * 4.5.3.1.1.  Local-part. The maximum total length of a user name or other local-part is 64 octets.
 * 4.5.3.1.2.  Domain. The maximum total length of a domain name or number is 255 octets.
 * <p>
 * формально <a href="https://datatracker.ietf.org/doc/html/rfc3696">rfc3696</a>
 * Without quotes, local-parts may consist of any combination of
 * alphabetic characters, digits, or any of the special characters
 * ! # $ % & ' * + - / = ?  ^ _ ` . { | } ~
 * period (".") may also appear, but may not be used to start or end the
 * local part, nor may two or more consecutive periods appear.  Stated
 * differently, any ASCII graphic (printing) character other than the
 * at-sign ("@"), backslash, double quote, comma, or square brackets may
 * appear without quoting.  If any of that list of excluded characters
 * are to appear, they must be quoted
 * <a href="https://en.wikipedia.org/wiki/Email_address">Email_address</a>
 * domain
 * Uppercase and lowercase Latin letters A to Z and a to z;
 * Digits 0 to 9, provided that top-level domain names are not all-numeric;
 * Hyphen -, provided that it is not the first or last character.
 */

class MailValidatorTest {
    private static final String MAX_LOCAL_64 = "abcdefghiikabcdefghiikabcdefghiikabcdefghiikabcdefghiikabcdefghK";
    private static final String MAX_DOMAIN_255 = "ikabcdefghiikab" +
            "abcdefghiikabcdefghiikabcdefghiikabcdefghiikabcdefghiikabcdefghiikabcdefghiikabc" +
            "defghiikabcdefghiikabcdefghiikabcdefghiikabcdefghiikabcdefghiikabcdefghiikabcdef" +
            "ghiikabcdefghiikabcdefghiikabcdefghiikabcdefghiikabcdefghiikabcdefghiikabcdefghi";

    static {
        System.out.println("MailValidatorTest.static. useFuncA=" + MailValidator.USE_FUNC_A);
    }

    /**
     * hint: ',' не работает внутри параметров. использовать заменный символ;
     * экранирование не помогает; запись через код "002C" не помогает
     */
    @ParameterizedTest
    @CsvSource({
            "adbc@mail.ru",
            "ad_bc@mail.ru",
            "ad-bc@mail.ru",
            "ad.bc@mail.ru",
            "petrov.b.c@mail.ru",
            "petrovBV@mail.ru",
            "APetrov@mail.ru",
            "APetrov@gov.mail.ru",
            "APetrov@gov.nasa",
            "adbc@l.ru",
            "kim@88.ch",
            MAX_LOCAL_64 + "@mail.ru",
            "user@" + MAX_DOMAIN_255,
    })
    void correct_mustPass(String example) {
        Assertions.assertTrue(MailValidator.isValid(example));
    }

    @ParameterizedTest
    @CsvSource({
            ".-@mail.ru",
            "1login_@mail.ru",
            "_login@mail.ru",
            "_login@ru.name.ru",
            "-login@mail.ru",
            "-login@ru.name.ru",
            ".login@mail.ru",
            "login.@ru.name.ru",
            "логинlogin@mail.ru",
            "loginлогин@mail.ru",
            "123..456@i.ru ",
            "123456-@mail.ru",
            "@123456@mail.ru",
            "762@.ru",
            "763@ru",
            "user@.ru",
            "123123@i.яя",
            "123123@ru.name.яяя",
            MAX_LOCAL_64 + "22@mail.ru", //64+2 len local part
            "user@" + MAX_DOMAIN_255 + "33", //255+2 len domain part
            //https://en.wikipedia.org/wiki/Email_address#cite_note-rfc3696-8
            "abc.example.com",
            "a@b@c@example.com",
            "a\"b(c)d,e:f;g<h>i[j\\k]l@example.com",
            "i.like.underscores@but_they_are_not_allowed_in_this_part"
            //, "user@example.88" - hard to fix

    })
    void incorrectMail_mustFail(String example) {
        Assertions.assertFalse(MailValidator.isValid(example));
    }

    @Test
    @DisabledIf("isModeA")
    void domain255_pass() {
        Assertions.assertEquals(255, MAX_DOMAIN_255.length());
    }

    @Test
    @DisabledIf("isModeA")
    void local64_pass() {
        Assertions.assertEquals(64, MAX_LOCAL_64.length());
    }

    public boolean isModeA() {
        return MailValidator.USE_FUNC_A;
    }

    private static class MailValidator {
        private static final boolean USE_FUNC_A = false;
        private static final Pattern FUNCB_PATTERN = Pattern.compile(
                        "[a-zA-Z]" //local-part first letter
                        + "[\\w.-]{0,62}" // local-part other letter
                        + "\\w"
                        + "@"
                        + "[a-zA-Z0-9]"
                        + "[a-zA-Z0-9.-]{1,254}"
        );

        public static boolean isValid(String email) {
            if (USE_FUNC_A) {
                return isValidA(email);
            }
            return isValidB(email);
        }

        public static boolean isValidA(String email) {
            return Pattern.matches("^проблемный@паттерн$", email);
        }

        public static boolean isValidB(String email) {
            return FUNCB_PATTERN.matcher(email).matches();
        }
    }


}
