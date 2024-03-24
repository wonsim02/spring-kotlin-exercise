package com.github.wonsim02.examples.jpapolymorphism.entity1

import com.github.wonsim02.examples.jpapolymorphism.Constants
import com.github.wonsim02.examples.jpapolymorphism.model.SubscriptionOrder
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * [SubscriptionOrder]의 추가적인 속성에 대한 JPA 엔티티.
 * [JpaOrderEntity.id]와 값을 공유하는 [id] 및 [SubscriptionOrder]의 추가적인 속성에 대한 열(Column)만을 가지고 있다.
 */
@Entity(name = JpaSubscriptionOrderEntity.ENTITY_NAME)
@Table(
    schema = Constants.SCHEMA_NAME,
    name = JpaSubscriptionOrderEntity.TABLE_NAME,
)
open class JpaSubscriptionOrderEntity(
    @Id
    val id: Long,

    @Column(name = "subscription_product_id")
    val subscriptionProductId: Long,

    @Column(name = "payment_amount_per_month")
    val paymentAmountPerMonth: Int,

    @Column(name = "last_paid_at")
    val lastPaidAt: Instant,
) {

    constructor(
        subscriptionOrder: SubscriptionOrder,
        newId: Long? = null,
    ) : this(
        id = newId ?: subscriptionOrder.id,
        subscriptionProductId = subscriptionOrder.subscriptionProductId,
        paymentAmountPerMonth = subscriptionOrder.paymentAmountPerMonth,
        lastPaidAt = subscriptionOrder.lastPaidAt,
    )

    companion object {

        const val ENTITY_NAME = "JpaSubscriptionOrderEntity"
        const val TABLE_NAME = "order_subscription"
    }
}
