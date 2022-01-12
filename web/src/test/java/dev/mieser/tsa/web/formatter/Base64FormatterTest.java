package dev.mieser.tsa.web.formatter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Locale;

import org.junit.jupiter.api.Test;

class Base64FormatterTest {

    private final Base64Formatter testSubject = new Base64Formatter();

    @Test
    void parseThrowsExceptionWhenStringCannotBeDecoded() {
        // given
        String illegalBase64 = "I'm Base64, trust me, bro!";

        // when / then
        assertThatIllegalArgumentException()
            .isThrownBy(() -> testSubject.parse(illegalBase64, Locale.GERMAN))
            .withMessage("Not a valid Base64 string.");
    }

    @Test
    void parseReturnsDecodedBinaryData() {
        // given
        String encoded = "NzM1NTYwOA==";

        // when
        byte[] decodedBinaryData = testSubject.parse(encoded, Locale.ENGLISH);

        // then
        assertThat(decodedBinaryData).isEqualTo("7355608".getBytes(UTF_8));
    }

    @Test
    void printReturnsEncodedBinaryData() {
        // given
        byte[] binaryData = "7355608".getBytes(UTF_8);

        // when
        String encodedData = testSubject.print(binaryData, Locale.CANADA);

        // then
        assertThat(encodedData).isEqualTo("NzM1NTYwOA==");
    }

}
