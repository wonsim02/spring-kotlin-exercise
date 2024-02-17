package com.github.wonsim02.infra.mongodb

/**
 * 추가적인 Mongo 데이터베이스 이름을 제공할 수 있는 빈 인터페이스.
 * @see com.github.wonsim02.infra.mongodb.config.AdditionalMongoDatabasesConfiguration.additionalMongoDatabasesRegistrar
 */
fun interface AdditionalMongoDatabaseNamesSupplier {

    operator fun invoke(): Iterable<String>
}
