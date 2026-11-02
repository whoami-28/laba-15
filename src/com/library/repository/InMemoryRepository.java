package com.library.repository;

import com.library.model.Identifiable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class InMemoryRepository<T extends Identifiable<ID>, ID> implements Repository<T, ID> {
    private final Map<ID, T> storage = new LinkedHashMap<>();

    @Override
    public synchronized void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Элемент для добавления не может быть null.");
        }
        storage.put(item.getId(), item);
    }

    @Override
    public synchronized Optional<T> findById(ID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public synchronized List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public synchronized List<T> findByPredicate(Predicate<T> predicate) {
        if (predicate == null) {
            return findAll();
        }
        List<T> results = new ArrayList<>();
        for (T item : storage.values()) {
            if (predicate.test(item)) {
                results.add(item);
            }
        }
        return results;
    }

    @Override
    public synchronized boolean update(T item) {
        if (item == null || !storage.containsKey(item.getId())) {
            return false;
        }
        storage.put(item.getId(), item);
        return true;
    }

    @Override
    public synchronized boolean deleteById(ID id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }

    @Override
    public synchronized int count() {
        return storage.size();
    }

    @Override
    public synchronized void clear() {
        storage.clear();
    }
}
