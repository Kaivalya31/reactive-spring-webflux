package com.reactivespring.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.stream.Collectors;

@ControllerAdvice//first place where the exceptions thrown by the controller
//will be caught and using this we can customize our responses for the clients
@Slf4j
public class GlobalExceptionHandler {

    //WebExchangeBindException is thrown by bean validation
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handleRequestBodyException(WebExchangeBindException exception){
        log.error("Exception caught in handleRequestBodyException: {}", exception.getMessage(), exception);

        var error = exception.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)//this prints the message developer-defined in bean validation annnotation
                .sorted().collect(Collectors.joining(","));
        log.error("Server error: {}", error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
