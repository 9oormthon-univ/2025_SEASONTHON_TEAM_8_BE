package textmate.backend.chatrooms.application;

import java.text.Normalizer;

public final class NameInitials {

    private NameInitials() {}

    public static String forDisplay(String rawName) {
        if (rawName == null) return "?";
        // 공백/제어문자 정리
        String name = Normalizer.normalize(rawName.trim(), Normalizer.Form.NFC);
        if (name.isEmpty()) return "?";

        // 유효 문자만 훑기 (앞에서부터)
        int[] codePoints = name.codePoints()
                .filter(cp -> !Character.isWhitespace(cp))
                .toArray();

        if (codePoints.length == 0) return "?";

        int first = codePoints[0];

        if (isHangul(first)) {
            // 한글: 첫 글자 1개
            return new String(Character.toChars(first));
        }

        // 영어/숫자/라틴: 두 글자 (대문자, 공백/기호 제외)
        StringBuilder sb = new StringBuilder(2);
        for (int cp : codePoints) {
            if (isAsciiLetterOrDigit(cp)) {
                sb.appendCodePoint(Character.toUpperCase(cp));
                if (sb.length() == 2) break;
            }
        }
        if (sb.length() > 0) return sb.toString();

        // 그 외(이모지/기타 스크립트): 첫 코드포인트 1개
        return new String(Character.toChars(first));
    }

    private static boolean isHangul(int cp) {
        // 완성형 음절, 호환 자모, 자모 포함 모두 허용
        Character.UnicodeBlock block = Character.UnicodeBlock.of(cp);
        return block == Character.UnicodeBlock.HANGUL_SYLLABLES
                || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                || block == Character.UnicodeBlock.HANGUL_JAMO
                || block == Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_A
                || block == Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_B;
    }

    private static boolean isAsciiLetterOrDigit(int cp) {
        return (cp >= 'A' && cp <= 'Z')
                || (cp >= 'a' && cp <= 'z')
                || (cp >= '0' && cp <= '9');
    }
}