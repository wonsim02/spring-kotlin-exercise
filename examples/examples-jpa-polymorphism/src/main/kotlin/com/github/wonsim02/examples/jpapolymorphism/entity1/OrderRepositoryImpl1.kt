package com.github.wonsim02.examples.jpapolymorphism.entity1

import com.github.wonsim02.examples.jpapolymorphism.model.Order
import com.github.wonsim02.examples.jpapolymorphism.model.OrderRepository
import com.github.wonsim02.examples.jpapolymorphism.model.SinglePaymentOrder
import com.github.wonsim02.examples.jpapolymorphism.model.SubscriptionOrder
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * 모든 [Order]의 구현체가 공유하는 속성 및 각 [Order] 구현체 별로 차별화되는 속성이 별도 테이블의 열(Column)로 저장되어 있을 때의
 * [OrderRepository] 구현체.
 * - [findById] 시에는 우선 [JpaOrderEntity]를 조회한 다음 [JpaOrderEntity.type]으로부터 [JpaSinglePaymentOrderEntity] 혹은
 *  [JpaSubscriptionOrderEntity]를 조회한다.
 * - [save] 시에는 우선 [JpaOrderEntity]를 저장한 다음 새로 생성된 [JpaOrderEntity.id]를 이용하여 [JpaSinglePaymentOrderEntity]
 *  혹은 [JpaSubscriptionOrderEntity]를 생성 및 저장한다.
 */
@Repository
class OrderRepositoryImpl1(
    private val jpaOrderRepo: JpaOrderEntityRepository,
    private val jpaSinglePaymentOrderRepo: JpaSinglePaymentOrderEntityRepository,
    private val jpaSubscriptionOrderRepo: JpaSubscriptionOrderEntityRepository,
) : OrderRepository {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun findById(id: Long): Order? {
        val jpaOrderEntity = jpaOrderRepo
            .findByIdOrNull(id)
            ?: return null

        return when (jpaOrderEntity.type) {
            Order.Type.SINGLE_PAYMENT -> {
                val jpaSinglePaymentOrderEntity = jpaSinglePaymentOrderRepo
                    .findById(id)
                    .orElseGet {
                        logger.warn("No JpaSinglePaymentOrderEntity for id={} found.", id)
                        null
                    }
                    ?: return null
                toSinglePaymentOrder(jpaOrderEntity, jpaSinglePaymentOrderEntity)
            }
            Order.Type.SUBSCRIPTION -> {
                val jpaSubscriptionOrderEntity = jpaSubscriptionOrderRepo
                    .findById(id)
                    .orElseGet {
                        logger.warn("No JpaSubscriptionOrderEntity for id={} found.", id)
                        null
                    }
                    ?: return null
                toSubscriptionOrder(jpaOrderEntity, jpaSubscriptionOrderEntity)
            }
        }
    }

    @Transactional
    override fun save(order: Order): Order {
        val jpaOrderEntity = jpaOrderRepo.save(JpaOrderEntity(order))

        return when (order) {
            is SinglePaymentOrder -> {
                val jpaSinglePaymentOrderEntity = jpaSinglePaymentOrderRepo.save(
                    JpaSinglePaymentOrderEntity(
                        singlePaymentOrder = order,
                        newId = jpaOrderEntity.id,
                    )
                )
                toSinglePaymentOrder(jpaOrderEntity, jpaSinglePaymentOrderEntity)
            }
            is SubscriptionOrder -> {
                val jpaSubscriptionOrderEntity = jpaSubscriptionOrderRepo.save(
                    JpaSubscriptionOrderEntity(
                        subscriptionOrder = order,
                        newId = jpaOrderEntity.id,
                    )
                )
                toSubscriptionOrder(jpaOrderEntity, jpaSubscriptionOrderEntity)
            }
        }
    }

    private fun toSinglePaymentOrder(
        jpaOrderEntity: JpaOrderEntity,
        jpaSinglePaymentOrderEntity: JpaSinglePaymentOrderEntity,
    ): SinglePaymentOrder {
        return SinglePaymentOrder(
            id = jpaOrderEntity.id,
            userId = jpaOrderEntity.userId,
            singlePaymentProductId = jpaSinglePaymentOrderEntity.singlePaymentProductId,
            paidAmount = jpaSinglePaymentOrderEntity.paidAmount,
            paidAt = jpaSinglePaymentOrderEntity.paidAt,
        )
    }

    private fun toSubscriptionOrder(
        jpaOrderEntity: JpaOrderEntity,
        jpaSubscriptionOrderEntity: JpaSubscriptionOrderEntity,
    ): SubscriptionOrder {
        return SubscriptionOrder(
            id = jpaOrderEntity.id,
            userId = jpaOrderEntity.userId,
            subscriptionProductId = jpaSubscriptionOrderEntity.subscriptionProductId,
            paymentAmountPerMonth = jpaSubscriptionOrderEntity.paymentAmountPerMonth,
            lastPaidAt = jpaSubscriptionOrderEntity.lastPaidAt,
        )
    }
}
