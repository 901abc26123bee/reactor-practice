package com.example.reactor.reactortest.createAndgenerate;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

public class CreateVSGenerate {
  // generate
  @Test
  void test12() {
    Flux.generate(sink -> {
          sink.next(System.currentTimeMillis());
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        })
        .subscribe(System.out::println);

    // 指定給 generate 的引數稱為產生器（generator），它會被不斷地呼叫，
    // sink 的型態是 SynchronousSink，要發佈訊號的話可透過它的 next 方法，
    // 在產生器的一次呼叫過程中，next 只能被呼叫一次，
    // 若要結束訊號的發佈，可以使用 complete 方法，
    // 上面的範例沒有呼叫 complete，資料就會一直發佈至資料流。
  }

  @Test
  void test13() {
    // 發佈 10 次系統時間，就結束訊號發佈的例子
    Flux.generate(
            () -> new AtomicInteger(0),
            (counter, sink) -> {
              if(counter.get() == 10) {
                sink.complete();
              }
              sink.next(System.currentTimeMillis());
              counter.incrementAndGet();
              try {
                Thread.sleep(1000);
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
              return counter;
            }
        )
        .subscribe(System.out::println);
  }

  // create
  @Test
  void test14() {
    Flux.create(sink -> {
      for (int i = 0; i < 10; i++) {
        sink.next(i);
      }
      sink.complete();
    }).subscribe(System.out::println);

    System.out.println("------------------------------------");
  }

  // create vs generate
  @Test
  void teset16() throws InterruptedException {
    Flux<Integer> integerFlux = Flux.create((FluxSink<Integer> fluxSink) -> {
      IntStream.range(0, 5)
          .peek(i -> System.out.println("going to emit - " + i))
          .forEach(fluxSink::next);
    });
    //First observer. takes 1 ms to process each element
    integerFlux.delayElements(Duration.ofMillis(1)).subscribe(i -> System.out.println("First :: " + i));

    //Second observer. takes 2 ms to process each element
    integerFlux.delayElements(Duration.ofMillis(2)).subscribe(i -> System.out.println("Second:: " + i));
    Thread.sleep(500);  // 確保序列執行完
  }

  @Test
  void test17() {
    Flux<Integer> integerFlux = Flux.create((FluxSink<Integer> fluxSink) -> {
      IntStream.range(0, 5)
          .peek(i -> System.out.println("going to emit - " + i))
          .forEach(fluxSink::next);
    }, FluxSink.OverflowStrategy.DROP);
  }

  // test generate
  public class SequenceGenerator {
    public Flux<Integer> generateFibonacciWithTuples() {
      return Flux.generate(
          () -> Tuples.of(0, 1),
          (state, sink) -> {
            sink.next(state.getT1());
            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
          }
      );
    }

    public Flux<Integer> generateFibonacciWithCustomClass(int limit) {
      return Flux.generate(
          () -> new FibonacciState(0, 1),
          (state, sink) -> {
            sink.next(state.getFormer());
            if (state.getLatter() > limit) {
              sink.complete();
            }
            int temp = state.getFormer();
            state.setFormer(state.getLatter());
            state.setLatter(temp + state.getLatter());
            return state;
          });
    }
  }
  @Test
  public void whenGeneratingNumbersWithTuplesState_thenFibonacciSequenceIsProduced() {
    SequenceGenerator sequenceGenerator = new SequenceGenerator();
    Flux<Integer> fibonacciFlux = sequenceGenerator.generateFibonacciWithTuples().take(5).log();

    StepVerifier.create(fibonacciFlux)
        .expectNext(0, 1, 1, 2, 3)
        .expectComplete()
        .verify();
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public class FibonacciState {
    private int former;
    private int latter;

    // constructor, getters and setters
  }

  @Test
  public void whenGeneratingNumbersWithCustomClass_thenFibonacciSequenceIsProduced() {
    SequenceGenerator sequenceGenerator = new SequenceGenerator();

    StepVerifier.create(sequenceGenerator.generateFibonacciWithCustomClass(10))
        .expectNext(0, 1, 1, 2, 3, 5, 8)
        .expectComplete()
        .verify();
  }
}
