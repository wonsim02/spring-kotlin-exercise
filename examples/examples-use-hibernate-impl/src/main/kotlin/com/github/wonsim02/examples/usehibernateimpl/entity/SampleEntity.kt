package com.github.wonsim02.examples.usehibernateimpl.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(
    schema = "public",
    name = "sample_entity",
    uniqueConstraints = [
        UniqueConstraint(
            name = "unique_property__uniq",
            columnNames = [SampleEntity.UNIQUE_PROPERTY],
        ),
    ],
)
class SampleEntity(
    @Id
    val id: Long,

    @Column(name = UNIQUE_PROPERTY)
    val uniqueProperty: String,

    @Column(name = NON_UNIQUE_PROPERTY)
    val nonUniqueProperty: Int,
) {

    companion object {

        const val UNIQUE_PROPERTY = "unique_property"
        const val NON_UNIQUE_PROPERTY = "non_unique_property"
    }
}
