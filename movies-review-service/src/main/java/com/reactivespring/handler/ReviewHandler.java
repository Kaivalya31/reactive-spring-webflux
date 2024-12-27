package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.validation.Validator;
import javax.validation.ConstraintViolation;
import java.util.stream.Collectors;

@Component//so that it gets scanned as a bean and automatically injected into the bean class
@Slf4j
public class ReviewHandler {

    @Autowired
    private Validator validator;

    private ReviewReactiveRepository reviewReactiveRepository;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository){
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    private void validate(Review review){
        var constraintViolations = validator.validate(review);
        log.info("constraintsViolations: {}", constraintViolations);

        if (constraintViolations.size() > 0) {
            var errorMessage = constraintViolations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(", "));
            log.info("errorMessage : {} ", errorMessage);
            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {

        return request.bodyToMono(Review.class)    //extract the request to a Mono
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)//Returns Mono<Review>
                .flatMap(savedReview -> {
                    return ServerResponse.status(HttpStatus.CREATED).bodyValue(savedReview);
                });//transform Mono<Review> to Mono<ServerResponse> and return
    }

    public Mono<ServerResponse> getReviews(ServerRequest request){
        var movieInfoId = request.queryParam("movieInfoId");

        if(movieInfoId.isPresent()){
            var reviewsFlux = reviewReactiveRepository.findReviewByMovieInfoId(Long.valueOf(movieInfoId.get()));
            return ServerResponse.ok().body(reviewsFlux, Review.class);
        }else{
            var reviewsFlux = reviewReactiveRepository.findAll();
            return ServerResponse.ok().body(reviewsFlux, Review.class);
        }
    }

    public Mono<ServerResponse> updateReview(ServerRequest request){
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("No review found for Review ID " + reviewId)));
        //switchIfEmpty() handles the scenario where findById() doesn't return any data

        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                .map(reqReview -> {
                    review.setReviewId(reqReview.getReviewId());
                    review.setMovieInfoId(reqReview.getMovieInfoId());
                    review.setComment(reqReview.getComment());
                    review.setRating(reqReview.getRating());

                    return review;
                })
                .flatMap(reviewReactiveRepository::save)
                .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
        );

        /*Alternative approach to handle the scenario where findById() doesn't return any data.
        We use the switchIfEmpty() while building the response instead of using it after findById().
        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(reqReview -> {
                            review.setReviewId(reqReview.getReviewId());
                            review.setMovieInfoId(reqReview.getMovieInfoId());
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());

                            return review;
                        })
                        .flatMap(reviewReactiveRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                )
                .switchIfEmpty(ServerResponse.notFound().build());  */

    }

    public Mono<ServerResponse> deleteReview(ServerRequest request){
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId);

        return existingReview
                .flatMap(review -> reviewReactiveRepository.deleteById(reviewId)
                        .then(ServerResponse.noContent().build()));
    }
}
