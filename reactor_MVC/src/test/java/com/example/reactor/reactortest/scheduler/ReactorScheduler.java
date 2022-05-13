package com.example.reactor.reactortest.scheduler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.scheduler.VirtualTimeScheduler;

public class ReactorScheduler {
  @Test
  void testDefaultScheduler() throws InterruptedException {
    List<Long> list = new ArrayList<>();
    Flux
        .interval(Duration.ofSeconds(10), Duration.ofSeconds(5))
        .take(3)
        .subscribe(list::add);

    Thread.sleep(10500);
    System.out.println(list);
    Thread.sleep(5000);
    System.out.println(list);
    Thread.sleep(5000);
    System.out.println(list);
    /*
    [0]
    [0, 1]
    [0, 1, 2]
    */
  }

  @Test
  void testVirtualTimeScheduler() throws InterruptedException {
    List<Long> list = new ArrayList<>();

    VirtualTimeScheduler scheduler = VirtualTimeScheduler.getOrSet();
    Flux
        .interval(Duration.ofSeconds(10), Duration.ofSeconds(5), scheduler)
        .take(3)
        .subscribe(list::add);

    scheduler.advanceTimeBy(Duration.ofSeconds(10));
    System.out.println(list);
    scheduler.advanceTimeBy(Duration.ofSeconds(5));
    System.out.println(list);
    scheduler.advanceTimeBy(Duration.ofSeconds(5));
    System.out.println(list);
    /*
    [0]
    [0, 1]
    [0, 1, 2]
    */
  }
}
