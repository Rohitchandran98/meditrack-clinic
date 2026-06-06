package com.airtribe.meditrack.util;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic in-memory data store backed by a HashMap.
 * Demonstrates: generics, Comparator, Iterator, streams & lambdas.
 *
 * @param <T> the entity type (must have a String getId() method via Identifiable contract)
 */
public class DataStore<T> implements Iterable<T> {

    private final Map<String, T> store = new LinkedHashMap<>();
    private final java.util.function.Function<T, String> keyExtractor;

    /**
     * @param keyExtractor function that extracts the unique ID from an entity (e.g., Entity::getId)
     */
    public DataStore(java.util.function.Function<T, String> keyExtractor) {
        this.keyExtractor = keyExtractor;
    }

    /** Add or update an entity. */
    public void save(T entity) {
        store.put(keyExtractor.apply(entity), entity);
    }

    /** Remove by ID, returns true if found. */
    public boolean delete(String id) {
        return store.remove(id) != null;
    }

    /** Find by ID, returns Optional. */
    public Optional<T> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    /** Returns all entities as an unmodifiable list. */
    public List<T> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    /** Filter using a lambda / Predicate — streams. */
    public List<T> filter(Predicate<T> predicate) {
        return store.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /** Sort all entities by a given Comparator — demonstrates Comparator usage. */
    public List<T> sorted(Comparator<T> comparator) {
        return store.values().stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public int size()      { return store.size(); }
    public boolean isEmpty() { return store.isEmpty(); }
    public boolean exists(String id) { return store.containsKey(id); }

    /** Iterator support — demonstrates Iterator pattern. */
    @Override
    public Iterator<T> iterator() {
        return store.values().iterator();
    }

    /** Replace all contents (used when loading from file). */
    public void loadAll(Collection<T> entities) {
        store.clear();
        for (T e : entities) store.put(keyExtractor.apply(e), e);
    }

    @Override
    public String toString() {
        return "DataStore{size=" + store.size() + "}";
    }
}
