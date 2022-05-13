package com.example.reactor.controller;

import com.example.reactor.pojo.User;
import com.example.reactor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// Annotation version
@RestController
@RequestMapping("/test1")
public class UserController {
  @Autowired
  private UserService userService;

  @GetMapping("/user/{id}")
  public Mono<User> getUserById(@PathVariable int id) {
    return userService.getUserById(id);
  }

  @GetMapping("/users")
  public Flux<User> getUsers() {
    return userService.getAllUser();
  }

  @PostMapping("/saveuser")
  public Mono<Void> saveUser(@RequestBody User user) {
    Mono<User> userMono = Mono.just(user);
    return userService.saveUserInfo(userMono);
  }

}

