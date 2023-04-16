package com.aditya.orderservice.service;

import com.aditya.orderservice.dto.InventoryResponse;
import com.aditya.orderservice.dto.OrderLineItemDto;
import com.aditya.orderservice.dto.OrderRequest;
import com.aditya.orderservice.event.OrderUpdateEvent;
import com.aditya.orderservice.model.Order;
import com.aditya.orderservice.model.OrderLineItem;
import com.aditya.orderservice.repository.OrderRepository;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    private final KafkaTemplate<String, OrderUpdateEvent> kafkaTemplate;

    public String createOrder(OrderRequest orderRequest) {
        log.info("Request Body : {}", orderRequest);
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItem> orderLineItems = orderRequest
                .getOrderLineItems()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        order.setOrderLineItems(orderLineItems);

        List<String> skuCodes = orderLineItems.stream()
                .map(OrderLineItem::getSkuCode)
                .toList();

        //Adding SpanId
        Span span = tracer.nextSpan().name("InventoryServiceLookup");
        try (Tracer.SpanInScope spanInScope = tracer.withSpan(span.start())) {
            // Method to call the external api from the service by registering the service in eureka
            // and it will help in discovering the port for the external service
            InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCodes", skuCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            // Method to call the external api from the service for a particular port
//        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
//                .uri("http://localhost:8217/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCodes", skuCodes).build())
//                .retrieve()
//                .bodyToMono(InventoryResponse[].class)
//                .block();

            boolean allInStock = Arrays.stream(inventoryResponses).allMatch(InventoryResponse::isInStock);

            if (allInStock) {
                orderRepository.save(order);
                kafkaTemplate.send("notificationTopic", new OrderUpdateEvent(order.getOrderNumber()));
                return "Order Placed Successfully";
            } else {
                throw new IllegalArgumentException("Currently not in stock , Please try again later");
            }
        } finally {
            span.end();
        }


    }

    private OrderLineItem mapToDto(OrderLineItemDto orderLineItemDto) {
        OrderLineItem orderLineItem = new OrderLineItem();
        orderLineItem.setSkuCode(orderLineItemDto.getSkuCode());
        orderLineItem.setPrice(orderLineItemDto.getPrice());
        orderLineItem.setQuantity(orderLineItemDto.getQuantity());

        return orderLineItem;
    }
}
