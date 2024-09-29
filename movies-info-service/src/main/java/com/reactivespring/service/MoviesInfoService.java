package com.reactivespring.service;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MoviesInfoService {

    private MovieInfoRepository movieInfoRepository;

    public MoviesInfoService(MovieInfoRepository movieInfoRepository){
        this.movieInfoRepository = movieInfoRepository;
    }

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
        return movieInfoRepository.save(movieInfo);
    }

    public Flux<MovieInfo> listMoviesInfo() {
        return movieInfoRepository.findAll();
    }

    public Mono<MovieInfo> findMovieInfoById(String id) {
        return movieInfoRepository.findById(id);
    }

    public Mono<MovieInfo> updateMovieInfo(MovieInfo updatedMovieInfo, String id) {
        return movieInfoRepository.findById(id)
                .flatMap(movieInfo -> {
                    movieInfo.setName(updatedMovieInfo.getName());
                    movieInfo.setCast(updatedMovieInfo.getCast());
                    movieInfo.setReleaseDate(updatedMovieInfo.getReleaseDate());
                    movieInfo.setIMDbRating(updatedMovieInfo.getIMDbRating());

                    return movieInfoRepository.save(movieInfo);
                });//flatMap is used as the save() will return a reactive type
    }

    public Mono<Void> deleteMovieInfo(String id) {
        return movieInfoRepository.deleteById(id);
    }

    public Flux<MovieInfo> findMovieInfoByYear(Integer year){
        return movieInfoRepository.findByYear(year);
    }

    public Mono<MovieInfo> findMovieInfoByName(String name){
        return movieInfoRepository.findByName(name);
    }
}
