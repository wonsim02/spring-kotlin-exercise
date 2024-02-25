package com.github.wonsim02.infra.mongodb

/**
 * Mongo 엔티티에 대해 Mongo 인덱스 정의를 지정한다.
 * [locationPattern]이 가리키는 위치에 존재하는 JSON 문서를 해석하여 인덱스를 생성하게 된다.
 * 인덱스를 생성하려는 JSON 파일은 다음 조건을 만족해야 한다:
 * - 파일 이름이 정규 표현식 기준 `idx\d+__`로 시작해야 한다.
 * - 파일 이름이 `.json`으로 끝나야 한다.
 */
annotation class MongoIndexDefinitionSource(val locationPattern: String) {

    companion object {

        internal data class ParseFilenameResult(
            val indexName: String,
            val description: String?,
        )

        private const val INDEX_GROUP_NAME = "index"
        private const val DESCRIPTION_GROUP_NAME = "description"
        private const val RESOURCE_NAME_PATTERN = "^(?<$INDEX_GROUP_NAME>idx\\d+)(__(?<$DESCRIPTION_GROUP_NAME>.*)|)\\.json\$"
        private val RESOURCE_NAME_REGEX = RESOURCE_NAME_PATTERN.toRegex()

        internal fun parseFilename(filename: String): ParseFilenameResult? {
            val matchResult = RESOURCE_NAME_REGEX.find(filename) ?: return null
            val indexName = matchResult.groups[INDEX_GROUP_NAME]?.value ?: return null
            val description = matchResult.groups[DESCRIPTION_GROUP_NAME]?.value

            return ParseFilenameResult(indexName, description)
        }
    }
}
