package dev.mieser.tsa.web.paging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class OffsetBasedPageableTest {

    @ParameterizedTest
    @ValueSource(longs = {-1337L, -10L, -1L})
    void constructorThrowsExceptionWhenOffsetIsInvalid(long illegalOffset) {
        // given / when / then
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new OffsetBasedPageable(illegalOffset, 15, null))
                .withMessage("Offset cannot be less than zero.");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1337, -1, 0})
    void constructorThrowsExceptionWhenPageSizeIsInvalid(int illegalPageSize) {
        // given / when / then
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new OffsetBasedPageable(15L, illegalPageSize, null))
                .withMessage("Page size must be greater than or equal to one.");
    }

    @MethodSource("offsetAndPageSizeToPageNumber")
    @ParameterizedTest
    void getPageNumberReturnsExpectedResult(long offset, int pageSize, int expectedPageNumber) {
        // given
        var testSubject = new OffsetBasedPageable(offset, pageSize, null);

        // when
        int actualPageNumber = testSubject.getPageNumber();

        // then
        assertThat(actualPageNumber).isEqualTo(expectedPageNumber);
    }

    @Test
    void getPageSizeReturnsPageSize() {
        // given
        int pageSize = 1337;

        var testSubject = new OffsetBasedPageable(10L, pageSize, null);

        // when
        int actualPageSize = testSubject.getPageSize();

        // then
        assertThat(actualPageSize).isEqualTo(pageSize);
    }

    @Test
    void getOffsetReturnsOffset() {
        // given
        long offset = 1337L;

        var testSubject = new OffsetBasedPageable(offset, 31, null);

        // when
        long actualOffset = testSubject.getOffset();

        // then
        assertThat(actualOffset).isEqualTo(offset);
    }

    @Test
    void getSortReturnsExpectedSort() {
        // given
        Sort sort = Sort.by("test1", "test2");

        var testSubject = new OffsetBasedPageable(12L, 31, sort);

        // when
        Sort actualSort = testSubject.getSort();

        // then
        assertThat(actualSort).isEqualTo(sort);
    }

    @Test
    void nextReturnsExpectedPage() {
        // given
        var testSubject = new OffsetBasedPageable(12L, 67, null);

        // when
        Pageable next = testSubject.next();

        // then
        assertThat(next).isEqualTo(new OffsetBasedPageable(79, 67, null));
    }

    @Test
    void previousOrFirstReturnsFirstPageWhenNoPreviousExists() {
        // given
        var testSubject = new OffsetBasedPageable(15L, 30, null);

        // when
        Pageable previousOrFirst = testSubject.previousOrFirst();

        // then
        assertThat(previousOrFirst).isEqualTo(new OffsetBasedPageable(0, 30, null));
    }

    @Test
    void previousOrFirstReturnsPreviousPageWhenPreviousPageExists() {
        // given
        var testSubject = new OffsetBasedPageable(1500L, 30, null);

        // when
        Pageable previousOrFirst = testSubject.previousOrFirst();

        // then
        assertThat(previousOrFirst).isEqualTo(new OffsetBasedPageable(1470, 30, null));
    }

    @Test
    void firstReturnsExpectedPage() {
        // given
        var testSubject = new OffsetBasedPageable(1337L, 30, null);

        // when
        Pageable first = testSubject.first();

        // then
        assertThat(first).isEqualTo(new OffsetBasedPageable(0, 30, null));
    }

    @Test
    void withPageReturnsExpectedPage() {
        // given
        var testSubject = new OffsetBasedPageable(1337L, 27, null);

        // when
        var withPage = testSubject.withPage(12);

        // then
        assertThat(withPage).isEqualTo(new OffsetBasedPageable(324L, 27, null));
    }

    @ParameterizedTest
    @MethodSource("offsetAndPageSizeToPrevious")
    void hasPreviousReturnsExpectedResult(long offset, int pageSize, boolean shouldHavePrevious) {
        // given
        var testSubject = new OffsetBasedPageable(offset, pageSize, null);

        // when
        boolean hasPrevious = testSubject.hasPrevious();

        // then
        assertThat(hasPrevious).isEqualTo(shouldHavePrevious);
    }

    static Stream<Arguments> offsetAndPageSizeToPageNumber() {
        return Stream.of(
                arguments(0L, 1, 0),
                arguments(1L, 1, 1),
                arguments(10L, 5, 2),
                arguments(10L, 7, 1)
        );
    }

    static Stream<Arguments> offsetAndPageSizeToPrevious() {
        return Stream.of(
                arguments(10L, 7, true),
                arguments(7L, 7, true),
                arguments(3L, 7, false)
        );
    }

}
