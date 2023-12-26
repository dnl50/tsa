package dev.mieser.tsa.persistence.impl;

import jakarta.enterprise.context.ApplicationScoped;

import dev.mieser.tsa.persistence.impl.entity.TspResponseEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class TspPanacheRepository implements PanacheRepository<TspResponseEntity> {

}
