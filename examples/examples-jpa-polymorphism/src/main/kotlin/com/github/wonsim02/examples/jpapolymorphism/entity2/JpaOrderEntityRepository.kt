package com.github.wonsim02.examples.jpapolymorphism.entity2

import org.springframework.data.jpa.repository.JpaRepository

interface JpaOrderEntityRepository : JpaRepository<JpaOrderEntity<*>, Long>
