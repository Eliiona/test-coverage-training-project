package lv.bootcamp.shelter.repository;

import lv.bootcamp.shelter.model.Adopter;

import java.util.Optional;

public interface AdopterRepository {
    Optional<Adopter> findById(Long id);
}
