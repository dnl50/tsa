package dev.mieser.tsa.persistence;

import dev.mieser.tsa.persistence.entity.TspRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TspRequestEntityRepository extends JpaRepository<TspRequestEntity, Long> {

}
