package com.example.webflux;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;


@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = PersonsController.class)
@AutoConfigureWebTestClient(timeout = "20000")//20 seconds
class ReactivePersonsControllerTest {

    @Autowired
    private WebTestClient webClient;

    WebClient client = WebClient.create("http://localhost:8989/");

    @MockBean
    PersonRepository personRepository;


    @Test
    void shouldGetPerson() {
        given(personRepository.findById(anyString()))
                .willReturn(Mono.just(new Person("1", "name")));
        var exchange = webClient.get()
                .uri("/api/persons/123")
                .accept(MediaType.APPLICATION_JSON)
                .exchange();

        exchange.expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("name");
    }

    @Test
    void shouldGetPersonWithBackpressure() {
        given(personRepository.findAll())
                .willReturn(Flux.just(new Person("1", "p1"),
                        new Person("2", "p2"),
                        new Person("3", "p3"),
                        new Person("4", "p4"),
                        new Person("5", "p5")
                ));
        webClient.get()
                .uri("/api/persons")
                .accept(MediaType.APPLICATION_JSON)
                .exchange().returnResult(Person.class)
                .getResponseBody()
                .log()
                .subscribe(new BaseSubscriber<>() {
                    int count = 0;

                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(2);
                    }

                    @Override
                    protected void hookOnNext(Person value) {
                        System.out.println(value);
                        count++;
                        if (count >= 2) {
                            count = 0;
                            request(2);
                        }
                    }


                });
    }

    @Test
    void shouldGetPersonWithWindow() {
        given(personRepository.findAll())
                .willReturn(Flux.just(new Person("1", "p1"),
                        new Person("2", "p2"),
                        new Person("3", "p3"),
                        new Person("4", "p4"),
                        new Person("5", "p5"),
                        new Person("6", "p6"),
                        new Person("7", "p7"),
                        new Person("8", "p8"),
                        new Person("9", "p9"),
                        new Person("10", "p10")
                ));
        webClient.get()
                .uri("/api/persons")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange().returnResult(Person.class)
                .getResponseBody()
                .window(2)
                .zipWith(Flux.interval(Duration.ZERO, Duration.ofSeconds(1)))
                .flatMap(Tuple2::getT1)
                .log()
                .subscribe(System.out::println);
    }

}
