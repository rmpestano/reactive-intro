package com.example.webflux;

import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
class AppInitializer {

    static final String PERSON_INDEX = "person";

    @Bean
    ApplicationListener<ApplicationReadyEvent> init(ReactiveElasticsearchClient client, PersonRepository personRepository) {
        return applicationReadyEvent -> {
            Mono<Boolean> indexExists = client.indices().existsIndex(new GetIndexRequest(PERSON_INDEX));
            Mono<Boolean> createIndex = indexExists.
                    map(exists -> {
                        if (!exists) {
                            client.indices().createIndex(new CreateIndexRequest(PERSON_INDEX));
                        }
                        return exists;
                    });
            createIndex.subscribe(System.out::println);

            var personsNames = Flux.just("Rafael", "Friso", "Elchin", "Paul", "Maarten", "Julian", "Gui", "Sandhya", "Elena", "Leo", "Mark");

            Mono<Void> deleteAll = personRepository.deleteAll();
            Flux<Person> createPersons = deleteAll.thenMany(personsNames)
                    .map(name -> Person.builder().name(name).build());
            //Flux<Mono<Person>> savePerson = createPersons.map(personRepository::save);
            Flux<Person> savedPersons = createPersons.flatMap(personRepository::save);
            savedPersons.subscribe(savedPerson -> System.out.println(savedPerson));
        };
    }
}
