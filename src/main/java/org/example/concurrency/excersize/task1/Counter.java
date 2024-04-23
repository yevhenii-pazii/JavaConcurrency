package org.example.concurrency.excersize.task1;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class Counter {
    private int count = 0;

    void inc() {
        count = count + 1;
    }

    int getCount() {
        return count;
    }
}
