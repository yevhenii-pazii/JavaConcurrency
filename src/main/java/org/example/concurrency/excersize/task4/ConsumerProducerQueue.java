package org.example.concurrency.excersize.task4;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConsumerProducerQueue<T> {

    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    private final int capacity;
    private final Queue<T> messages;

    public ConsumerProducerQueue(int capacity) {
        this.capacity = capacity;
        messages = new ArrayDeque<>(capacity);
    }

    public void publish(T message) throws InterruptedException {
        lock.lock();
        try {
            while (messages.size() == capacity) {
                notFull.await();
            }
            messages.add(message);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public T consume() throws InterruptedException {
        lock.lock();
        try {
            while (messages.isEmpty()) {
                notEmpty.await();
            }
            var message = messages.poll();
            notFull.signal();
            return message;
        } finally {
            lock.unlock();
        }
    }

}
