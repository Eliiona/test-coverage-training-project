package lv.bootcamp.shelter.repository;

import lv.bootcamp.shelter.model.Animal;

import java.util.Optional;

public interface AnimalRepository {
    Optional<Animal> findById(Long id);
}
