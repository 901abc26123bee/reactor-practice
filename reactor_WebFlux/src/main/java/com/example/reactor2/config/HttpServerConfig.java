package com.example.reactor2.config;

import static org.springframework.web.reactive.function.server.RouterFunctions.toHttpHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.netty.http.server.HttpServer;

@Configuration
public class HttpServerConfig {

  @Autowired
  private Environment environment;

  @Autowired
  RouterConfig routerConfig;

  // 3.Finish Adapter
  @Bean
  public void createReactorServer() {
    RouterFunction<ServerResponse> route = routerConfig.routingFunction();
    HttpHandler httpHandler = toHttpHandler(route);
    ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);

    // create server
    HttpServer httpServer = HttpServer.create();
    httpServer.handle(adapter).bindNow();
  }
}
