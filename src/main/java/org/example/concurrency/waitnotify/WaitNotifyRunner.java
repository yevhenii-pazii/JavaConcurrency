package org.example.concurrency.waitnotify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
//@Component
public class WaitNotifyRunner implements CommandLineRunner {

    private static class Some {

        synchronized void doWait() throws InterruptedException {
            wait();
        }

        synchronized void doNotify() {
            notifyAll();
        }
    }


    @Override
    public void run(String... args) throws Exception {
        final Some lock = new Some();
        log.info("main started");

        for (var i = 0; i < 4; i++) {
            new Thread(() -> {
                log.info("Thread started");
                try {
                    lock.doWait();
                } catch (InterruptedException e) {
                    log.info("Interrupted");
                }
                log.info("done");
            }).start();
        }

        Thread.sleep(100);

        log.info("main done");
        lock.doNotify();
    }
}
