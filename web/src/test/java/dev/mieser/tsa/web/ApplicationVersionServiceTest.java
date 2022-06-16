package dev.mieser.tsa.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.Test;

class ApplicationVersionServiceTest {

    @Test
    void getApplicationVersionStripsLeadingAndTrailingWhitespace() {
        // given
        var testSubject = new ApplicationVersionService("version-with-whitespace.txt");

        // when
        String applicationVersion = testSubject.getApplicationVersion();

        // then
        assertThat(applicationVersion).isEqualTo("1.2.3");
    }

    @Test
    void getApplicationVersionReadsFileWithExpectedEncoding() {
        // given
        var testSubject = new ApplicationVersionService("version-with-umlaut.txt");

        // when
        String applicationVersion = testSubject.getApplicationVersion();

        // then
        assertThat(applicationVersion).isEqualTo("1.ü");
    }

    @Test
    void getApplicationVersionThrowsExceptionWhenVersionFileWasNotFound() {
        // given
        var testSubject = new ApplicationVersionService("unknown-file.txt");

        // when / then
        assertThatIllegalStateException()
            .isThrownBy(testSubject::getApplicationVersion)
            .withMessage("Application version file was not found on the classpath.");
    }

    @Test
    void getApplicationVersionReadsGeneratedVersionFileByDefault() {
        // given
        var testSubject = new ApplicationVersionService();

        // when
        var applicationVersion = testSubject.getApplicationVersion();

        // then
        assertThat(applicationVersion).isNotBlank();
    }

}
