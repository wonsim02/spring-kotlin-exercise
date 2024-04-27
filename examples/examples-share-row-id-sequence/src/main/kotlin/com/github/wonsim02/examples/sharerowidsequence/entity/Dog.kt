package com.github.wonsim02.examples.sharerowidsequence.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity(name = "Dog")
@Table(schema = "public", name = "dog")
open class Dog(
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val petId: Long = 0L,

    @Column(name = "name")
    val name: String,

    @Column(name = "species")
    @Enumerated(value = EnumType.STRING)
    val species: Species,
) {

    enum class Species {

        BEAGLE,
        CHIHUAHUA,
        DALMATIAN,
        GOLDEN_RETRIEVER,
        SHIH_TZU,
        ;
    }
}
