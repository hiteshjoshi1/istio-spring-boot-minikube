package com.hitesh.demo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface KayakRepository extends ReactiveMongoRepository<Kayak, Long> {
}