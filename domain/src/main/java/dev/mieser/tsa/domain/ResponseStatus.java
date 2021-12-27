package dev.mieser.tsa.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Enumeration of all status values a compliant TSAs should produce according to <a href="https://datatracker.ietf.org/doc/html/rfc3161.html">RFC 3161</a>.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ResponseStatus {

    GRANTED(0),

    GRANTED_WITH_MODS(1),

    REJECTION(2),

    WAITING(3),

    REVOCATION_WARNING(4),

    REVOCATION_NOTIFICATION(5);

    /**
     * The integer representation of the status.
     */
    private final int value;

    /**
     * @param value The value the corresponding {@link ResponseStatus} should be returned for.
     * @return An optional containing the corresponding {@link ResponseStatus} or {@link Optional#empty()} when no corresponding enum constant is defined.
     */
    public static Optional<ResponseStatus> fromIntValue(int value) {
        return EnumSet.allOf(ResponseStatus.class).stream()
                .filter(status -> status.value == value)
                .findFirst();
    }

}
