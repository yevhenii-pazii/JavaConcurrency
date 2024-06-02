package org.example.concurrency.excersize.task3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class ThreadPoolScheduler implements CommandLineRunner {

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
//    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
//    private final ExecutorService executorService = new SameThreadExecutorService();

    public static class SameThreadExecutorService extends AbstractExecutorService {

        //volatile because can be viewed by other threads
        private volatile boolean terminated;

        @Override
        public void shutdown() {
            terminated = true;
        }

        @Override
        public boolean isShutdown() {
            return terminated;
        }

        @Override
        public boolean isTerminated() {
            return terminated;
        }

        @Override
        public boolean awaitTermination(long theTimeout, TimeUnit theUnit) throws InterruptedException {
            shutdown(); // TODO ok to call shutdown? what if the client never called shutdown???
            return terminated;
        }

        @Override
        public List<Runnable> shutdownNow() {
            return Collections.emptyList();
        }

        @Override
        public void execute(Runnable theCommand) {
            theCommand.run();
        }
    }

    @Bean
    public ExecutorService executorService() {
        return executorService;
    }

    private final Service service;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("task 3");
        //run();
        run2();
    }

    @Slf4j
    private static class Task implements Callable<Integer> {

        private final int number;

        public Task(int number) {
            this.number = number;
        }

        @Override
        public Integer call() throws Exception {
            log.info("processing number {}", number);
            return number * number;
        }

    }

    private void run() throws InterruptedException, ExecutionException {
        List<Callable<Integer>> tasks = new ArrayList<>(100);
        for (var i = 0; i < 100; i++) {
            tasks.add(new Task(i));
        }

        log.info("pre invoke");
        List<Future<Integer>> futureResult = executorService.invokeAll(tasks);

        List<Integer> result = new ArrayList<>(100);
        for(var f : futureResult) {

            result.add(f.get());
        }

        log.info("result {}", result);
        executorService.shutdown();
    }

    private void run2() throws ExecutionException, InterruptedException {
        List<Future<Integer>> futureResult = new ArrayList<>(100);
        log.info("pre invoke");
        for (var i = 0; i < 100; i++) {
            futureResult.add(service.calculate(i));
        }
        List<Integer> result = new ArrayList<>(100);
        for(var f : futureResult) {
            result.add(f.get());
        }

        log.info("result {}", result);
        executorService.shutdown();
    }



}
