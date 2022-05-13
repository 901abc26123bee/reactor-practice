package com.example.reactor.reactortest.map;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class MapTest {
  // map vs flatmap
  @Test
  void testmap() {
    System.out.println("------ map --------");
    Function<String, String> mapper = String::toUpperCase;
    Flux<String> inFlux = Flux.just("baeldung", ".", "com");
    Flux<String> outFlux = inFlux.map(mapper);

    StepVerifier.create(outFlux)
        .expectNext("BAELDUNG", ".", "COM")
        .expectComplete()
        .verify();

    System.out.println("------ flatmap --------");
    Function<String, Publisher<String>> mapper2 = s -> Flux.just(s.toUpperCase().split("")).share();

    Flux<String> inFlux2 = Flux.just("baeggghhhhpppppp", "......", "ccccooomm").share();
    Flux<String> outFlux2 = inFlux2.flatMap(mapper2);

    List<String> output = new ArrayList<>();
    outFlux2.subscribe(output::add);
    for (String s : output) {
      System.out.print(s);
    }
  }
}
