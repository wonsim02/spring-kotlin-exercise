package com.github.wonsim02.infra.mongodb.testutil

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName

/**
 * [MongoDBContainer]가 replica set을 만들려고 해서 직접 만든 [GenericContainer]의 구현체.
 */
class CustomMongoDBContainer(
    dockerImageName: DockerImageName,
    database: String,
    username: String,
    password: String,
) : GenericContainer<CustomMongoDBContainer>(dockerImageName) {

    init {
        withEnv("MONGO_INITDB_DATABASE", database)
        withEnv("MONGO_INITDB_ROOT_USERNAME", username)
        withEnv("MONGO_INITDB_ROOT_PASSWORD", password)
        withExposedPorts(27017)
        withNetwork(Network.SHARED)
    }

    /**
     * 실제 컨데이너 외부에서 접속 가능한 포트를 반환한다.
     * @see <a href="https://java.testcontainers.org/features/networking/">
     *     Networking and communicating with containers
     *     </a>
     */
    fun getMappedPorts(): List<Int> {
        return exposedPorts.map(this::getMappedPort)
    }
}
