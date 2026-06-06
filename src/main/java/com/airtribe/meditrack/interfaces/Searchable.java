package com.airtribe.meditrack.interfaces;

import java.util.List;

/**
 * Generic Searchable interface with default utility method.
 *
 * @param <T> the type of entity to search
 */
public interface Searchable<T> {

    /** Search by unique identifier. */
    T searchById(String id);

    /** Search by name (case-insensitive partial match). */
    T searchByName(String name);

    /** Default method — searches a list by ID. */
    default T findInList(List<T> list, String id) {
        for (T item : list) {
            if (item instanceof Searchable) {
                @SuppressWarnings("unchecked")
                T result = ((Searchable<T>) item).searchById(id);
                if (result != null) return result;
            }
        }
        return null;
    }
}
