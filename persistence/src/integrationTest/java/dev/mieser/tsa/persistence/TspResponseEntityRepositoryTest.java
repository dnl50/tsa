package dev.mieser.tsa.persistence;

import dev.mieser.tsa.domain.HashAlgorithm;
import dev.mieser.tsa.domain.ResponseStatus;
import dev.mieser.tsa.persistence.entity.TspRequestEntity;
import dev.mieser.tsa.persistence.entity.TspResponseEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DataJpaTest
@Transactional
@Sql("classpath:/dev/mieser/tsa/persistence/response-data.sql")
class TspResponseEntityRepositoryTest {

    private final TspResponseEntityRepository testSubject;

    private final TspRequestEntityRepository requestEntityRepository;

    @Autowired TspResponseEntityRepositoryTest(TspResponseEntityRepository testSubject, TspRequestEntityRepository requestEntityRepository) {
        this.testSubject = testSubject;
        this.requestEntityRepository = requestEntityRepository;
    }

    @Test
    void save() {
        // given
        TspResponseEntity entityToSave = createEntity();

        // when
        TspResponseEntity savedEntity = testSubject.save(entityToSave);

        // then
        TspResponseEntity expectedSavedEntity = createEntity();
        expectedSavedEntity.setId(savedEntity.getId());
        expectedSavedEntity.getRequest().setId(savedEntity.getRequest().getId());

        assertThat(testSubject.findById(savedEntity.getId())).contains(expectedSavedEntity);
    }

    @Test
    void findByIdReturnsExpectedEntity() {
        // given / when
        Optional<TspResponseEntity> entity = testSubject.findById(3L);

        // then
        assertThat(entity).map(TspResponseEntity::getId).contains(3L);
    }

    @Test
    void deleteByIdCascadesDelete() {
        // given / when
        testSubject.deleteById(2L);

        // then
        assertSoftly(softly -> {
            softly.assertThat(testSubject.existsById(2L)).isFalse();
            softly.assertThat(requestEntityRepository.existsById(2L)).isFalse();
        });
    }

    private TspResponseEntity createEntity() {
        TspRequestEntity requestEntity = TspRequestEntity.builder()
                .hashAlgorithm(HashAlgorithm.SHA512)
                .hash("aGFzaA==")
                .nonce("1337")
                .certificateRequested(true)
                .tsaPolicyId("1.3.6")
                .asnEncoded("cmVx")
                .build();

        return TspResponseEntity.builder()
                .status(ResponseStatus.REJECTION)
                .statusString("test")
                .failureInfo(12)
                .generationTime(ZonedDateTime.parse("2021-11-13T21:29:13Z"))
                .serialNumber(3315L)
                .request(requestEntity)
                .asnEncoded("cmVz")
                .build();
    }

    @Configuration
    @EntityScan(basePackageClasses = TspRequestEntityRepository.class)
    @EnableJpaRepositories(basePackageClasses = TspResponseEntityRepository.class)
    static class TestContextConfiguration {

    }

}
