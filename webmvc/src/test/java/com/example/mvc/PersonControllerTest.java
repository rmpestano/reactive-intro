package com.example.mvc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = PersonsController.class)
@AutoConfigureWebTestClient(timeout = "20000")//20 seconds
class PersonControllerTest {

    @Autowired
    WebTestClient webClient;

    @MockBean
    PersonRepository personRepository;

    @Test
    void shouldFindPersonById() {
        given(personRepository.findById(anyString()))
                .willReturn(Optional.of(new Person("id", "test")));
        var exchange = webClient.get()
                .uri("/api/persons/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange();

        exchange.expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("test");
    }

}
