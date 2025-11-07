package com.quind.ecommerce.order.infrastructure.persistence.postgres;

import com.quind.ecommerce.order.domain.model.Order;
import com.quind.ecommerce.order.domain.model.OrderStatus;
import com.quind.ecommerce.order.domain.ports.OrderRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
class OrderRepositoryAdapterIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("orders_db")
            .withUsername("orders_user")
            .withPassword("orders_pass");

    @DynamicPropertySource
    static void r2dbcProperties(DynamicPropertyRegistry registry) {
        String r2dbcUrl = String.format("r2dbc:postgresql://%s:%d/%s",
                postgres.getHost(), postgres.getMappedPort(5432), postgres.getDatabaseName());

        registry.add("spring.r2dbc.url", () -> r2dbcUrl);
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
    }

    @Autowired
    private OrderRepositoryPort orderRepository;

    @Test
    void shouldSaveAndFindOrder() {
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .customerId("99")
                .totalAmount(new BigDecimal("12345"))
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Mono<Order> test = orderRepository.save(order)
                .then(orderRepository.findById(order.getId()));

        StepVerifier.create(test)
                .assertNext(o -> {
                    assertEquals(order.getId(), o.getId());
                    assertEquals("99", o.getCustomerId());
                    assertEquals(new BigDecimal("12345"), o.getTotalAmount());
                    assertEquals(OrderStatus.PENDING, o.getStatus());
                })
                .verifyComplete();
    }
}
