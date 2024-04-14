package com.github.wonsim02.infra.jpa.entity

import com.vladmihalcea.hibernate.type.array.ListArrayType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(
    schema = "public",
    name = "test_entity",
)
@TypeDef(
    name = "list_array",
    typeClass = ListArrayType::class,
)
class TestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "string_property")
    val stringProperty: String,

    @Column(name = "array_property", columnDefinition = "text[]")
    @Type(type = "list_array")
    val arrayProperty: List<String>,
)
