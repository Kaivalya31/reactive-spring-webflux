package com.reactivespring.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class FluxAndMonoController {
    @GetMapping("/flux")
    public Flux<Integer> flux(){
        return Flux.just(15, 06, 24).log();
    }

    @GetMapping("/mono")
    public Mono<Integer> mono(){
        return Mono.just(15).log();
    }

    //Specifying the MediaType instructs the endpoint to produce a
    //stream of data to the client.
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> stream(){
        return Flux.interval(Duration.ofSeconds(2)).log();
    }
}
