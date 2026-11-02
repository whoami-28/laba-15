package com.library.repository;

import com.library.model.Identifiable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Repository<T extends Identifiable<ID>, ID> {

    void add(T item);

    Optional<T> findById(ID id);

    List<T> findAll();

    List<T> findByPredicate(Predicate<T> predicate);

    boolean update(T item);

    boolean deleteById(ID id);

    int count();

    void clear();
}
