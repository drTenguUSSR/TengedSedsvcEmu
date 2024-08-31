package mil.teng.q2024.sedsvc.emu.via.kafka.services;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class NameValidatorUtils {
    private static final Pattern SAFE_NAME = Pattern.compile("^[a-zA-Z0-9._-]{3,240}$");
    private static final int MAX_NAME_LENGTH = 249;

    public static String validateTopicName(String src) {
        if (!StringUtils.hasText(src)) {
            return "src=[" + src + "] is null or empty";
        }
        if (src.length() < 3 || src.length() > MAX_NAME_LENGTH) {
            return "src=[" + src + "] is too short(src<3) or too long(src>" + MAX_NAME_LENGTH + ")";
        }
        if (!safeName(src)) {
            return "src=[" + src + "] regExp mismatch";
        }
        return null;
    }

    public static boolean safeName(String src) {
        if (!StringUtils.hasText(src)) {
            return false;
        }
        return SAFE_NAME.matcher(src).find();
    }

    public static String safeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "must not be null or empty";
        }
        int pos = contentType.indexOf("/");
        if (pos == -1) {
            return "must contain '/'";
        }
        String typeA = contentType.substring(0, pos);
        String typeB = contentType.substring(pos + 1);
        if (!StringUtils.hasText(typeA) || typeA.indexOf("application") == -1) {
            return "typeA error. typeA=[" + typeA + "].ct=["+contentType+"]";
        }
        if (!StringUtils.hasText(typeB) || typeB.indexOf(";type=") == -1) {
            return "typeB error. typeB=[" + typeB + "].ct=["+contentType+"]";
        }
        return null;
    }
}
