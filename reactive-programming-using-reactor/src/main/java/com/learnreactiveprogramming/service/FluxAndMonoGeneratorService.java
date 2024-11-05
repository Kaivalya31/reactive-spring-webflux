package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public Flux<String> namesFlux(){
        //A flux usually comes from a DB or as a result of a service call.
        return Flux.fromIterable(List.of("Kaivalya", "Sourav", "Srivastava")).log();
    }

    public Mono<String> nameMono(){
        return Mono.just("Kaivalya").log();
    }

    public Flux<String> namesFluxImmutable(){
        return Flux.fromIterable(List.of("Kaivalya", "Sourav", "Srivastava"))
                .map(String :: toUpperCase);
    }

    public Flux<String> fluxTransformUsingMap(){
        return Flux.fromIterable(List.of("Kaivalya", "Sourav", "Srivastava"))
                .map(String :: toUpperCase)
                .log();
    }

    public Flux<String> fluxFilterOperator(int minLength){
        return Flux.fromIterable(List.of("Mr.", "Kaivalya", "Sourav", "Srivastava"))
                .filter(s -> s.length() >= minLength);
    }

    private Flux<String> splitString(String s){
        String[] charArray = s.split("");
        int duration = new Random().nextInt(5);
        //delay might break the order of elements as the flatMap processes asynchronously and diff. elements will be
        // rcvd. at diff. intervals
        return Flux.fromArray(charArray).delayElements(Duration.ofSeconds(duration));
    }

    //There's another operator concatMap(), same as flatMap() with a diff. that it preserves the order the elements.
    //Although, the operations in concatMap() are async. but it waits for each element before returning so it takes more time.
    public  Flux<String> fluxFlatMapOperator(){
        return Flux.fromIterable(List.of("Kaivalya", "Sourav", "Srivastava"))
                .map(String :: toUpperCase)
                .flatMap(s -> splitString(s))
                .log();
    }

    public Mono<List<String>> monoFlatMapOperator(int minLength){
        return Mono.just("Kaivalya")
                .map(String::toUpperCase)
                .filter(s -> s.length() > minLength)
                .flatMap(s -> getCharList(s))
                .log();
    }

    public Flux<String> flatMapManyOperator(int minLength){
        return Mono.just("Kaivalya")
                .map(String::toUpperCase)
                .filter(s -> s.length() > minLength)
                .flatMapMany(this::splitString)
                .log();
    }

    public  Flux<String> fluxTransformOperator(int minLength){
        //Create a Function functional interface to assign the duty of mapping the string to upper case and filtering on
        //the basis of length. This is how 'Function' FI is useful as it lets us assign a particular redundant/common funtioanlity
        //to a variable and we can use the variable repeatedly in the tranform() to apply the assigned funtionality/ies.
        Function<Flux<String>, Flux<String>> mapAndFilter = name -> name.map(String :: toUpperCase)
                .filter(s -> s.length() > minLength);

        return Flux.fromIterable(List.of("Kaivalya", "Sourav", "Srivastava"))
                .transform(mapAndFilter)
                .flatMap(s -> splitString(s))
                //defaultIfEmpty() accepts an instance of the same type and returns that instance if the Flux is empty.
                //switchIfEmpty() is another option but it takes & returns a flux instead of an instance of same type.
                .defaultIfEmpty("EMPTY!")
                //.switchIfEmpty(Flux.just("EMPTY!"))
                .log();
    }

    //concat() is only applicable to Flux and subscribes to the FLuxes in sequence
    public Flux<String> concatOperation(){
        Flux<String> flux1 = Flux.just("KAIVALYA");
        Flux<String> flux2 = Flux.just("SOURAV", "SRIVASTAVA");

        return Flux.concat(flux1, flux2).log();
    }

    public Flux<String> concatWithOperation(){
        Flux<String> flux1 = Flux.just("KAIVALYA", "SOURAV");
        Flux<String> flux2 = Flux.just("SRIVASTAVA");

        return flux1.concatWith(flux2).log();
    }

    //merge() subscribes to all the Fluxes simultaneously
    public Flux<String> mergeOperation(){
        Flux<String> flux1 = Flux.just("MR.", "KAIVALYA").delayElements(Duration.ofMillis(100));
        Flux<String> flux2 = Flux.just("SOURAV", "SRIVASTAVA").delayElements(Duration.ofMillis(110));

        return Flux.merge(flux1, flux2).log();//MR., SOURAV, KAIVALYA, SRIVASTAVA
    }

    public Flux<String> mergeWithOperation(){
        Mono<String> mono1 = Mono.just("KAIVALYA");
        Mono<String> mono2 = Mono.just("SRIVASTAVA");

        return mono2.mergeWith(mono1).log();//SRIVASTAVA, KAIVALYA
    }

    //mergeSequential() subscibes all the Fluxes simultaneously but the merge happens sequentially
    public Flux<String> mergeSequentialOperation(){
        Flux<String> flux1 = Flux.just("MR.", "KAIVALYA");
        Flux<String> flux2 = Flux.just("SOURAV", "SRIVASTAVA");

        return Flux.mergeSequential(flux1, flux2).log();//MR., KAIVALYA, SOURAV, SRIVASTAVA
        //return Flux.mergeSequential(flux2, flux1).log();//SOURAV, SRIVASTAVA, MR., KAIVALYA
    }

    public Flux<Integer> zipOperation(){
        Flux<Integer> flux1 = Flux.just(24, 15);
        Flux<Integer> flux2 = Flux.just(60, 42);

        return Flux.zip(flux1, flux2, (first, second) -> first + second).log();//84, 67
    }

    public Flux<Integer> zipWithOperation(){
        Flux<Integer> flux1 = Flux.just(24, 15);
        Flux<Integer> flux2 = Flux.just(60, 42);

        return flux2.zipWith(flux1, (first, second) -> first - second).log();//36, 27
        //return flux1.zipWith(flux2, (first, second) -> first - second).log();//-36, -27
    }

    public Flux<Integer> zipOperationWithoutCombinator(){
        Flux<Integer> flux1 = Flux.just(24, 15);
        Flux<Integer> flux2 = Flux.just(60, 42);
        Flux<Integer> flux3 = Flux.just(6, 105);
        Flux<Integer> flux4 = Flux.just(114, 78);

        return Flux.zip(flux1, flux2, flux3, flux4)
                .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4()).log();//204, 240
    }

    //zip() or zipWith() returns a Mono when operating on Monos.
    public Mono<Integer> zipOperationWithoutCombinatorMono(){
        Mono<Integer> flux1 = Mono.just(24);
        Mono<Integer> flux2 = Mono.just(42);
        Mono<Integer> flux3 = Mono.just(105);
        Mono<Integer> flux4 = Mono.just(78);

        return Mono.zip(flux1, flux2, flux3, flux4)
                .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4()).log();//249
    }
    private Mono<List<String>> getCharList(String s){
        String[] charArray = s.split("");
        return Mono.just(List.of(charArray));
    }

    public static void main(String[] args) {
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();
        fluxAndMonoGeneratorService.namesFlux()
                .subscribe(name -> {           //subscribe() is for accessing the Flux elements which'll be returned 1-by-1.
                    System.out.println("Flux element: " + name);
                });
        fluxAndMonoGeneratorService.nameMono()
                .subscribe(name -> {
                    System.out.println("Mono element: " + name);
                });

        fluxAndMonoGeneratorService.fluxTransformUsingMap()
                .subscribe(name -> {
                    System.out.println("Transfomed Flux element: " + name);
                });
    }
}
