package com.example.reactor2;

import com.example.reactor2.pojo.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.jws.soap.SOAPBinding.Use;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest
class Reactor2ApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testWebClient() throws JsonProcessingException {
		// request server host
		WebClient webClient = WebClient.create("http://localhost:8081");
		// getById
		String id  = "1";
		User userResult = webClient.get().uri("/test1/user/{id}", id)
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(User.class)
				.block();
		System.out.println(userResult.getName());

		// getAllUsers
		Flux<User> results = webClient.get().uri("/test1/users")
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToFlux(User.class);
		results.map(stu -> stu.getName()).buffer().doOnNext(System.out::println).blockFirst();

		// saveUser
		MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
		bodyValues.add("name", "Ray");
		bodyValues.add("gender", "male");
		bodyValues.add("age", "48");
		User user = new User("Paff", "female", 18);
		Mono<Void>  saveUser = webClient.post().uri("/test1/saveuser")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
//				.body(BodyInserters.fromFormData(bodyValues))
				.body(BodyInserters.fromPublisher(Mono.just(user), User.class))
				.retrieve().bodyToMono(Void.class);
		// saveUser.block();
		saveUser.subscribe();

		// saveUser
		ObjectMapper mapper = new ObjectMapper();
		User user2 = new User("Alex", "male", 33);
		String json = mapper.writeValueAsString(user2);
		Mono<Void>  saveUser2 = webClient.post().uri("/test1/saveuser")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(user2), User.class)
				.retrieve().bodyToMono(Void.class);
		// saveUser.block();
		saveUser2.subscribe();

		// getAllUsers
		Flux<User> results2 = webClient.get().uri("/test1/users")
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToFlux(User.class);
		results.map(stu -> stu.getName()).buffer().doOnNext(System.out::println).blockFirst();
	}
}
