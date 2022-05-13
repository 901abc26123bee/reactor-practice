package com.example.reactor.reactortest.zip;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

public class zipTest {
  // Operator
  // zip(static method), zipWith
  // 在異步條件下，數據流的流速不同，使用zip能夠一對一地將兩個或多個數據流的元素對齊發出。
  private Flux<String> getZipDescFlux() {
    String desc = "Zip two sources together, that is to say wait for all the sources to emit one element and combine these elements once into a Tuple2.";
    return Flux.fromArray(desc.split("\\s+"));  // 1
  }

  // CountDownLatch是多線程控制的一種工具，它被稱為门阀、计数器或者闭锁。
  // 這個工具經常用來用來協調多個線程之間的同步，或者說起到線程之間的通信（而不是用作互斥的作用）。下面我們就來一起認識一下CountDownLatch
  @Test
  public void testSimpleOperators() throws InterruptedException {
    System.out.println("******************* zip **********************");
    CountDownLatch countDownLatch = new CountDownLatch(1);  // 2
    Flux.zip(
            getZipDescFlux(),
            Flux.interval(Duration.ofMillis(200)))  // 3
        .subscribe(t -> System.out.println(t.getT1()), null, countDownLatch::countDown);    // 4
    countDownLatch.await(10, TimeUnit.SECONDS);     // 5
		/*
			1. 將英文說明用空格拆分為字符串流；
			2. 定義一個CountDownLatch，初始為1，則會等待執行1次countDown方法後結束，不使用它的話，測試方法所在的線程會直接返回而不會等待數據流發出完畢；
			3. 使用Flux.interval聲明一個每200ms發出一個元素的long數據流；因為zip操作是一對一的，故而將其與字符串流zip之後，字符串流也將具有同樣的速度；
			4. zip之後的流中元素類型為Tuple2，使用getT1方法拿到字符串流的元素；定義完成信號的處理為countDown;
			5. countDownLatch.await(10, TimeUnit.SECONDS)會等待countDown倒數至0，最多等待10秒鐘。
		* */

    System.out.println("******************** zipWith without CountDownLatch *********************");
    getZipDescFlux().zipWith(Flux.interval(Duration.ofMillis(200))).subscribe(System.out::println);
    System.out.println("******************** zipWith with CountDownLatch *********************");
    CountDownLatch countDownLatch2 = new CountDownLatch(1);
    getZipDescFlux().zipWith(Flux.interval(Duration.ofMillis(200)))
        .subscribe(t -> System.out.println(t.getT1()), null, countDownLatch::countDown);
    countDownLatch2.await(3, TimeUnit.SECONDS);
  }
}
