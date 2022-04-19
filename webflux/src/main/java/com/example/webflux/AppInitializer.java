package com.example.webflux;

import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import reactor.core.publisher.Flux;

@Configuration
class AppInitializer {

    static final String PERSON_INDEX = "person";

    @Bean
    ApplicationListener<ApplicationReadyEvent> init(ReactiveElasticsearchClient client, PersonRepository personRepository) {
        return applicationReadyEvent -> {
            client.indices().existsIndex(new GetIndexRequest(PERSON_INDEX))
                    .map(exists -> {
                        if (!exists) {
                            client.indices().createIndex(new CreateIndexRequest(PERSON_INDEX));
                        }
                        return exists;
                    })
                    .then(personRepository.deleteAll())
                    .thenMany(Flux.just("Rafael", "Friso", "Gui", "Elchin", "Paul", "Maarten", "Julian", "Sandhya", "Elena", "Leo", "Mark"))
                    .map(name -> Person.builder().name(name).build())
                    .flatMap(personRepository::save)
                    .subscribe(System.out::println);
        };
    }
}
