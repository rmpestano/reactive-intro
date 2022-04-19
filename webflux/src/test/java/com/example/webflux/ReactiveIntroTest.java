package com.example.webflux;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


class ReactiveIntroTest {


    @Test
    void nothingHappens() {
        Mono.just("data")
                .log()
                .map(data -> data.toUpperCase());
    }

    @Test
    void monoSubscribe() {
        final AtomicReference<String> dataResult = new AtomicReference<>();
        Mono.just("data")
                .log()
                .map(data -> data.toUpperCase())
                .subscribe(result -> {
                    dataResult.set(result);
                    assertThat(result).isEqualTo("will throw ex in reactor thread");
                });

        assertThat(dataResult.get()).isEqualTo("DATA");
    }

    @Test
    void monoBlock() {
        String result = Mono.just("data")
                .log()
                .map(data -> data.toUpperCase())
                .block();
        assertThat(result).isEqualTo("DATA");
    }

    @Test
    void monoError() {
        Mono error = Mono.just("data")
                .map(data -> {
                    int a = 1 / 0;
                    return data;
                }).log();

        StepVerifier.create(error)
                .expectNextCount(0)
                .expectError(ArithmeticException.class)
                .verify();

        assertThatThrownBy(() -> error.block()).isInstanceOf(ArithmeticException.class)
                .hasMessage("/ by zero");

    }

    @Test
    void monoErrorReturn() {
        String error = Mono.just("data")
                .map(data -> {
                    int a = 1 / 0;
                    return data;
                })
                .onErrorReturn("error").log()
                .block();

        assertThat(error).isEqualTo("error");
    }

    @Test
    void monoParallel() {
        Mono<String> anotherMono = Mono.just("Another data")
                .map(String::toUpperCase)
                .log()
                .subscribeOn(Schedulers.newParallel("Surepay"));
        Mono.just("data")
                .mergeWith(anotherMono)
                .log()
                .map(String::toUpperCase)
                .subscribe(System.out::println);
    }

    @Test
    void monoParallelSync() {
        Mono<String> anotherMono = Mono.just("Another data")
                .map(String::toUpperCase)
                .log()
                .subscribeOn(Schedulers.newParallel("Surepay"));
        Mono.just("data")
                .zipWith(anotherMono)
                .log()
                .map(tuple -> tuple.getT1().concat(tuple.getT2()))
                .subscribe(System.out::println);
    }

    @Test
    void monoZip() {
        Mono<String> firstMono = Mono.just("firstMono")
                .map(String::toUpperCase)
                .log()
                .subscribeOn(Schedulers.newParallel("Surepay"));
        Mono<String> secondMono = Mono.just("data")
                .log()
                .map(String::toUpperCase)
                .subscribeOn(Schedulers.newParallel("Surepay2"));

        String result = Mono.zip(firstMono, secondMono)
                .log()
                .map(Tuple2::toString)
                .block();

        assertThat(result).isEqualTo("[FIRSTMONO,DATA]");

    }

    @Test
    void fluxTest() {
        List<Integer> values = new LinkedList<>();
        Flux.just(1, 2, 3, 4, 5)
                .log()
                .map(value -> value * 2)
                .subscribe(values::add);

        assertThat(values).containsExactly(2, 4, 6, 8, 10);
    }

    @Test
    void fluxFilter() {
        Flux<String> source = Flux.just("John", "Monica", "Mark", "Cloe", "Frank", "Casper", "Olivia", "Emily", "Cate")
                .filter(name -> name.length() == 4)
                .map(String::toUpperCase).log();
        StepVerifier
                .create(source)
                .expectNext("JOHN")
                .expectNextMatches(name -> name.startsWith("MA"))
                .expectNext("CLOE", "CATE")
                .expectComplete()
                .verify();
    }

    @Test
    void fluxEmpty() {
        Flux<String> source = Flux.just("John", "Monica", "Mark", "Cloe", "Frank", "Casper", "Olivia", "Emily", "Cate")
                .filter(name -> name.length() == 99)
                .map(String::toUpperCase);
        StepVerifier
                .create(source)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void fluxSwitchEmpty() {
        Flux<String> source = Flux.just("John", "Monica", "Mark", "Cloe", "Frank", "Casper", "Olivia", "Emily", "Cate")
                .filter(name -> name.length() == 99)
                .switchIfEmpty(Mono.just("Surepay"))
                .map(String::toUpperCase);
        StepVerifier
                .create(source)
                .expectNext("SUREPAY")
                .verifyComplete();

    }

    @Test
    void fluxTake() {
        Flux<String> source = Flux.just("John", "Monica", "Mark", "Cloe", "Frank", "Casper", "Olivia", "Emily", "Cate")
                .take(2)
                .log();

        StepVerifier
                .create(source)
                .expectNext("John", "Monica")
                .expectComplete()
                .verify();
    }

}
