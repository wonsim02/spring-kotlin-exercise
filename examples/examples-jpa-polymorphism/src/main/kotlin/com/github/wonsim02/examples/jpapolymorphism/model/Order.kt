package com.github.wonsim02.examples.jpapolymorphism.model

/**
 * 상품 결제를 나타내는 모델.
 * 공통된 속성으로 [id], [userId] 및 [type]을 가진다.
 */
sealed class Order {

    abstract val id: Long
    abstract val userId: Long
    abstract val type: Type

    enum class Type {

        SINGLE_PAYMENT,
        SUBSCRIPTION,
    }
}
