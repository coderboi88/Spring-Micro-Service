package com.aditya.orderservice.api;

import com.aditya.orderservice.dto.OrderRequest;
import com.aditya.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderApi {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
//    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")  //FaultTolerence
//    @TimeLimiter(name = "inventory")   //How much time a API should wait before sending timeout exception
//    @Retry(name = "inventory")  //Incase if the api fails , we will retry it
    public CompletableFuture<String> createOrder(@RequestBody OrderRequest orderRequest) {
        return CompletableFuture.supplyAsync(()-> orderService.createOrder(orderRequest));
    }

    public CompletableFuture<String> fallbackMethod(OrderRequest orderRequest, RuntimeException e) {
        return CompletableFuture.supplyAsync(() -> "Oops! Some error occurred");
    }
}
