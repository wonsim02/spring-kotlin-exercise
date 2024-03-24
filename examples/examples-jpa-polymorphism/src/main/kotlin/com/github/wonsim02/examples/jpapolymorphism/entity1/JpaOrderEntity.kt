package com.github.wonsim02.examples.jpapolymorphism.entity1

import com.github.wonsim02.examples.jpapolymorphism.Constants
import com.github.wonsim02.examples.jpapolymorphism.model.Order
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

/**
 * [Order]에 대한 JPA 엔티티.
 * [Order] 구현체가 공통으로 가지는 속성인 [Order.id], [Order.userId] 및 [Order.type]에 대응되는 열(Column)만을 가지고 있다.
 */
@Entity(name = JpaOrderEntity.ENTITY_NAME)
@Table(
    schema = Constants.SCHEMA_NAME,
    name = JpaOrderEntity.TABLE_NAME,
)
open class JpaOrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "user_id")
    val userId: Long,

    @Column(name = "type")
    @Enumerated(value = EnumType.STRING)
    val type: Order.Type,
) {

    constructor(order: Order) : this(
        id = order.id,
        userId = order.userId,
        type = order.type,
    )

    companion object {

        const val ENTITY_NAME = "JpaOrderEntity"
        const val TABLE_NAME = "order"
    }
}
