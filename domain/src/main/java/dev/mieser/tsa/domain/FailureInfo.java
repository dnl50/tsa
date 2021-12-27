package dev.mieser.tsa.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Enumeration of all {@code PKIFailureInfo} values a TSA should support according to <a href="https://datatracker.ietf.org/doc/html/rfc3161.html">RFC 3161</a>.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum FailureInfo {

    BAD_ALGORITHM(1 << 7),

    BAD_REQUEST(1 << 5),

    BAD_DATA_FORMAT(1 << 2),

    TIME_NOT_AVAILABLE(1 << 9),

    UNACCEPTED_POLICY(1 << 8),

    UNACCEPTED_EXTENSION(1 << 23),

    ADD_INFO_NOT_AVAILABLE(1 << 22),

    SYSTEM_FAILURE(1 << 30);

    /**
     * The integer value of the {@code PKIFailureInfo}.
     */
    private final int value;

    /**
     * @param value The value to get the corresponding  {@link FailureInfo} constant for.
     * @return The corresponding {@link FailureInfo} constant or {@link Optional#empty()} when no constant with the specified value is defined.
     */
    public static Optional<FailureInfo> fromIntValue(int value) {
        return EnumSet.allOf(FailureInfo.class).stream()
                .filter(failureInfo -> failureInfo.value == value)
                .findFirst();
    }

}
