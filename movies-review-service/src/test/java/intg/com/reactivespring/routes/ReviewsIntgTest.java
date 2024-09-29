package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    private static final String REVIEWS_URL = "/v1";
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReactiveMongoRepository reactiveMongoRepository;

    @BeforeEach
    void setUp() {

        var reviewList = List.of(new Review(null, 1L, "Awesome", 9.0),
                new Review(null, 2L, "Very Good", 8.5),
                new Review(null, 3L, "Good", 8.0));

        reactiveMongoRepository.saveAll(reviewList).blockLast();
    }

    @AfterEach
    void tearDown() {
        reactiveMongoRepository.deleteAll().block();
    }

    @Test
    void testAddReview(){

        var movieReview = new Review(null, 2L, "Very Good", 8.5);
        
        webTestClient
                .post()
                .uri(REVIEWS_URL + "/addReview")
                .bodyValue(movieReview)
                .exchange()//calling the endpoint spun up by SpringBootTest annotation
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var savedMovieReview = reviewEntityExchangeResult.getResponseBody();
                    assertNotNull(savedMovieReview);
                    assertNotNull(savedMovieReview.getReviewId());
                });
    }
}
