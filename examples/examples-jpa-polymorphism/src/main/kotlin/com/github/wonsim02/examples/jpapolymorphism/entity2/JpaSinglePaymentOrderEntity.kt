package com.github.wonsim02.examples.jpapolymorphism.entity2

import com.github.wonsim02.examples.jpapolymorphism.Constants
import com.github.wonsim02.examples.jpapolymorphism.model.Order
import com.github.wonsim02.examples.jpapolymorphism.model.SinglePaymentOrder
import java.time.Instant
import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.Table

/**
 * [SinglePaymentOrder]에 대한 JPA 엔티티.
 * - [JpaOrderEntity]의 `type` 열의 값은 [Order.Type.SINGLE_PAYMENT]와 일치한다.
 * - [SinglePaymentOrder.id]에 대응되는 [id] 열은 [JpaOrderEntity]로부터 상속받는다.
 */
@Entity(name = JpaSinglePaymentOrderEntity.ENTITY_NAME)
@Table(
    schema = Constants.SCHEMA_NAME,
    name = JpaSinglePaymentOrderEntity.TABLE_NAME,
)
@DiscriminatorValue(value = JpaSinglePaymentOrderEntity.DISCRIMINATOR_VALUE)
open class JpaSinglePaymentOrderEntity private constructor(
    order: Order,

    @Column(name = "single_payment_product_id")
    val singlePaymentProductId: Long,

    @Column(name = "paid_amount")
    val paidAmount: Int,

    @Column(name = "paid_at")
    val paidAt: Instant,
) : JpaOrderEntity<SinglePaymentOrder>(order) {

    constructor(singlePaymentOrder: SinglePaymentOrder) : this(
        order = singlePaymentOrder,
        singlePaymentProductId = singlePaymentOrder.singlePaymentProductId,
        paidAmount = singlePaymentOrder.paidAmount,
        paidAt = singlePaymentOrder.paidAt,
    )

    override fun toOrder(): SinglePaymentOrder {
        return SinglePaymentOrder(
            id = id,
            userId = userId,
            singlePaymentProductId = singlePaymentProductId,
            paidAmount = paidAmount,
            paidAt = paidAt,
        )
    }

    companion object {

        const val ENTITY_NAME = "JpaSinglePaymentOrderEntity"
        const val TABLE_NAME = "order_single_payment"
        const val DISCRIMINATOR_VALUE = "SINGLE_PAYMENT"

        init {
            assert(Order.Type.valueOf(DISCRIMINATOR_VALUE) == Order.Type.SINGLE_PAYMENT)
        }
    }
}
