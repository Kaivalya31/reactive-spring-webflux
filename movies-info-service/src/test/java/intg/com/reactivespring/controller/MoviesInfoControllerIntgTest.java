package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)//spins up the spring context,
// random port so that 8080 or actual port of the server isn't used
@ActiveProfiles("test")
@AutoConfigureWebTestClient//reqd. to interact with the endpoint defined in the controller
class MoviesInfoControllerIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;//used to interact with the endpoint

    private static String MOVIES_INFO_SERVICE_URL = "/v1";

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
    void testAddMovieInfo() {

        var movieInfo = new MovieInfo(null, "The Godfather", List.of("Al Pacino", "Robert De Niro"), LocalDate.parse("1972-03-24"), 1972, 9.2);

        webTestClient
                .post()
                .uri(MOVIES_INFO_SERVICE_URL + "/addmovie")
                .bodyValue(movieInfo)
                .exchange()//calling the endpoint spun up by SpringBootTest annotation
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assert savedMovieInfo.getMovieInfoId() != null;
                });
    }

    @Test
    void testListMoviesInfo() {

        webTestClient
                .get()
                .uri(MOVIES_INFO_SERVICE_URL + "/listmovies")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void testListMoviesInfoByYear() {

        var uri = UriComponentsBuilder.fromUriString(MOVIES_INFO_SERVICE_URL + "/listmovies")
                        .queryParam("year", 2000)
                                .buildAndExpand().toUri();

        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void testListMoviesInfoByName() {

        var uri = UriComponentsBuilder.fromUriString(MOVIES_INFO_SERVICE_URL + "/listmoviesbyname")
                .queryParam("name", "Forrest Gump")
                .buildAndExpand().toUri();

        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void testFindMovieInfoById() {

        var movieId = "MIS001";

        webTestClient
                .get()
                .uri(MOVIES_INFO_SERVICE_URL + "/findmovie/{id}", movieId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var foundMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert foundMovieInfo != null;
                    assert foundMovieInfo.getMovieInfoId().equals("MIS001");
                });

        //Another way of testing
        webTestClient
                .get()
                .uri(MOVIES_INFO_SERVICE_URL + "/findmovie/{id}", movieId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("The Conjuring");
    }

    @Test
    void testFindMovieInfoByIdNotFound() {

        var movieId = "MIS002";

        webTestClient
                .get()
                .uri(MOVIES_INFO_SERVICE_URL + "/findmovie/{id}", movieId)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void testUpdateMovieInfo() {
        var movieInfo = new MovieInfo(null, "The Conjuring", List.of("Patrick Wilson", "Vera Farmiga", "Ron Livingston"),
                LocalDate.parse("2013-08-02"), 2013, 7.5);
        var movieId = "MIS001";

        webTestClient
                .put()
                .uri(MOVIES_INFO_SERVICE_URL + "/updatemovieinfo/{id}", movieId)
                .bodyValue(movieInfo)
                .exchange()//calling the endpoint spun up by SpringBootTest annotation
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(updatedMovieInfo);
                    assertNotNull(updatedMovieInfo.getMovieInfoId());
                    assertEquals(updatedMovieInfo.getCast(), List.of("Patrick Wilson", "Vera Farmiga", "Ron Livingston"));
                });
    }

    @Test
    void testUpdateMovieInfoNotFound() {
        var movieInfo = new MovieInfo(null, "The Conjuring", List.of("Patrick Wilson", "Vera Farmiga", "Ron Livingston"),
                LocalDate.parse("2013-08-02"), 2013, 7.5);
        var movieId = "MIS002";

        webTestClient
                .put()
                .uri(MOVIES_INFO_SERVICE_URL + "/updatemovieinfo/{id}", movieId)
                .bodyValue(movieInfo)
                .exchange()//calling the endpoint spun up by SpringBootTest annotation
                .expectStatus()
                .isNotFound();
    }

    @Test
    void testDeleteMovieInfo() {
        var deletedMovieId = "MIS001";

        webTestClient
                .delete()
                .uri(MOVIES_INFO_SERVICE_URL + "/deletemovieinfo/{id}", deletedMovieId)
                .exchange()
                .expectStatus()
                .isNoContent();

        //testing whether the movie has been removed
        webTestClient
                .get()
                .uri(MOVIES_INFO_SERVICE_URL + "/findmovie/{id}", deletedMovieId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var foundMovie = movieInfoEntityExchangeResult.getResponseBody();
                    assertNull(foundMovie);
                });
    }
}