package dev.mieser.tsa.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.mieser.tsa.persistence.entity.TspResponseEntity;

public interface TspResponseEntityRepository extends JpaRepository<TspResponseEntity, Long> {

}
