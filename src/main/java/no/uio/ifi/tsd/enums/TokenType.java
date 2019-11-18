package no.uio.ifi.tsd.enums;

import java.util.Arrays;

public enum TokenType {

    IMPORT, EXPORT, ADMIN;

    public static TokenType get(String tokenType) {
        return Arrays.stream(TokenType.values()).filter(e -> e.name().equalsIgnoreCase(tokenType)).findAny().orElse(IMPORT);
    }

}
