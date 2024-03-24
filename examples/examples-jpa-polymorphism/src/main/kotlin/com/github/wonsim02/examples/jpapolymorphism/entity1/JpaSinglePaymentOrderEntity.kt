package com.github.wonsim02.examples.jpapolymorphism.entity1

import com.github.wonsim02.examples.jpapolymorphism.Constants
import com.github.wonsim02.examples.jpapolymorphism.model.SinglePaymentOrder
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * [SinglePaymentOrder]의 추가적인 속성에 대한 JPA 엔티티.
 * [JpaOrderEntity.id]와 값을 공유하는 [id] 및 [SinglePaymentOrder]의 추가적인 속성에 대한 열(Column)만을 가지고 있다.
 */
@Entity(name = JpaSinglePaymentOrderEntity.ENTITY_NAME)
@Table(
    schema = Constants.SCHEMA_NAME,
    name = JpaSinglePaymentOrderEntity.TABLE_NAME,
)
open class JpaSinglePaymentOrderEntity(
    @Id
    val id: Long,

    @Column(name = "single_payment_product_id")
    val singlePaymentProductId: Long,

    @Column(name = "paid_amount")
    val paidAmount: Int,

    @Column(name = "paid_at")
    val paidAt: Instant,
) {

    constructor(
        singlePaymentOrder: SinglePaymentOrder,
        newId: Long? = null,
    ) : this(
        id = newId ?: singlePaymentOrder.id,
        singlePaymentProductId = singlePaymentOrder.singlePaymentProductId,
        paidAmount = singlePaymentOrder.paidAmount,
        paidAt = singlePaymentOrder.paidAt,
    )

    companion object {

        const val ENTITY_NAME = "JpaSinglePaymentOrderEntity"
        const val TABLE_NAME = "order_single_payment"
    }
}
