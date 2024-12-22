package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient//reqd. to interact with the endpoint defined in the controller
public class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean//this is used as we still need to spin the Spring context
    private MoviesInfoService moviesInfoServiceMock;

    private static String MOVIES_INFO_SERVICE_URL = "/v1";

    @Test
    void testListMoviesInfo() {
        var movieInfos = List.of(new MovieInfo(null, "Forrest Gump", List.of("Tom Hanks", "Robin Wright"),
                        LocalDate.parse("1994-07-06"), 1994, 8.8),
                new MovieInfo(null, "Hera Pheri", List.of("Paresh Rawal", "Akshay Kumar", "Suneil Shetty"),
                        LocalDate.parse("2000-03-31"), 2000, 8.2),
                new MovieInfo("MIS001", "The Conjuring", List.of("Patrick Wilson", "Vera Farmiga"),
                        LocalDate.parse("2013-08-02"), 2013, 7.5));

        //using mockito to perform a dummy and not actual operation of fetching movies from the DB
        when(moviesInfoServiceMock.listMoviesInfo()).thenReturn(Flux.fromIterable(movieInfos));

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
    void testFindMovieInfoById() {
        var movieId = "MIS001";

        var movie = new MovieInfo(movieId, "The Conjuring", List.of("Patrick Wilson", "Vera Farmiga"),
                LocalDate.parse("2013-08-02"), 2013, 7.5);

        when(moviesInfoServiceMock.findMovieInfoById(movieId)).thenReturn(Mono.just(movie));

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
    void testAddMovieInfo() {
        var movieInfo = new MovieInfo("mockId", "The Godfather", List.of("Al Pacino", "Robert De Niro"), LocalDate.parse("1972-03-24"), 1972,9.2);

        when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(movieInfo));

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
                    assertEquals("mockId", savedMovieInfo.getMovieInfoId());
                });
    }

    @Test
    void testUpdateMovieInfo() {
        var origMovieInfo = new MovieInfo(null, "The Conjuring", List.of("Patrick Wilson", "Vera Farmiga", "Ron Livingston"),
                LocalDate.parse("2013-08-02"), 2013, 7.5);
        var newMovieId = "MIS001";

        when(moviesInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(Mono.just(
                new MovieInfo(newMovieId, "The Conjuring", List.of("Patrick Wilson", "Vera Farmiga", "Ron Livingston"),
                        LocalDate.parse("2013-08-02"), 2013, 7.5)
        ));

        webTestClient
                .put()
                .uri(MOVIES_INFO_SERVICE_URL + "/updatemovieinfo/{id}", newMovieId)
                .bodyValue(origMovieInfo)
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
    void testDeleteMovieInfo() {
        var deletedMovieId = "MIS001";

        when(moviesInfoServiceMock.deleteMovieInfo(isA(String.class))).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri(MOVIES_INFO_SERVICE_URL + "/deletemovieinfo/{id}", deletedMovieId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void testAddMovieInfoValidation() {
        var movieInfo = new MovieInfo("mockId", "", List.of(""), LocalDate.parse("1972-03-24"), 1972, -1.2);

        //We don't need mocking once we have an exception handler
        //when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(movieInfo));

        webTestClient
                .post()
                .uri(MOVIES_INFO_SERVICE_URL + "/addmovie")
                .bodyValue(movieInfo)
                .exchange()//calling the endpoint spun up by SpringBootTest annotation
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var response = stringEntityExchangeResult.getResponseBody();
                    assertNotNull(response);
                    var expectedResponseMessage = "IMDb rating can't be less than 0,Movie name can't be left empty,You must mention atleast a single cast";
                    System.out.println(expectedResponseMessage);
                    assertEquals(expectedResponseMessage, response);
                });
    }
}
