package org.example.concurrency.excersize.task2;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class CacheableCrudService<K, V> implements CrudService<K, V> {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock read = lock.readLock();
    private final Lock write = lock.writeLock();

    private final CrudService<K, V> service;

    @GuardedBy("lock")
    private final Map<K, V> data = new HashMap<>();

    public CacheableCrudService(CrudService<K, V> service) {
        this.service = service;
    }

    @Override
    public V crate(K key, V value) {
        V result = service.crate(key, value); // should be safe doing it here
        write.lock();
        try {
            data.put(key, result);
            return result;
        } finally {
            write.unlock();
        }
    }

    @Override
    public V read(K key) {
        read.lock();
        try {
            V cached = data.get(key);

            if (cached == null) { // should it be inside of read try catch???
                read.unlock();
                write.lock();
                try {
                    cached = data.get(key);
                    if (cached == null) {
                        cached = service.read(key);
                        data.put(key, cached);
                    }
                    read.lock(); // down-grade lock
                } finally {
                    write.unlock();
                }
            }

            return cached;
        } finally {
            read.unlock();
        }

    }

    @Override
    public V update(K key, V value) {
        V newValue = service.update(key, value); //intentionally before lock
        write.lock();
        try {
            data.put(key, newValue);
            return newValue;
        } finally {
            write.unlock();
        }
    }

    @Override
    public V delete(K key) {
        V value = service.delete(key); //intentionally before lock
        write.lock();
        try {
            data.remove(key);
            return value;
        } finally {
            write.unlock();
        }
    }

}
