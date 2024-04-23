# Java Concurrency 

Tasks and implementations for the concurrency exercise  

## Task 1

https://math.hws.edu/javanotes/c12/exercises.html 12.1

Write a program to find out. Use the following unsynchronized counter class, which you can include as a nested class in your program:

```java
static class Counter {
    int count;

    void inc() {
        count = count + 1;
    }

    int getCount() {
        return count;
    }
}
```

Write a thread class that will repeatedly call the inc() method in an object of type Counter. The object should be a shared global variable. Create several threads, start them all, and wait for all the threads to terminate. Print the final value of the counter, and see whether it is correct.
