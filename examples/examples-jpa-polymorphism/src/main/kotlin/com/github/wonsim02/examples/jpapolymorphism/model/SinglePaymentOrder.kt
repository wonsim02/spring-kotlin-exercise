package com.github.wonsim02.examples.jpapolymorphism.model

import java.time.Instant

/**
 * 단건 결제를 나타내는 [Order]의 구현체.
 * - [type]의 값은 [Order.Type.SINGLE_PAYMENT]으로 고정이다.
 * - [singlePaymentProductId], [paidAmount] 및 [paidAt]을 추가적인 속성으로 가진다.
 */
data class SinglePaymentOrder(
    override val id: Long = 0L,
    override val userId: Long,
    val singlePaymentProductId: Long,
    val paidAmount: Int,
    val paidAt: Instant,
) : Order() {

    override val type = Type.SINGLE_PAYMENT
}
