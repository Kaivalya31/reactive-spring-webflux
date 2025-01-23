package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)//this will spin up the HTTP server for integ. tests to interact
@TestPropertySource(
        properties = {
                "restClient.moviesInfoURL=http://localhost:8084/v1",
                "restClient.reviewsURL=http://localhost:8084/v1/reviews"
        }
)
public class MoviesControllerIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void testRetrieveMovieById(){

        var movieId = "ABC";

        stubFor(WireMock.get(WireMock.urlEqualTo("/v1/findmovie" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert movie != null;
                    assert movie.getReviewList().size() == 2;
                    assert movie.getMovieInfo().getName().equals("Batman Begins");
                });
    }

    @Test
    void testRetrieveMovieById_404NotFound(){

        var movieId = "ABC";

        stubFor(WireMock.get(WireMock.urlEqualTo("/v1/findmovie" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("There is no movie info available for the ID: ABC");

        //verify that there were no retries
        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/findmovie" + "/" + movieId)));
    }

    @Test
    void testRetrieveMovieById_404NotFoundForReviewService(){

        var movieId = "ABC";

        stubFor(WireMock.get(WireMock.urlEqualTo("/v1/findmovie" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(404)));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert movie != null;
                    assert movie.getReviewList().size() == 0;//we return an empty Mono when there's no review found
                    assert movie.getMovieInfo().getName().equals("Batman Begins");
                });
    }

    @Test
    void testRetrieveMovieById_5XX(){

        var movieId = "ABC";

        stubFor(WireMock.get(WireMock.urlEqualTo("/v1/findmovie" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("MovieInfo service unavailable")));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server exception in MoviesInfoService: MovieInfo service unavailable");

        //verify if the retries happened 3 times which means the call was made 4 times in total
        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/findmovie" + "/" + movieId)));
    }

    @Test
    void testRetrieveMovieById_5XXForReviewService(){

        var movieId = "ABC";

        stubFor(WireMock.get(WireMock.urlEqualTo("/v1/findmovie" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Reviews service unavailable")));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server exception in MoviesInfoService: Reviews service unavailable");

        //verify if the retries happened 3 times which means the call was made 4 times in total
        WireMock.verify(4, getRequestedFor(urlPathMatching("/v1/reviews*")));
    }
}
