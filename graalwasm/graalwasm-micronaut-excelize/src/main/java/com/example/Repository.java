package com.example;

import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.transaction.annotation.Transactional;

@io.micronaut.data.annotation.Repository
@Transactional
public interface Repository extends CrudRepository<Book,Long> {
}
