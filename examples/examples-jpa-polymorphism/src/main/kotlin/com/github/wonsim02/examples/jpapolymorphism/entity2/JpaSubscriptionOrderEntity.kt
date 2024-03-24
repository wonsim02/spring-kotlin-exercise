package com.github.wonsim02.examples.jpapolymorphism.entity2

import com.github.wonsim02.examples.jpapolymorphism.Constants
import com.github.wonsim02.examples.jpapolymorphism.model.Order
import com.github.wonsim02.examples.jpapolymorphism.model.SubscriptionOrder
import java.time.Instant
import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.Table

/**
 * [SubscriptionOrder]에 대한 JPA 엔티티.
 * - [JpaOrderEntity]의 `type` 열의 값은 [Order.Type.SUBSCRIPTION]와 일치한다.
 * - [SubscriptionOrder.id]에 대응되는 [id] 열은 [JpaOrderEntity]로부터 상속받는다.
 */
@Entity(name = JpaSubscriptionOrderEntity.ENTITY_NAME)
@Table(
    schema = Constants.SCHEMA_NAME,
    name = JpaSubscriptionOrderEntity.TABLE_NAME,
)
@DiscriminatorValue(value = JpaSubscriptionOrderEntity.DISCRIMINATOR_VALUE)
open class JpaSubscriptionOrderEntity private constructor(
    order: Order,

    @Column(name = "subscription_product_id")
    val subscriptionProductId: Long,

    @Column(name = "payment_amount_per_month")
    val paymentAmountPerMonth: Int,

    @Column(name = "last_paid_at")
    val lastPaidAt: Instant,
) : JpaOrderEntity<SubscriptionOrder>(order) {

    constructor(subscriptionOrder: SubscriptionOrder) : this(
        order = subscriptionOrder,
        subscriptionProductId = subscriptionOrder.subscriptionProductId,
        paymentAmountPerMonth = subscriptionOrder.paymentAmountPerMonth,
        lastPaidAt = subscriptionOrder.lastPaidAt,
    )

    override fun toOrder(): SubscriptionOrder {
        return SubscriptionOrder(
            id = id,
            userId = userId,
            subscriptionProductId = subscriptionProductId,
            paymentAmountPerMonth = paymentAmountPerMonth,
            lastPaidAt = lastPaidAt,
        )
    }

    companion object {

        const val ENTITY_NAME = "JpaSubscriptionOrderEntity"
        const val TABLE_NAME = "order_subscription"
        const val DISCRIMINATOR_VALUE = "SUBSCRIPTION"

        init {
            assert(Order.Type.valueOf(DISCRIMINATOR_VALUE) == Order.Type.SUBSCRIPTION)
        }
    }
}
