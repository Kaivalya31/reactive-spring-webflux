package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(controllers = FluxAndMonoController.class)//this will start the Spring Boot app.
//and make all the endpoints defined in the controller available.
@AutoConfigureWebTestClient//Ensures that the TestClient instance is automatically
    //injected into this class.
class FluxAndMonoControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void flux() {
        webTestClient
                .get()
                .uri("/flux")
                .exchange()//This will actually call the endpoint.
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Integer.class)
                .hasSize(3);
    }

    @Test
    void fluxApproach2() {
        Flux<Integer> output = webTestClient
                                        .get()
                                        .uri("/flux")
                                        .exchange()//This will actually call the endpoint.
                                        .expectStatus()
                                        .is2xxSuccessful()
                                        .returnResult(Integer.class)
                                        .getResponseBody();

        StepVerifier.create(output)
                .expectNext(15, 6, 24)
                .verifyComplete();
    }

    @Test
    void fluxApproach3() {
        webTestClient
                .get()
                .uri("/flux")
                .exchange()//This will actually call the endpoint.
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Integer.class)
                .consumeWith(listEntityExchangeResult -> {
                   var response = listEntityExchangeResult.getResponseBody();
                   assertTrue(Objects.requireNonNull(response).size() == 3);
                });
    }

    @Test
    void mono(){
        webTestClient
                .get()
                .uri("/mono")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Integer.class)
                .consumeWith(integerEntityExchangeResult -> {
                    var response = integerEntityExchangeResult.getResponseBody();
                    assertEquals(Objects.requireNonNull(response), 15);
                });
    }

    @Test
    void stream() {
        Flux<Long> output = webTestClient
                .get()
                .uri("/stream")
                .exchange()//This will actually call the endpoint.
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(Long.class)
                .getResponseBody();

        StepVerifier.create(output)
                .expectNext(0L, 1L, 2L, 3L)
                .thenCancel()//to finiish the test execution as the stream endpt. will
                //keep emitting values infinitely
                .verify();
    }
}