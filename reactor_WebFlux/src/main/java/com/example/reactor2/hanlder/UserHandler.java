package com.example.reactor2.hanlder;

import com.example.reactor2.pojo.User;
import com.example.reactor2.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// 2. Create Hanlder
@Component
public class UserHandler {

  private final UserService userService;

  public UserHandler(UserService userService) {
    this.userService = userService;
  }

  public Mono<ServerResponse> getUserById(ServerRequest request) {
    int userId = Integer.valueOf(request.pathVariable("id"));
    Mono<User> userMono = this.userService.getUserById(userId);
    return userMono.flatMap(user -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(user)))
        .switchIfEmpty(ServerResponse.notFound().build());
  }

  public Mono<ServerResponse> getAllUsers(ServerRequest request) {
    Flux<User> users = userService.getAllUser();
    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(users, User.class);
  }

  public Mono<ServerResponse> saveUser(ServerRequest request) {
    Mono<User> userMono = request.bodyToMono(User.class);
    return ServerResponse.ok().build(userService.saveUserInfo((userMono)));
  }
}