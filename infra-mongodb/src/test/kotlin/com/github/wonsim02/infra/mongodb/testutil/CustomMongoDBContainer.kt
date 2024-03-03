package com.github.wonsim02.infra.mongodb.testutil

import com.github.wonsim02.infra.mongodb.InfraMongodbTestBase
import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName

/**
 * [MongoDBContainer]가 replica set을 만들려고 해서 직접 만든 [GenericContainer]의 구현체.
 */
class CustomMongoDBContainer(
    dockerImageName: DockerImageName,
    private val database: String,
    private val username: String,
    private val password: String,
) : GenericContainer<CustomMongoDBContainer>(dockerImageName) {

    init {
        withEnv("MONGO_INITDB_DATABASE", database)
        withEnv("MONGO_INITDB_ROOT_USERNAME", username)
        withEnv("MONGO_INITDB_ROOT_PASSWORD", password)
        withExposedPorts(27017)
        withNetwork(Network.SHARED)
    }

    constructor() : this(
        dockerImageName = DockerImageName
            .parse("public.ecr.aws/docker/library/mongo:4.0")
            .asCompatibleSubstituteFor("mongo"),
        database = InfraMongodbTestBase.DATABASE,
        username = InfraMongodbTestBase.USERNAME,
        password = InfraMongodbTestBase.PASSWORD,
    )

    /**
     * 실제 컨데이너 외부에서 접속 가능한 포트를 반환한다.
     * @see <a href="https://java.testcontainers.org/features/networking/">
     *     Networking and communicating with containers
     *     </a>
     */
    private fun getMappedPorts(): List<Int> {
        return exposedPorts.map(this::getMappedPort)
    }

    private val springPropertyGetterMap: Map<String, () -> Any> = mapOf(
        "spring.data.mongodb.host" to ::getHost,
        "spring.data.mongodb.port" to { getMappedPorts().first() },
        "spring.data.mongodb.database" to { database },
        "spring.data.mongodb.username" to { username },
        "spring.data.mongodb.password" to { password },
        "CONF_MONGODB_AUTHENTICATION_DATABASE" to { "admin" },
    )

    fun provideMongoDbProperties(): Array<String> {
        return springPropertyGetterMap
            .map { (propertyKey, propertyValueGetter) ->
                "--$propertyKey=${propertyValueGetter()}"
            }
            .toTypedArray()
    }

    fun setTestContainerProperties(registry: DynamicPropertyRegistry) {
        for ((propertyKey, propertyValueGetter) in springPropertyGetterMap) {
            registry.add(propertyKey, propertyValueGetter)
        }
    }
}
