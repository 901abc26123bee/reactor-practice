package com.example.reactor;


import static org.hamcrest.MatcherAssert.assertThat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.logging.Log;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

@SpringBootTest
class ReactorApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	public void test3() {
		ArrayList<String> strs = new ArrayList<>();
		strs.add("JJJ");
		strs.add("KKK");
		strs.add("QQQ");
		Flux<Integer> flux1 =  Flux.just(1,2,3).empty();
		Stream<String> stream = strs.stream();
		Flux<String> flux2 = Flux.fromStream(stream);

		Mono<String> helloWorld = Mono.just("Hello World").justOrEmpty("empty");
		Mono.just("Hello World").subscribe(System.out::println);
		Flux.just(1,2,3,4,5).subscribe(System.out::println);
		System.out.println();
		Flux<String> manyWords = Flux.fromIterable(strs).doOnNext(System.out::println);
		System.out.println("+++++++++++++++++");
	}

	@Test
	public void test4() {
		final Random random = new Random();
		Flux.generate(ArrayList::new, (list, sink) -> {
			int value = random.nextInt(100);
			list.add(value);
			sink.next(value);
			if (list.size() == 10) {
				sink.complete();
			}
			return list;
		}).subscribe(System.out::println);

		System.out.println("-------------------------------");

		Flux.create(sink -> {
			for (int i = 0; i < 10; i++) {
				sink.next(i);
			}
			sink.complete();
		}).subscribe(System.out::println);
	}

	@Test
	public void test5() {
		// 創建一個不包含任何消息通知的序列
		Flux.range(1, 10)
				.timeout(Flux.never(), v -> Flux.never())
				.subscribe(System.out::println);

		System.out.println("-------------------------------------");
		Flux.generate(sink -> {
			sink.next("Hello");
			sink.complete();
		}).subscribe(System.out::println);

		System.out.println("-------------------------------------");
		final Random random = new Random();
		Flux.generate(ArrayList::new, (list, sink) -> {
			int value = random.nextInt(100);
			list.add(value);
			sink.next(value);
			if (list.size() == 10) {
				sink.complete();
			}
			return list;
		}).subscribe(System.out::println);

		System.out.println("-------------------------------------");
		Flux.create(sink -> {
			for (int i = 0; i < 10; i++) {
				sink.next(i);
			}
			sink.complete();
		}).subscribe(System.out::println);
	}

	@Test
	void test6() {
		Mono.fromSupplier(() -> "Hello").subscribe(System.out::println);
		Mono.justOrEmpty(Optional.of("Hello")).subscribe(System.out::println);
		Mono.create(sink -> sink.success("Hello")).subscribe(System.out::println);

		Flux.range(1, 100).buffer(20).subscribe(System.out::println);
		Flux.range(1, 10).bufferUntil(i -> i % 2 == 0).subscribe(System.out::println);
		Flux.range(1, 10).bufferWhile(i -> i % 2 == 0).subscribe(System.out::println);
	}

	@Test
	void test7() {
		Flux.just(1, 2, 3, 4, 5, 6).subscribe(
				System.out::println,
				System.err::println,
				() -> System.out.println("Completed!"));

		Mono.error(new Exception("some error")).subscribe(
				System.out::println,
				System.err::println,
				() -> System.out.println("Completed!")
		);
	}


	// Unit Test : StepVerifier
	private Flux<Integer> generateFluxFrom1To6() {
		return Flux.just(1, 2, 3, 4, 5, 6);
	}
	private Mono<Integer> generateMonoWithError() {
		return Mono.error(new Exception("some error"));
	}
	@Test
	public void testViaStepVerifier() {
		StepVerifier.create(generateFluxFrom1To6())
				.expectNext(1, 2, 3, 4, 5, 6)
				.expectComplete()
				.verify();
		StepVerifier.create(generateMonoWithError())
				.expectErrorMessage("some error")
				.verify();
	}

	// Unit Test : StepVerifier
	// Operator
	// map, flatMap, filter
	@Test
	void test8() {
//		Flux.range(1, 6).map(e -> e + 2).delayElements(Duration.ofMillis(100)).subscribe(System.out::println);
		StepVerifier.create(Flux.range(1, 6)    // 1
						.map(i -> i * i))   // 2
				.expectNext(1, 4, 9, 16, 25, 36)    //3
				.expectComplete();  // 4

		System.out.println("*****************************************");
		// flatMap通常用於每個元素又會引入數據流的情況  -->  ex: s -> Flux.fromArray(s.split("\\s*")
		Flux.just("flux", "mono")
				.flatMap(s -> Flux.fromArray(s.split("\\s*"))   // 1
						.delayElements(Duration.ofMillis(100))) // 2
				.doOnNext(System.out::print); // peek==不會消費數據流, output: fmolnuxo
		StepVerifier.create(
						Flux.just("flux", "mono")
								.flatMap(s -> Flux.fromArray(s.split("\\s*"))   // 1
										.delayElements(Duration.ofMillis(100))) // 2
								.doOnNext(System.out::print)) // 3
				.expectNextCount(8) // 4
				.verifyComplete();
		/*
		* 1. 對於每一個字符串s，將其拆分為包含一個字符的字符串流；
			2. 對每個元素延遲100ms；
			3. 對每個元素進行打印（注doOnNext方法是“偷窺式”的方法，不會消費數據流）；
			4. 驗證是否發出了8個元素。
		* */

		StepVerifier.create(Flux.range(1, 6)
						.filter(i -> i % 2 == 1)    // 1
						.map(i -> i * i))
				.expectNext(1, 9, 25)   // 2
				.verifyComplete();
	}




	// Schedulers
	// 正常情況下，調用這個方法會被阻塞 2 秒鐘，然後同步地返回結果。
	// 我們藉助elastic調度器將其變為異步，由於是異步的，為了保證測試方法所在的線程能夠等待結果的返回，我們使用CountDownLatch：
	private String getStringSync() {
		try {
			System.out.println("start");
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "Hello, Reactor!";
	}

	@Test
	public void testSyncToAsync() throws InterruptedException {
		System.out.println("---------------- start ----------------");
		CountDownLatch countDownLatch = new CountDownLatch(1);
		Mono.fromCallable(() -> getStringSync())    // 1
				.subscribeOn(Schedulers.elastic())  // 2
				.subscribe(System.out::println, null, countDownLatch::countDown);
		countDownLatch.await(10, TimeUnit.SECONDS);
		System.out.println("---------------- end ----------------");
		/*
		* 1. 使用fromCallable聲明一個基於Callable的Mono；
			2. 使用subscribeOn將任務調度到Schedulers內置的彈性線程池執行，彈性線程池會為Callable的執行任務分配一個單獨的線程。
		* */
	}

	@Test
	void test9() {
		Flux.range(1, 50)
				.map(e -> e % 11)
				.publishOn(Schedulers.elastic()).filter(e -> e % 2 != 0)
				.publishOn(Schedulers.parallel()).flatMap(e -> Flux.range(0, e))
				.subscribeOn(Schedulers.single()).subscribe(System.out::print);
		/*
		 * 1. publishOn會影響鏈中其後的操作符，比如第一個publishOn調整調度器為elastic，則filter的處理操作是在彈性線程池中執行的；同理，flatMap是執行在固定大小的parallel線程池中的；
		 * 2. subscribeOn無論出現在什麼位置，都只影響源頭的執行環境，也就是range方法是執行在單線程中的，直至被第一個publishOn切換調度器之前，所以range後的map也在單線程中執行。
		 */
	}

	@Test
	public void testErrorHandling() {
		Flux.range(1, 6)
				.map(i -> 10/(i-3)) // 1
				.map(i -> i*i)
				.subscribe(System.out::println, System.err::println);
	}

	@Test
	void testErrorHandling2() throws InterruptedException {
		// 1. 捕獲並返回一個靜態的缺省值
		//		onErrorReturn方法能夠在收到錯誤信號的時候提供一個缺省值：
		Flux.range(1, 6)
				.map(i -> 10/(i-3)) // error occur at i=3
				.onErrorReturn(0)   // 當發生異常時提供一個缺省值0
				.map(i -> i*i)
				.subscribe(System.out::println, System.err::println);

		System.out.println("------------------------------------------");
		// 2. 捕獲並執行一個異常處理方法或計算一個候補值來頂替
		//		onErrorResume方法能夠在收到錯誤信號的時候提供一個新的數據流：
		Flux.range(1, 6)
				.map(i -> 10/(i-3))
				.onErrorResume(e -> Mono.just(new Random().nextInt(6))) // 提供新的数据流
				.map(i -> i*i)
				.subscribe(System.out::println, System.err::println);
		/*
			Flux.just(endpoint1, endpoint2)
					.flatMap(k -> callExternalService(k))   // 1. 調用外部服務；
					.onErrorResume(e -> getFromCache(k));   // 2. 如果外部服務異常，則從緩存中取值代替。
		* */

		System.out.println("------------------------------------------");
		// 3. 捕獲，並再包裝為某一個業務相關的異常，然後再拋出業務異常
		//		有時候，我們收到異常後並不想立即處理，而是會包裝成一個業務相關的異常交給後續的邏輯處理，可以使用onErrorMap方法：
//		Flux.just("timeout1")
//				.flatMap(k -> callExternalService(k))   // 1
//				.onErrorMap(original -> new BusinessException("SLA exceeded", original)); // 2

		// 4. 捕獲，記錄錯誤日誌，然後繼續拋出
		//     如果對於錯誤你只是想在不改變它的情況下做出響應（如記錄日誌），並讓錯誤繼續傳遞下去， 那麼可以用doOnError 方法。
		//     前面提到，形如doOnXxx是只讀的，對數據流不會造成影響：

//		Flux.just(endpoint1, endpoint2)
//				.flatMap(k -> callExternalService(k))
//				.doOnError(e -> {   // 1
//					log("uh oh, falling back, service failed for key " + k);    // 2
//				})
//				.onErrorResume(e -> getFromCache(k));

		// 5. 使用finally 來清理資源，或使用Java 7 引入的"try-with-resource"
//		Flux.using(
//				() -> getResource(),    // 1
//				resource -> Flux.just(resource.getAll()),   // 2
//				MyResource::clean   // 3
//		);

		System.out.println("------------------------------------------");
		// retry
		// *retry對於上游Flux是採取的重訂閱（re-subscribing）的方式，因此重試之後實際上已經一個不同的序列了， 發出錯誤信號的序列仍然是終止了的。舉例如下：
		// 可見，retry不過是再一次從新訂閱了原始的數據流，從1開始。第二次，由於異常再次出現，便將異常傳遞到下游了。
		Flux.range(1, 6)
				.map(i -> 10 / (3 - i))
				.retry(3)
				.subscribe(System.out::println, System.err::println);
		Thread.sleep(100);  // 確保序列執行完
	}

	@Test
	public void testBackpressure() {
		// 假設，我們現在有一個非常快的Publisher—— Flux.range(1, 6)，然後自定義一個每秒處理一個數據元素的慢的Subscriber，Subscriber就需要通過request(n)的方法來告知上游它的需求速度。代碼如下：
		Flux.range(1, 6)    // 1
				.doOnRequest(n -> System.out.println("Request " + n + " values..."))    // 2
				.subscribe(new BaseSubscriber<Integer>() {  // 3
					@Override
					protected void hookOnSubscribe(Subscription subscription) { // 4
						System.out.println("Subscribed and make a request...");
						request(1); // 5
					}

					@Override
					protected void hookOnNext(Integer value) {  // 6
						try {
							TimeUnit.SECONDS.sleep(1);  // 7
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("Get value [" + value + "]");    // 8
						request(1); // 9
					}
				});
		// 1. Flux.range是一個快的Publisher；
		// 2. 在每次request的時候打印request個數；
		// 3. 通過重寫BaseSubscriber的方法來自定義Subscriber；
		// 4. hookOnSubscribe定義在訂閱的時候執行的操作；
		// 5. 訂閱時首先向上游請求1個元素；
		// 6. hookOnNext定義每次在收到一個元素的時候的操作；
		// 7. sleep 1秒鐘來模擬慢的Subscriber；
		// 8. 打印收到的元素；
		// 9. 每次處理完1個元素後再請求1個。
	}

	@Test
	void test10() {
		AtomicInteger sum = new AtomicInteger(0);
		Flux
				.just(1, 2, 3, 4)
				.reduce(Integer::sum)
				.subscribe(sum::set);
		System.out.println("Sum is: {}  "+ sum.get());
		// 它將 10 打印到控制台上，因為Flux.just()默認情況下使用當前線程，因此當它到達日誌記錄語句時已經計算了結果。

		AtomicInteger sum2 = new AtomicInteger(0);
		Flux
				.just(1, 2, 3, 4)
				.subscribeOn(Schedulers.elastic())
				.reduce(Integer::sum)
				.subscribe(sum2::set);
		System.out.println("Sum is: {}  "+ sum2.get());
		// 這種情況下的答案是0，因為 using subscribeOn()將在不同的調度程序工作線程上執行訂閱，使其異步。

		Flux.just(1, 2, 3, 4)
				.reduce(Integer::sum)
				.subscribe(System.out::println);
	}

	@Test
	void test11() {
		Flux<Integer> numbers = Flux
				.just(1, 2, 3, 4)
				.log()
				.share();
		numbers
				.reduce(Integer::sum)
				.subscribe(System.out::println); // 10

		numbers
				.reduce((a, b) -> a * b)
				.subscribe(System.out::println); // 24
	}


	// block() vs subscribe
	@Test
	void test15() throws InterruptedException {
		// subscribe()另一方面，您的調用在Mono單獨的調度程序上異步執行，讓您的主線程完成。由於默認使用的調度程序使用守護線程來執行您的訂閱，因此您的程序在終止之前不會等待它完成。
		Flux.just(1,2,3,4)
				.delayElements(Duration.ofMillis(100))
				.log()
				.subscribe();
		Thread.currentThread().sleep(500);
		System.out.println("------------------------------");
		// block()調用明確地持有主線程，直到發布者完成。當它完成時，它執行了map()調用，因此打印了值。
		Flux.just(1,2,3,4)
				.delayElements(Duration.ofMillis(100))
				.log()
				.blockLast();
		System.out.println("------------------------------");
		Flux.just(1,2,3,4)
				.delayElements(Duration.ofMillis(100))
				.log()
				.blockFirst();
	}



	// ---------------------------------------------------------------------------
	// hot cold publisher
	@Test
	public void defer(){
		//声明阶段创建DeferClass对象

		Mono<Date> m1 = Mono.just(new Date());
		Mono<Date> m2 = Mono.defer(()->Mono.just(new Date()));
		m1.subscribe(System.out::println);
		System.out.println("-----------");
		m2.subscribe(System.out::println);
		//延迟5秒钟
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("*********** after 5 seconds ************");
		m1.subscribe(System.out::println);
		System.out.println("-----------");
		m2.subscribe(System.out::println);
	}

}
