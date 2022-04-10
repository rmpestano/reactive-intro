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
        /*client.get()
                .uri("api/persons")
                .retrieve().bodyToFlux(Person.class)
                .limitRate(1)
                .subscribe(new Subscriber<Person>() {

                    int count = 0;

                    @Override
                    public void onSubscribe(Subscription subscription) {
                        if (count >= 2) {
                            count = 0;
                            subscription.request(2);
                        }
                    }

                    @Override
                    public void onNext(Person person) {
                        count++;
                        System.out.println(person);

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });*/

    }

}
