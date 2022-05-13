package com.example.reactor2.hanlder;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TimeHandler {

  public Mono<ServerResponse> getTime(ServerRequest request) {
    return ok().contentType(MediaType.TEXT_PLAIN).body(
            Mono.just("Now is " + new SimpleDateFormat("HH:mm:ss").format(new Date())), String.class);
  }

  public Mono<ServerResponse> getDate(ServerRequest request) {
    return ok().contentType(MediaType.TEXT_PLAIN).body(
            Mono.just("Today is " + new SimpleDateFormat("yyyy-MM-dd").format(new Date())), String.class);
  }

  public Mono<ServerResponse> sendTimePerSec(ServerRequest serverRequest) {
    return ok().contentType(MediaType.TEXT_EVENT_STREAM).body(  // 1
        Flux.interval(Duration.ofSeconds(1)).   // 2
            map(l -> new SimpleDateFormat("HH:mm:ss").format(new Date())),
        String.class);
        //  MediaType.TEXT_EVENT_STREAM表示Content-Type為text/event-stream，即SSE；Server Send Event），
        //  在客戶端發起一次請求後會保持該連接，服務器端基於該連接持續向客戶端發送數據，從HTML5開始加入。
        //  利用interval生成每秒一個數據的流。
  }
}
