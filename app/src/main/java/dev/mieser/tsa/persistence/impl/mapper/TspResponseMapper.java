package dev.mieser.tsa.persistence.impl.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import dev.mieser.tsa.domain.TimeStampRequestData;
import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.persistence.impl.entity.TspRequestEntity;
import dev.mieser.tsa.persistence.impl.entity.TspResponseEntity;

/**
 * Mapstruct {@link Mapper} to map between domain objects and JPA entities.
 */
@Mapper
public interface TspResponseMapper {

    TimeStampResponseData toDomain(TspResponseEntity entity);

    TspResponseEntity fromDomain(TimeStampResponseData domain);

    @Mapping(target = "id", ignore = true)
    TspRequestEntity fromDomain(TimeStampRequestData domain);

}
