package dev.mieser.tsa.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.mieser.tsa.persistence.entity.TspRequestEntity;

public interface TspRequestEntityRepository extends JpaRepository<TspRequestEntity, Long> {

}
