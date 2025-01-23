package com.reactivespring.router;

import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration//to cr8 a bean to be scanned by the SB appln. to get all the routes.
public class ReviewRouter {

    @Bean//reqd. so that this route get configd. in the SB appln.
    public RouterFunction<ServerResponse> reviewsRoute(ReviewHandler reviewHandler){

        return route()
                .GET("/v1/helloworld", (request -> ServerResponse.ok().bodyValue("HELLO WORLD")))
                .POST("/v1/reviews", request -> reviewHandler.addReview(request))
                .GET("/v1/reviews", request -> reviewHandler.getReviews(request))
                .PUT("/v1/reviews/{id}", request -> reviewHandler.updateReview(request))
                .DELETE("/v1/reviews/{id}", request -> reviewHandler.deleteReview(request))
                .build();
    }
}
