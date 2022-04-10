package com.example.mvc;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
class AppInitializer {

    static final String PERSON_INDEX = "person";

    @Bean
    ApplicationListener<ApplicationReadyEvent> init(RestHighLevelClient client, PersonRepository personRepository) {
        return applicationReadyEvent -> {
            try {
                if (!client.indices().exists(new GetIndexRequest(PERSON_INDEX), RequestOptions.DEFAULT)) {
                    client.indices().create(new CreateIndexRequest(PERSON_INDEX), RequestOptions.DEFAULT);
                }
                personRepository.deleteAll();
                var personsNames = List.of("Rafael", "Friso", "Elchin", "Paul", "Maarten", "Julian", "Gui", "Sandhya", "Elena", "Leo", "Mark");

                personsNames.stream()
                        .map(name -> Person.builder().name(name).build())
                        .map(personRepository::save)
                        .forEach(System.out::println);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }
}
