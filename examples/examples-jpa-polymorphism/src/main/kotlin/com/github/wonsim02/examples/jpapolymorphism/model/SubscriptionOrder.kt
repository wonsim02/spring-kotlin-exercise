package com.github.wonsim02.examples.jpapolymorphism.model

import java.time.Instant

/**
 * 구독 결제를 나타내는 [Order]의 구현체.
 * - [type]의 값은 [Order.Type.SUBSCRIPTION]으로 고정이다.
 * - [subscriptionProductId], [paymentAmountPerMonth] 및 [lastPaidAt]을 추가적인 속성으로 가진다.
 */
data class SubscriptionOrder(
    override val id: Long = 0L,
    override val userId: Long,
    val subscriptionProductId: Long,
    val paymentAmountPerMonth: Int,
    val lastPaidAt: Instant,
) : Order() {

    override val type = Type.SUBSCRIPTION
}
