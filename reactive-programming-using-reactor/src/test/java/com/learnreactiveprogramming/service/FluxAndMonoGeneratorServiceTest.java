package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

class FluxAndMonoGeneratorServiceTest {

    private FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    void testNamesFlux(){
        Flux<String> names = fluxAndMonoGeneratorService.namesFlux();

        StepVerifier.create(names)
                .expectNext("Kaivalya", "Sourav", "Srivastava")
                .expectNextCount(0) //param is the count of remaining events
                .verifyComplete(); //verifies whether onComplete() is called or not
    }

    @Test
    void testMapTransform(){
        Flux<String> tansformedFlux = fluxAndMonoGeneratorService.fluxTransformUsingMap();

        StepVerifier.create(tansformedFlux)
                .expectNext("KAIVALYA", "SOURAV", "SRIVASTAVA")
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void testFluxImmutability(){
        Flux<String> names = fluxAndMonoGeneratorService.namesFluxImmutable();

        StepVerifier.create(names)
                .expectNext("KAIVALYA", "SOURAV", "SRIVASTAVA")
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void testFluxFilterOperator(){
        Flux<String> filteredFlux = fluxAndMonoGeneratorService.fluxFilterOperator(5);

        StepVerifier.create(filteredFlux)
                .expectNext("Kaivalya", "Sourav", "Srivastava")
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void testFluxFlatMapOperator(){
        Flux<String> flatMapFlux = fluxAndMonoGeneratorService.fluxFlatMapOperator();

        StepVerifier.create(flatMapFlux)
                //.expectNext("Sourav", "Kaivalya", "Srivastava") can't use as the order is uncertain
                .expectNextCount(24)
                .verifyComplete();
    }

    @Test
    void testMonoFlatMapOperator(){
        Mono<List<String>> flatMapMono = fluxAndMonoGeneratorService.monoFlatMapOperator(3);

        StepVerifier.create(flatMapMono)
                .expectNext(List.of("K", "A", "I", "V", "A", "L", "Y", "A"))
                .verifyComplete();
    }

    @Test
    void testFlatMapManyOperator(){
        Flux<String> flattenedFlux = fluxAndMonoGeneratorService.flatMapManyOperator(5);

        StepVerifier.create(flattenedFlux)
                .expectNext("K", "A", "I", "V", "A", "L", "Y", "A")
                .verifyComplete();
    }

    @Test
    void testFluxTransformOperator(){
        Flux<String> transformedFlux = fluxAndMonoGeneratorService.fluxTransformOperator(6);

        StepVerifier.create(transformedFlux)
                //.expectNext("K", "A", "I", "V", "A", "L", "Y", "A", "S", "R", "I", "V", "A", "S", "T", "A", "V", "A") Order is uncertain
                .expectNextCount(18)
                .verifyComplete();
    }

    @Test
    void testFluxTransformOperatorEmptyFlux(){
        Flux<String> transformedFlux = fluxAndMonoGeneratorService.fluxTransformOperator(15);//'15' will filter out all the
        //strings and an empty flux will be returned

        StepVerifier.create(transformedFlux)
                .expectNext("EMPTY!")
                .verifyComplete();
    }

    @Test
    void testFluxConcat(){
        Flux<String> concatenatedFlux = fluxAndMonoGeneratorService.concatOperation();

        StepVerifier.create(concatenatedFlux)
                .expectNext("KAIVALYA", "SOURAV", "SRIVASTAVA")
                .verifyComplete();
    }

    @Test
    void testConcatWithOperation() {
        Flux<String> concatenatedFlux = fluxAndMonoGeneratorService.concatWithOperation();

        StepVerifier.create(concatenatedFlux)
                .expectNext("KAIVALYA", "SOURAV", "SRIVASTAVA")
                .verifyComplete();
    }

    @Test
    void testMergeOperation() {
        Flux<String> mergedFlux = fluxAndMonoGeneratorService.mergeOperation();

        StepVerifier.create(mergedFlux)
                .expectNext("MR.", "SOURAV", "KAIVALYA", "SRIVASTAVA")
                .verifyComplete();
    }

    @Test
    void testMergeWithOperation() {
        Flux<String> mergedFlux = fluxAndMonoGeneratorService.mergeWithOperation();

        StepVerifier.create(mergedFlux)
                .expectNext("SRIVASTAVA", "KAIVALYA")
                .verifyComplete();
    }

    @Test
    void testMergeSequentialOperation(){
        Flux<String> mergedFlux = fluxAndMonoGeneratorService.mergeSequentialOperation();

        StepVerifier.create(mergedFlux)
                .expectNext("MR.", "KAIVALYA", "SOURAV", "SRIVASTAVA")
                .verifyComplete();
    }

    @Test
    void testZipOperation() {
        Flux<Integer> zippedFlux = fluxAndMonoGeneratorService.zipOperation();

        StepVerifier.create(zippedFlux)
                .expectNext(84, 57)
                .verifyComplete();
    }

    @Test
    void testzipOperationWithoutCombinatorMono() {
        Mono<Integer> zippedMono = fluxAndMonoGeneratorService.zipOperationWithoutCombinatorMono();

        StepVerifier.create(zippedMono)
                .expectNext(249)
                .verifyComplete();
    }

    @Test
    void testZipWithOperation() {
        Flux<Integer> zippedFlux = fluxAndMonoGeneratorService.zipWithOperation();

        StepVerifier.create(zippedFlux)
                .expectNext(36, 27)
                .verifyComplete();
    }

    @Test
    void testZipWithoutCombinatorOperation() {
        Flux<Integer> zippedFlux = fluxAndMonoGeneratorService.zipOperationWithoutCombinator();

        StepVerifier.create(zippedFlux)
                .expectNext(204, 240)
                .verifyComplete();
    }
}