package org.example.concurrency.excersize.task1;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class CounterTest {

    @Slf4j
    @Builder
    @RequiredArgsConstructor
    static class Worker implements Runnable {

        private final Counter counter;
        private final int incrementCount;
        private final CountDownLatch startLatch;
        private final CountDownLatch endLatch;


        @Override
        public void run() {
            try {
                startLatch.await();

                log.info("Start processing");
                for (int i = 0; i < incrementCount; i++) {
                    counter.inc();
                }

                endLatch.countDown();
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Test
    void testNotThreadSafe() throws InterruptedException {
        var threads = 10;
        var increments = 1000;


        var counter = new Counter();

        /*
            this helps to hold all thread till all is ready to get started
            All workers waiting for test thread to get start
         */
        CountDownLatch start = new CountDownLatch(1);

        /*
            this helps the test thread to wait till all thread finished
            Test thread waits for all workers
         */
        CountDownLatch end = new CountDownLatch(threads);

        for (var i = 0; i < threads; i++) {
            new Thread(
                    Worker.builder()
                            .counter(counter)
                            .incrementCount(increments)
                            .startLatch(start)
                            .endLatch(end)
                            .build()).start();
        }

        start.countDown();

        end.await();

        log.info("Actual {}, Expected {}", counter.getCount(), threads * increments);
        assertThat(counter.getCount()).isNotEqualTo(threads * increments);
    }

}