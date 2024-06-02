package org.example.concurrency.excersize.task4;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Getter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ConsumerProducerQueueTest {

    private static class Worker implements Runnable {

        private final int prefix;

        private final int repeat;

        @Getter
        private final List<Integer> results;

        private final CountDownLatch start;

        private final CountDownLatch end;

        private final Function<Integer, Integer> function;

        public Worker(int prefix, int repeat, CountDownLatch start, CountDownLatch end, Function<Integer, Integer> function) {
            this.prefix = prefix;
            this.repeat = repeat;
            results = new ArrayList<>(repeat);
            this.start = start;
            this.end = end;
            this.function = function;
        }

        @Override
        public void run() {
            try {
                start.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < repeat; i++) {
                results.add(function.apply(prefix + i));
            }
            end.countDown();
        }

    }

    private static Stream<Arguments> data() {
        return Stream.of(
            arguments(10, 10, 10, 100),
            arguments(10, 10, 10, 10_000),
            arguments(10, 5, 50, 100),
            arguments(10, 50, 5, 100)
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void testBalancedCountOfConsumersAndProducers(
        int capacity, int producersNumber, int consumersNumber, int messagesPerThread) throws InterruptedException {

        ConsumerProducerQueue<Integer> queue = new ConsumerProducerQueue<>(capacity);

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch end = new CountDownLatch(producersNumber + consumersNumber);

        List<Worker> producers = new ArrayList<>(producersNumber);
        for (int i = 0; i < producersNumber; i++) {
            producers.add(
                new Worker(i * messagesPerThread, messagesPerThread, start, end, n -> {
                    try {
                        queue.publish(n);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return n;
                })
            );
        }

        List<Worker> consumers = new ArrayList<>(consumersNumber);
        int messagesToConsumeByConsumers = producersNumber * messagesPerThread / consumersNumber; //TODO may lose some messages - division
        for (int i = 0; i < consumersNumber; i++) {
            consumers.add(
                new Worker(i * messagesToConsumeByConsumers, messagesToConsumeByConsumers, start, end, n -> {
                    try {
                        return queue.consume();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
            );
        }

        try (
            ExecutorService producersPool = Executors.newFixedThreadPool(producersNumber);
            ExecutorService consumersPool = Executors.newFixedThreadPool(consumersNumber)
        ) {
            producers.forEach(producersPool::submit);
            consumers.forEach(consumersPool::submit);
            start.countDown();

            boolean completed = end.await(10, TimeUnit.SECONDS);
            if (!completed) {
                throw new RuntimeException("Test timed out");
            }
        }

        List<Integer> produced = new ArrayList<>(producersNumber * messagesPerThread);
        producers.forEach(c -> produced.addAll(c.getResults()));

        List<Integer> consumed = new ArrayList<>(producersNumber * messagesPerThread);
        consumers.forEach(c -> consumed.addAll(c.getResults()));

        assertThat(consumed)
            .hasSize(producersNumber * messagesPerThread)
            .containsExactlyInAnyOrderElementsOf(produced);
    }

}
