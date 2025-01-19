package ru.bpmcons.sbi_elma.utils;

import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

import java.util.Base64;

@UtilityClass
public class MD5Utils {
    private static final int MD5_DIGEST_LEN = 16;

    public static boolean isValidHash(@Nullable String hash) {
        if (hash == null) {
            return false;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(hash);
            return decoded.length == MD5_DIGEST_LEN;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
