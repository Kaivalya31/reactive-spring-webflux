package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
public class MoviesInfoController {

    //We can use @Autowired i.e. field injection
    private MoviesInfoService moviesInfoService;

    //Constructor injecting is preferred over @Autowired
    public MoviesInfoController(MoviesInfoService moviesInfoService){
        this.moviesInfoService = moviesInfoService;
    }

    @PostMapping("/addmovie")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo){//@Valid invokes bean validation
        return moviesInfoService.addMovieInfo(movieInfo);
    }

    @GetMapping("/listmovies")
    public Flux<MovieInfo> listMoviesInfo(@RequestParam(value = "year", required = false) Integer year
                                          , @RequestParam(value = "name", required = false) String name){
        if (year != null)
            return moviesInfoService.findMovieInfoByYear(year);

        return moviesInfoService.listMoviesInfo();
    }

    @GetMapping("/listmoviesbyname")
    public Mono<MovieInfo> listMoviesInfoByName(@RequestParam(value = "name", required = true) String name){
        return moviesInfoService.findMovieInfoByName(name);
    }

    @GetMapping("/findmovie/{id}")
    public Mono<ResponseEntity<MovieInfo>> findMovieInfoById(@PathVariable String id){
        return moviesInfoService.findMovieInfoById(id)
                .map(movieInfo -> {
                    return ResponseEntity.ok().body(movieInfo);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping("/updatemovieinfo/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody @Valid MovieInfo updatedMovieInfo, @PathVariable String id){
        return moviesInfoService.updateMovieInfo(updatedMovieInfo, id)
                .map(movieInfo -> {
                    return ResponseEntity.ok().body(movieInfo);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/deletemovieinfo/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfo(@PathVariable String id){
        return  moviesInfoService.deleteMovieInfo(id);
    }
}
