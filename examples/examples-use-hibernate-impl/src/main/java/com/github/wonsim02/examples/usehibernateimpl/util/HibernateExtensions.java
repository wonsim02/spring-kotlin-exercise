package com.github.wonsim02.examples.usehibernateimpl.util;

import org.hibernate.action.internal.EntityInsertAction;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.internal.AbstractSaveEventListener;
import org.hibernate.event.internal.DefaultMergeEventListener;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.SessionImpl;
import org.hibernate.jdbc.Expectation;
import org.hibernate.jdbc.Expectations;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.HibernateException;
import org.hibernate.persister.entity.EntityPersister;
import org.jetbrains.annotations.NotNull;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.Map;

/**
 * Hibernate의 구현을 응용한 확장 함수 모음.
 * Javadoc을 사용하기 위해 JAVA로 작성함.
 */
class HibernateExtensions {

    /**
     * {@link AbstractEntityPersister#insert(Serializable, Object[], boolean[], int, String, Object, SharedSessionContractImplementor) insert}
     * 함수에서 배치 쿼리 실행 여부가 {@code false}일 때의 로직을 실행한다.
     */
    static int performEarlyInsert(
        @NotNull AbstractEntityPersister persister,
        @NotNull Serializable id,
        @NotNull Object[] fields,
        @NotNull boolean[] notNull,
        int idx,
        @NotNull String sql,
        @NotNull SessionImpl session
    ) throws SQLException, HibernateException {
        @NotNull Expectation expectation = Expectations.appropriateExpectation(
            persister.getInsertResultCheckStyles()[idx]
        );
        boolean callable = persister.isInsertCallable(idx);

        @NotNull PreparedStatement insert = session
            .getJdbcCoordinator()
            .getStatementPreparer()
            .prepareStatement(sql, callable);

        persister.dehydrate(
            /* id = */ id,
            /* fields = */ fields,
            /* rowId = */ null,
            /* includeProperty = */ notNull,
            /* includeColumns = */ persister.getPropertyColumnInsertable(),
            /* j = */ idx,
            /* ps = */ insert,
            /* session = */ session,
            /* index = */1 + expectation.prepare(insert),
            /* isUpdate = */false
        );

        return session
            .getJdbcCoordinator()
            .getResultSetReturn()
            .executeUpdate(insert);
    }

    /**
     * {@link AbstractEntityPersister#insert(Serializable, Object[], Object, SharedSessionContractImplementor) insert}
     * 함수에서 SQL 쿼리에 {@code on conflict} 구문을 추가하여 실행한다.
     * @param entity {@code insert} 쿼리를 실행하려는 대상
     * @param session {@link EntityManager#getDelegate()}로 얻은 {@link SessionImpl}
     * @param persister {@param entity}에 대한 {@link AbstractEntityPersister}
     * @param mergeMap {@link DefaultMergeEventListener#getMergeMap}로 얻은 {@link Map}
     * @param onConflictStatement {@code on conflict} 구문으로 지정할 행동
     * @param entityIdGetter {@param entity}의 ID를 반환하는 함수
     * @param <T> 엔티티의 타입
     * @param <ID> 엔티티 ID의 타입
     * @return {@code insert} 구문으로 엔티티가 추가되었는지 여부
     * @throws SQLException {@link HibernateExtensions#performEarlyInsert(AbstractEntityPersister, Serializable, Object[], boolean[], int, String, SessionImpl)}로부터 생성된 예외
     * @throws HibernateException {@link HibernateExtensions#performEarlyInsert(AbstractEntityPersister, Serializable, Object[], boolean[], int, String, SessionImpl)}로부터 생성된 예외
     * @see AbstractSaveEventListener#performSaveOrReplicate(Object, EntityKey, EntityPersister, boolean, Object, EventSource, boolean)
     * @see EntityInsertAction
     */
    static <T, ID extends Serializable> boolean performConstraintViolationInsertion(
        @NotNull T entity,
        @NotNull SessionImpl session,
        @NotNull AbstractEntityPersister persister,
        @SuppressWarnings("rawtypes") @NotNull Map mergeMap,
        @NotNull String onConflictStatement,
        @NotNull Function<T, ID> entityIdGetter
    ) throws SQLException, HibernateException {
        @NotNull Object[] propertyValues = persister.getPropertyValuesToInsert(
            /* object = */ entity,
            /* mergeMap = */ mergeMap,
            /* session = */ ((SharedSessionContractImplementor) session)
        );
        boolean[] propertyInsertability = persister.getPropertyInsertability();
        String[] sqlInsertStrings = persister.getSQLInsertStrings();

        boolean insertSuccessful = true;
        for (int idx = 0; idx < sqlInsertStrings.length; idx++) {
            @NotNull String sql = sqlInsertStrings[idx];
            @NotNull String newSql = sql + " on conflict " + onConflictStatement;
            @NotNull ID id = entityIdGetter.apply(entity);

            int insertedCount = performEarlyInsert(
                /* persister = */ persister,
                /* id = */ id,
                /* fields = */ propertyValues,
                /* notNull = */ propertyInsertability,
                /* idx = */ idx,
                /* sql = */ newSql,
                /* session = */ session
            );

            if (insertedCount != 1) {
                insertSuccessful = false;
            }
        }

        return insertSuccessful;
    }
}
