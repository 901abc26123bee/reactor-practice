package com.example.reactor.service;

import com.example.reactor.pojo.User;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class UserServiceImpl implements UserService{
  private final Map<Integer, User> users = new HashMap<>();

  public UserServiceImpl(){
    users.put(1, new User("Lisa", "female", 20));
    users.put(2, new User("Atom", "male", 30));
    users.put(3, new User("Xeon", "male", 40));
  }

  @Override
  public Mono<User> getUserById(int id) {
    return Mono.justOrEmpty(this.users.get(id));
  }

  @Override
  public Flux<User> getAllUser() {
    return Flux.fromIterable(this.users.values());
  }

  @Override
  public Mono<Void> saveUserInfo(Mono<User> userMono) {
    return userMono.doOnNext((user) -> {
      int id = users.size() + 1;
      users.put(id, user);
    }).thenEmpty(Mono.empty());
  }
}
