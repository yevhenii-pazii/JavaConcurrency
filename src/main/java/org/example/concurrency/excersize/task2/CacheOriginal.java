package org.example.concurrency.excersize.task2;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class CacheOriginal<K, V> {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock read = lock.readLock();
    private final Lock write = lock.writeLock();

    private final Supplier<List<V>> supplier;
    private final Function<List<V>, Map<K, V>> transformer;

    @GuardedBy("lock") private int hash; // change indicator
    @GuardedBy("lock") private Map<K, V> data;

    public CacheOriginal(Supplier<List<V>> supplier, Function<List<V>, Map<K, V>> transformer) {
        this.supplier = supplier;
        this.transformer = transformer;

        var list = supplier.get();
        hash = list.hashCode();
        data = transformer.apply(list);
    }

    public Map<K, V> data() {
        var supplied = supplier.get();
        var suppliedHash = supplied.hashCode();

        read.lock();
        if (suppliedHash != hash) { //need to update
            read.unlock();
            write.lock();
            try {
                supplied = supplier.get(); // have to go once more
                suppliedHash = supplied.hashCode();

                if (suppliedHash != hash) { //might be updated already
                    hash = suppliedHash;
                    data = transformer.apply(supplied);
                }
                read.lock(); // downgrade to read;
            } finally {
                write.unlock();
            }
        }

        try {
            return data;
        } finally {
            read.unlock();
        }
    }
}
