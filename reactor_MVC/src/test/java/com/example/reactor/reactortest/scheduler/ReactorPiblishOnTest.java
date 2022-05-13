package com.example.reactor.reactortest.scheduler;

import java.time.Duration;
import java.util.ArrayList;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ReactorPiblishOnTest {

  // PublishOn
  // 執行的方式與一般的operator一樣，會影響從publishOn以下的operator chain，改變其threading context，也就是改變執行緒，直到如果有下一個publishOn出現。
  @Test
  void testPublishOn() throws InterruptedException {
    Scheduler s = Schedulers.newParallel("parallel-scheduler", 2);

    final Flux<String> flux = Flux
        .range(1, 2)
        .map(i -> 10 + i)
        .publishOn(s)
        .map(i -> "value " + i);

    Thread thread = new Thread(() ->
        flux.subscribe(e -> System.out.println(Thread.currentThread().getName() + ",  " + e)));
    thread.start();
    Thread.sleep(100);
  }

  // SubscribeOn
  // 跟publishOn幾乎一模一樣，會去改變operator chain的threading context，
  // 差別在於publishOn只會改變之後的，不會朔及既往，而subscribeOn則是從頭到尾都改變，不論在哪一個位置，直到遇到publishOn為止，
  // 也就是如果有publishOn，那之後的operator 仍然會依照publishOn所指定的Scheduler。
  @Test
  void testSubscribeOn() throws InterruptedException {
    Scheduler s = Schedulers.newParallel("parallel-scheduler", 2);
    final Flux<String> flux = Flux
        .range(1, 2)
        .map(i -> 10 + i)
        .subscribeOn(s)
        .map(i -> "value " + i);
    Thread thread = new Thread(() ->
        flux.subscribe(e -> System.out.println(Thread.currentThread().getName() + ",  " + e)));
    thread.start();
    Thread.sleep(100);
  }

  @Test
  void testPublishOn2() throws InterruptedException {
    ArrayList<Integer> firstListOfUrls = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      firstListOfUrls.add(i);
    }

    ArrayList<String> secondListOfUrls = new ArrayList<>();
    secondListOfUrls.add("D");
    secondListOfUrls.add("E");

    Flux.fromIterable(firstListOfUrls) //contains A, B and C
        .subscribe(body -> System.out.println(
            Thread.currentThread().getName() + " from first list, got " + body));
    Flux.fromIterable(secondListOfUrls) //contains D and E
        .subscribe(body -> System.out.println(
            Thread.currentThread().getName() + " from second list, got " + body + "----------"));
    Thread.sleep(2000);
  }

  @Test
  void testPublishOn3() throws InterruptedException {
    ArrayList<Integer> firstListOfUrls = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      firstListOfUrls.add(i);
    }

    ArrayList<String> secondListOfUrls = new ArrayList<>();
    secondListOfUrls.add("D");
    secondListOfUrls.add("E");

    // .publishOn(Schedulers.boundedElastic())
    Flux.fromIterable(firstListOfUrls) //contains A, B and C
        .publishOn(Schedulers.boundedElastic())
        .subscribe(body -> System.out.println(
            Thread.currentThread().getName() + " from first list, got " + body));
    Flux.fromIterable(secondListOfUrls) //contains D and E
        .publishOn(Schedulers.boundedElastic())
        .subscribe(body -> System.out.println(
            Thread.currentThread().getName() + " from second list, got " + body + " ----------"));
    Thread.sleep(2000);
  }

  @Test
  void test1() throws InterruptedException {
    Flux<Integer> flux = Flux.range(0, 10)
        .map(i -> {
          System.out.println(
              "Mapping one for " + i + " is done by thread " + Thread.currentThread().getName());
          return i;
        })
        .publishOn(Schedulers.boundedElastic())
        .map(i -> {
          System.out.println(
              "Mapping two for " + i + " is done by thread " + Thread.currentThread().getName());
          return i;
        })
        .publishOn(Schedulers.parallel())
        .map(i -> {
          System.out.println(
              "Mapping three for " + i + " is done by thread " + Thread.currentThread().getName());
          return i;
        });
    flux.subscribe();

    System.out.println("-----------------------");

    Flux<Integer> flux2 = Flux.range(0, 2)
        .publishOn(Schedulers.immediate())
        .map(i -> {
          System.out.println("Mapping for " + i + " is done by thread " + Thread.currentThread().getName());
          return i;
        });
    flux2.subscribe();
  }
}
