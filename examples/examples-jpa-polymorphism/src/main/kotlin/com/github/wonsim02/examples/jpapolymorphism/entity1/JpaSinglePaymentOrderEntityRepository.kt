package com.github.wonsim02.examples.jpapolymorphism.entity1

import org.springframework.data.jpa.repository.JpaRepository

interface JpaSinglePaymentOrderEntityRepository : JpaRepository<JpaSinglePaymentOrderEntity, Long>
