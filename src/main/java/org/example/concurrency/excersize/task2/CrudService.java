package org.example.concurrency.excersize.task2;

public interface CrudService<K, V> {

    V crate(K key, V value);
    V read(K key);
    V update(K key, V value);
    V delete(K key);

}
