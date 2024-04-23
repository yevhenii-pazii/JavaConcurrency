package org.example.concurrency.chachefrombook;

import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Serfs as a cache using concurrent hash map as result
 *
 * @param <A>
 * @param <V>
 */
@ThreadSafe
public class Memoizer<A, V> implements Function<A, V> {

    private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<>();
    private final Function<A, V> function;

    public Memoizer(Function<A, V> function) {
        this.function = function;
    }

    @Override
    public V apply(A a) {
        var future = cache.get(a);
        if (future == null) {
            var callable = new Callable<V>() {
                @Override
                public V call() {
                    return function.apply(a);
                }
            };

            var task = new FutureTask<>(callable);
            future = cache.putIfAbsent(a, task);
            if (future == null) { // if not null, other thread already added and executed the future, join
                future = task;
                task.run();
            }
        }
        try {
            return future.get();
        } catch (Exception e) {
            cache.remove(a, future);
        }
        return null;
    }
}
