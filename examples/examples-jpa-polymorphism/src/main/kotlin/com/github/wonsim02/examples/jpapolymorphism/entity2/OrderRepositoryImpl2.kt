package com.github.wonsim02.examples.jpapolymorphism.entity2

import com.github.wonsim02.examples.jpapolymorphism.model.Order
import com.github.wonsim02.examples.jpapolymorphism.model.OrderRepository
import com.github.wonsim02.examples.jpapolymorphism.model.SinglePaymentOrder
import com.github.wonsim02.examples.jpapolymorphism.model.SubscriptionOrder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

/**
 * [Order] 및 [Order]의 서브클래스에 대한 JPA 엔티티가 [JPA Polymorphism](https://www.baeldung.com/hibernate-inheritance#joined-table)를
 * 이용하여 작성되었을 때의 [OrderRepository] 구현체.
 * [com.github.wonsim02.examples.jpapolymorphism.entity1.OrderRepositoryImpl1]의 구현에 비해 훨씬 간결하다.
 */
@Repository
class OrderRepositoryImpl2(
    private val jpaOrderRepo: JpaOrderEntityRepository,
) : OrderRepository {

    override fun findById(id: Long): Order? {
        return jpaOrderRepo
            .findByIdOrNull(id)
            ?.toOrder()
    }

    override fun save(order: Order): Order {
        val jpaOrderEntity = when (order) {
            is SinglePaymentOrder -> JpaSinglePaymentOrderEntity(order)
            is SubscriptionOrder -> JpaSubscriptionOrderEntity(order)
        }
        return jpaOrderRepo.save(jpaOrderEntity).toOrder()
    }
}
