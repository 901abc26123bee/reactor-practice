package com.example.reactor2.service;

import com.example.reactor2.pojo.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
  Mono<User> getUserById(int id);

  Flux<User> getAllUser();

  Mono<Void> saveUserInfo(Mono<User> user);
}