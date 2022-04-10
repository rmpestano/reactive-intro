package com.example.webflux;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/persons", produces = APPLICATION_JSON_VALUE)
class PersonsController {

    private final PersonRepository personRepository;

    @GetMapping
    public Flux<Person> listPerson() {
        return personRepository.findAll()
                //.limitRate(5) //uncomment to handle 1k requests  ab -n 10000 -c 1000 http://localhost:8080/api/persons
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Person>> getPerson(@PathVariable("id") String id) {
        return personRepository.findById(id)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @GetMapping("/name")
    public Mono<ResponseEntity<Person>> findByName(@RequestParam("name") String name) {
        return personRepository.findByNameContaining(name)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "delay", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Person> getPersonsDelayed() {
        return personRepository.findAll()
                .delayElements(Duration.ofSeconds(2));
    }

    @GetMapping(value = "events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PersonEvent> getPersonEvents() {
        return personRepository.findAll()
                .zipWith(Flux.interval(Duration.ofSeconds(2)))
                .map(personAndTime -> new PersonEvent(personAndTime.getT1().name(), now()));
    }

    record PersonEvent(String name, LocalDateTime time) {
    }

}
