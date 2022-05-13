package com.example.reactor2;

import com.example.reactor2.pojo.User;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

public class Client {

  public static void main(String[] args) {
    // request server host
    WebClient webClient = WebClient.create("http://localhost:8082");

    String id  = "1";
    User userResult = webClient.get().uri("/test2/users/{id}")
        .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(User.class)
        .block();
    System.out.println(userResult.getName());

    Flux<User> results = webClient.get().uri("/test2/users")
        .accept(MediaType.APPLICATION_JSON).retrieve().bodyToFlux(User.class);
    results.map(stu -> stu.getName()).buffer().doOnNext(System.out::println).blockFirst();
  }
}
