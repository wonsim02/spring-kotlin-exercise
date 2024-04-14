package com.github.wonsim02.examples.usehibernateimpl.entity

import org.springframework.data.jpa.repository.JpaRepository

interface SampleEntityJpaRepository : JpaRepository<SampleEntity, Long>
