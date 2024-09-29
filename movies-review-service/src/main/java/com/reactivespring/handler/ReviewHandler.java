package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component//so that it gets scanned as a bean and automatically injected into the bean class
public class ReviewHandler {

    private ReviewReactiveRepository reviewReactiveRepository;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository){
        this.reviewReactiveRepository = reviewReactiveRepository;
    }
    public Mono<ServerResponse> addReview(ServerRequest request) {

        return request.bodyToMono(Review.class)
                .flatMap(reviewReactiveRepository::save)//Returns Mono<Review>
                .flatMap(savedReview -> {
                    return ServerResponse.status(HttpStatus.CREATED).bodyValue(savedReview);
                });//transform Mono<Review> to Mono<ServerResponse> and return
    }
}
