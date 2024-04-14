package com.github.wonsim02.examples.usehibernateimpl.util

import org.hibernate.event.internal.AbstractSaveEventListener
import org.hibernate.event.internal.DefaultMergeEventListener
import org.hibernate.event.internal.MergeContext
import org.hibernate.event.spi.EntityCopyObserverFactory
import org.hibernate.internal.SessionImpl
import org.hibernate.persister.entity.AbstractEntityPersister
import java.io.Serializable
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * [DefaultMergeEventListener.onMerge] 함수 내 로직에서 [AbstractEntityPersister.getPropertyValuesToInsert]의 안자로 사용할
 * [Map]을 구하는 과정을 재현한 함수.
 * @see DefaultMergeEventListener.onMerge
 * @see DefaultMergeEventListener.createEntityCopyObserver
 * @see DefaultMergeEventListener.getMergeMap
 * @see AbstractSaveEventListener.performSave
 * @see AbstractSaveEventListener.performSaveOrReplicate
 */
internal fun getMergeMap(session: SessionImpl): Map<*, *> {
    val serviceRegistry = session.factory.serviceRegistry
    val entityCopyObserver = serviceRegistry
        .getService(EntityCopyObserverFactory::class.java)
        .createEntityCopyObserver()

    return MergeContext(session, entityCopyObserver).invertMap()
}

/**
 * constraint violation을 발생시키지 않고 엔티티 `insert`를 수행할 수 있게 한다.
 * @param entity `insert`하려는 엔티티
 * @param entityManager [PersistenceContext] 어노테이션을 통해 주입받은 [EntityManager]
 * @param onConflictStatement `on conflict` 구문으로 지정할 행동
 * @param entityIdGetter [entity]의 ID를 반환하는 함수
 * @return [Boolean] 엔티티 `insert` 성공 여부
 */
internal fun <T : Any, ID : Serializable> performConstraintViolationSafeInsertion(
    entity: T,
    entityManager: EntityManager,
    onConflictStatement: String = "do nothing",
    entityIdGetter: (T) -> ID,
): Boolean {
    val session = entityManager.delegate as? SessionImpl ?: return false
    val persister = session
        .getEntityPersister(null, entity) as? AbstractEntityPersister
        ?: return false

    return HibernateExtensions.performConstraintViolationInsertion(
        /* entity = */ entity,
        /* session = */ session,
        /* persister = */ persister,
        /* mergeMap = */ getMergeMap(session),
        /* onConflictStatement = */ onConflictStatement,
        /* entityIdGetter = */ entityIdGetter,
    )
}
