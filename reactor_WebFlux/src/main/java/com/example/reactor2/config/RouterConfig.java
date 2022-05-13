package com.example.reactor2.config;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import com.example.reactor2.hanlder.TimeHandler;
import com.example.reactor2.hanlder.UserHandler;
import com.example.reactor2.service.UserService;
import com.example.reactor2.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {

  // 1. Create Router
  @Bean
  public RouterFunction<ServerResponse> routingFunction() {
    UserService userService = new UserServiceImpl();
    UserHandler userHandler = new UserHandler(userService);
    return RouterFunctions.route(
            GET("/test2/users/{id}").and(accept(MediaType.APPLICATION_JSON)), userHandler::getUserById)
        .andRoute(GET("/test2/users").and(accept(MediaType.APPLICATION_JSON)), userHandler::getAllUsers)
        .andRoute(POST("/test2/saveuser").and(accept(MediaType.APPLICATION_JSON)), userHandler::saveUser);
  }

  @Autowired
  TimeHandler dateHandler;
  @Bean
  public RouterFunction<ServerResponse> routingFunction2() {
    return RouterFunctions.route(GET("/test3/time"), dateHandler::getTime)
            .andRoute(GET("/test3/date"), dateHandler::getDate)
            .andRoute(GET("/test3/times"), dateHandler::sendTimePerSec);
  }
}
