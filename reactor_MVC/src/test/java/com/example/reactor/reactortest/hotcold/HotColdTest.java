package com.example.reactor.reactortest.hotcold;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class HotColdTest {
  /*
  reactor的兩種時間
    1. Assembly Time
        我們學習了一些Flux或是Mono的operator，像是map、flatMap、filter......等等，
        也知道在宣告整個Flux的過程中並不會馬上觸發這些operators的行為，因為我們知道Reactor是lazy的，
        而這個在宣告並且串聯operators的時刻就被稱作是「Assembly Time」。
    2. Subscription Time
        在Assembly Time時，我們宣告並組合了Flux，但並不會馬上的開始執行，而是直到有人訂閱(subscription)，
        而官方推薦最簡單的訂閱方式就是「Flux.subscribe(valueConsumer, errorConsumer)」

        講了這麼多次當subscribe觸發後才開始動作，其實觸發的方式有三種。
          1. subscribe：之前介紹都是屬於這種
          2. block：Flux.blockFirst，阻斷也會觸發。
          3. hot publisher：接下來就來說明第三種。
  */
  // 直播、首播就像是 Hot Publisher，每個進來頻道的人都是從最新的地方開始，進入的時間點不同得到的資料就會不同，就算沒有人看影片還是持續在跑動。
  // 一般的影片就是像是 Cold Publisher，在沒有點開之前是不會做事，每個人點開都一樣是從最一開始的地方。
  @Test
  void testCold() throws InterruptedException {
    Flux<Long> clockTicks = Flux.interval(Duration.ofSeconds(1));

    clockTicks.subscribe(tick -> System.out.println("clock1 " + tick + "s"));
    Thread.sleep(2000);

    clockTicks.subscribe(tick -> System.out.println("\tclock2 " + tick + "s"));
    Thread.sleep(5000);
  }

  @Test
  void testHot() throws InterruptedException {
    Flux<Long> source = Flux.interval(Duration.ofSeconds(1));
    Flux<Long> clockTicks = source.share(); // share() --> turn cold to hot
    clockTicks.subscribe(tick -> System.out.println("clock1 " + tick + "s"));
    Thread.sleep(2000);

    clockTicks.subscribe(tick -> System.out.println("\tclock2 " + tick + "s"));
    Thread.sleep(5000);
  }

  // Cold Publisher是Lazy loading，但其實just在assembly time就已經準備好所有的data，當有Subscriber訂閱只是在replay一次而已。

  // 使用Flux.just的時候，在assembly time就已經取得資料，我們可以改成透過defer，回歸lazy，直到subscribe才會取資料，
  // defer就是延遲的意思，透過Supplier來達到延遲的效果，來源資料會被延遲到subscribe才會被初始化，
  // 也是因為這樣，如果來源資料是與當下時間有關，這樣每個不同的subscribe都會取到不同的時間，但本質上還是一個Cold Publisher。

  // 如果是lazy的要等到subscribe才會觸發的就是COLD Publisher，如果無論有沒有subscribe都會先取得資料的，則是Hot Publisher

  @Test
  void testJustAndDefer() throws InterruptedException {
    // just --> hot
    // defer --> cold , lazy 直到subscribe才會取資料
    Mono<Long> clock = Mono.just(System.currentTimeMillis());
    Mono<Long> clockDefer = Mono.defer(() -> Mono.just(System.currentTimeMillis()));

    Thread.sleep(500);
    clock.subscribe(t -> System.out.println("clock:"+ t));
    clockDefer.subscribe(t -> System.out.println("\tclockDefer:"+ t));

    Thread.sleep(500);
    clock.subscribe(t -> System.out.println("clock:"+ t));
    clockDefer.subscribe(t -> System.out.println("\tclockDefer:"+ t));
  }
}
