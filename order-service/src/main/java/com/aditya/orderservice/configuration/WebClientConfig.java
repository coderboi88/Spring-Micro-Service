package com.aditya.orderservice.configuration;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    //Creating a bean for calling the External API. @LoadBalanced will be used in case of multiple instances of the service been registered in eureka
    //and it needs to identified by the caller.
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}

