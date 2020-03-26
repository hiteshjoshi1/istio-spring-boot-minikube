package com.hitesh.demo;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	ApplicationRunner init(KayakRepository repository) {

		Object[][] data = { { "sea", "Andrew", 300.12, "NDK" }, { "creek", "Andrew", 100.75, "Piranha" },
				{ "loaner", "Andrew", 75, "Necky" } };

		return args -> {
			repository.deleteAll().thenMany(Flux.just(data).map(array -> {
				return new Kayak((String) array[0], (String) array[1], (Number) array[2], (String) array[3]);
			}).flatMap(repository::save)).thenMany(repository.findAll())
					.subscribe(kayak -> System.out.println("saving " + kayak.toString()));

		};
	}

}
