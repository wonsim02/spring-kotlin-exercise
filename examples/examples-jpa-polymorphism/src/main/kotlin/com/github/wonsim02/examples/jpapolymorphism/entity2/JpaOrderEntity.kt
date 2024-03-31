package com.github.wonsim02.examples.jpapolymorphism.entity2

import com.github.wonsim02.examples.jpapolymorphism.Constants
import com.github.wonsim02.examples.jpapolymorphism.model.Order
import javax.persistence.Column
import javax.persistence.DiscriminatorColumn
import javax.persistence.DiscriminatorType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.Table

/**
 * [JPA Polymorphism](https://www.baeldung.com/hibernate-inheritance#joined-table)을 이용한 [Order]에 대한 JPA 엔티티.
 * [Order.type]에 대응되는 `type` 열은 판별자(Discriminator) 열로 사용된다.
 */
@Entity(name = JpaOrderEntity.ENTITY_NAME)
@Table(
    schema = Constants.SCHEMA_NAME,
    name = JpaOrderEntity.TABLE_NAME,
)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(
    name = JpaOrderEntity.DISCRIMINATOR_COLUMN,
    discriminatorType = DiscriminatorType.STRING,
)
sealed class JpaOrderEntity<ORDER : Order>(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "user_id")
    val userId: Long,
) {

    constructor(order: Order) : this(
        id = order.id,
        userId = order.userId,
    )

    abstract fun toOrder(): ORDER

    companion object {

        const val ENTITY_NAME = "JpaOrderEntity"
        const val TABLE_NAME = "order"

        const val DISCRIMINATOR_COLUMN = "type"
    }
}
