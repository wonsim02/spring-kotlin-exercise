package com.github.wonsim02.examples.jpapolymorphism.model

/**
 * [Order]에 대한 리포지토리.
 * [Order]에 대응되는 JPA 엔티티 정의에 따라 구현 방식이 달라진다.
 */
interface OrderRepository {

    fun findById(id: Long): Order?

    fun save(order: Order): Order
}
