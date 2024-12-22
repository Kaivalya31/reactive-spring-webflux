package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest //scans the application for the repo. class and use it in the TCs.
@ActiveProfiles("test") //to specify the profile to be used by the embedded MongoDB instance
    //started by @DataMongoTest for the below tests.
class MovieInfoRepositoryIntegTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp() {
        var movieInfos = List.of(new MovieInfo(null, "Forrest Gump", List.of("Tom Hanks", "Robin Wright"),
                LocalDate.parse("1994-07-06"), 1994, 8.8),
                new MovieInfo(null, "Hera Pheri", List.of("Paresh Rawal", "Akshay Kumar", "Suneil Shetty"),
                        LocalDate.parse("2000-03-31"), 2000, 8.2),
                new MovieInfo("MIS001", "The Conjuring", List.of("Patrick Wilson", "Vera Farmiga"),
                        LocalDate.parse("2013-08-02"), 2013, 7.5));

        movieInfoRepository.saveAll(movieInfos).blockLast();//bloackLast() ensures that the
        //saveAll() method gets completed before we execute a method on the repo. object inside
        //any TC. This is bcoz method calls via the repo. object are async.
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void testFindAll(){
        var movieInfo = movieInfoRepository.findAll();

        StepVerifier.create(movieInfo)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void testFindById(){
        var movieInfo = movieInfoRepository.findById("MIS001");

        StepVerifier.create(movieInfo)
                //.expectNextCount(1)
                .assertNext(movieInfo1 -> {
                    assertEquals(movieInfo1.getName(), "The Conjuring");
                })
                .verifyComplete();
    }

    @Test
    void testFindByYear(){
        var movieInfoFlux = movieInfoRepository.findByYear(2013);

        StepVerifier.create(movieInfoFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testSaveMovieInfo(){
        var newMovie = new MovieInfo(null, "12th Fail", List.of("Vikrant Massey", "Medha Shankr", "Anant Joshi"),
                LocalDate.parse("2023-10-23"), 2023, 9.1);
        var newMovieAdded = movieInfoRepository.save(newMovie).log();

        StepVerifier.create(newMovieAdded)
                //.expectNextCount(1)
                .assertNext(movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieInfoId());
                    assertEquals(movieInfo1.getReleaseDate(), LocalDate.parse("2023-10-23"));
                })
                .verifyComplete();
    }

    @Test
    void testUpdateMovieInfo(){
        var movieInfo = movieInfoRepository.findById("MIS001").block();//block() gives access to the actual type instead of a Mono
        movieInfo.setMovieInfoId("MIS0001");

        var newMovieAdded = movieInfoRepository.save(movieInfo).log();

        StepVerifier.create(newMovieAdded)
                //.expectNextCount(1)
                .assertNext(movieInfo1 -> {
                    assertEquals(movieInfo1.getMovieInfoId(), "MIS0001");
                })
                .verifyComplete();
    }

    @Test
    void testDeleteMovieInfo(){
        movieInfoRepository.deleteById("MIS001").block();//block() ensures that delete
        //is completed before other operations are performed
        var movieInfos = movieInfoRepository.findAll().log();

        StepVerifier.create(movieInfos)
                .expectNextCount(2)
                .verifyComplete();
    }
}