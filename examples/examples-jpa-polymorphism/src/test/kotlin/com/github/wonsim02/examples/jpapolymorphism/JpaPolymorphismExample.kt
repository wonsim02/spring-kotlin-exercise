package com.github.wonsim02.examples.jpapolymorphism

import com.github.wonsim02.examples.jpapolymorphism.entity1.ExamplesJpaPolymorphismEntity1Configuration
import com.github.wonsim02.examples.jpapolymorphism.entity2.ExamplesJpaPolymorphismEntity2Configuration
import com.github.wonsim02.examples.jpapolymorphism.model.OrderRepository
import com.github.wonsim02.examples.jpapolymorphism.model.SinglePaymentOrder
import com.github.wonsim02.examples.jpapolymorphism.model.SubscriptionOrder
import com.github.wonsim02.infra.jpa.CustomPostgresqlContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.boot.runApplication
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant

/**
 * [ExamplesJpaPolymorphismEntity1Configuration]의 엔티티 정의를 따라 DB에 저장된 [SinglePaymentOrder] 및 [SubscriptionOrder]가
 * [ExamplesJpaPolymorphismEntity2Configuration]의 엔티티 정의를 따랐을 때 DB로부터 잘 조회되는지 검사한다.
 */
@Testcontainers
class JpaPolymorphismExample {

    @Test
    fun `JPA polymorphism usage for simplification of OrderRepository implementation`() {
        val app1 = runApplication<App1>(*testContainer.provideTestContainerProperties())
        val orderRepo1 = app1.getBean(OrderRepository::class.java)
        val order1 = orderRepo1.save(
            SinglePaymentOrder(
                id = 0L,
                userId = 1L,
                singlePaymentProductId = 1L,
                paidAmount = 10000,
                paidAt = Instant.now(),
            )
        )
        val order2 = orderRepo1.save(
            SubscriptionOrder(
                id = 0L,
                userId = 2L,
                subscriptionProductId = 2L,
                paymentAmountPerMonth = 9990,
                lastPaidAt = Instant.now(),
            )
        )
        SpringApplication.exit(app1, ExitCodeGenerator { 0 })

        val app2 = runApplication<App2>(*testContainer.provideTestContainerProperties())
        val orderRepo2 = app2.getBean(OrderRepository::class.java)
        val foundOrder1 = assertDoesNotThrow { orderRepo2.findById(order1.id)!! }
        val foundOrder2 = assertDoesNotThrow { orderRepo2.findById(order2.id)!! }
        assertEquals(order1, foundOrder1)
        assertEquals(order2, foundOrder2)
        SpringApplication.exit(app2, ExitCodeGenerator { 0 })
    }

    companion object {

        @Container
        val testContainer = CustomPostgresqlContainer(
            database = "database",
            username = "username",
            password = "password",
        )
    }
}
