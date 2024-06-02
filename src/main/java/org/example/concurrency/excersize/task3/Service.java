package org.example.concurrency.excersize.task3;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Service {

    @Async("executorService")
    public Future<Integer> calculate(int value) {
        log.info("processing number {}", value);
        return CompletableFuture.completedFuture(value * value);
    }

}
