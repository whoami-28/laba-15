package com.library.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public final class GenericUtils {

    private GenericUtils() {

    }

    public static <T> List<T> filter(Collection<T> items, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        if (items == null || predicate == null) {
            return result;
        }
        for (T item : items) {
            if (predicate.test(item)) {
                result.add(item);
            }
        }
        return result;
    }

    public static <T, R> List<R> map(Collection<T> items, Function<T, R> mapper) {
        List<R> result = new ArrayList<>();
        if (items == null || mapper == null) {
            return result;
        }
        for (T item : items) {
            result.add(mapper.apply(item));
        }
        return result;
    }

    public static <T> Optional<T> findFirst(Collection<T> items, Predicate<T> predicate) {
        if (items == null || predicate == null) {
            return Optional.empty();
        }
        for (T item : items) {
            if (predicate.test(item)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }
}
