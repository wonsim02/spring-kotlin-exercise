package com.github.wonsim02.infra.mongodb.util

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

/**
 * 주어진 클래스의 모든 nested 속성의 경로를 해석한 결과.
 * @param target nested 속성을 해석할 대상 클래스
 * @property className 해석 대상 클래스로부터 새로 생성할 클래스의 이름
 * @param processingEnv [javax.annotation.processing.Processor.process] 함수로부터 주입받은 환경
 * @param prefix top-level 클래스의 속성으로부터 해석 대상 클래스에 도달하기까지 거친 속성 이름 목록
 * @property primitiveFields
 * @property nonPrimitiveFields
 * @property joinedPrefixes
 */
internal class MongoDocumentPropertyTypes(
    target: TypeElement,
    val className: String,
    processingEnv: ProcessingEnvironment,
    vararg prefix: String
) {
    val primitiveFields: Map<String, String>
    val nonPrimitiveFields: Map<String, MongoDocumentPropertyTypes>
    val joinedPrefixes: String? = prefix
        .takeUnless { it.isEmpty() }
        ?.joinToString(SEPARATOR)
        ?.plus(SEPARATOR)

    init {
        val primitiveFieldsMap: MutableMap<String /* field name */, String /* full path */> = mutableMapOf()
        val nonPrimitiveFieldsMap: MutableMap<String /* field name */, MongoDocumentPropertyTypes> = mutableMapOf()

        for (field in target.enclosedElements) {
            // `field`가 변수가 아니거나 `static` 속성이면 무시한다.
            if (field !is VariableElement || field.modifiers.contains(Modifier.STATIC)) continue

            val parseResult = MongoDocumentPropertyTypeParseResult.of(
                field = field,
                processingEnv = processingEnv
            ) ?: continue

            when (parseResult) {
                is MongoDocumentPropertyTypeParseResult.Primitive
                -> primitiveFieldsMap[parseResult.fieldName] = joinedPrefixes
                    ?.let { it + parseResult.fieldName }
                    ?: parseResult.fieldName
                is MongoDocumentPropertyTypeParseResult.NonPrimitive
                -> nonPrimitiveFieldsMap[parseResult.fieldName] = MongoDocumentPropertyTypes(
                    parseResult.fieldTypeElement,
                    parseResult.generatedClassName,
                    processingEnv,
                    *prefix, parseResult.fieldName,
                )
            }
        }

        primitiveFields = primitiveFieldsMap
        nonPrimitiveFields = nonPrimitiveFieldsMap
    }

    companion object {

        private const val SEPARATOR = "."
    }
}
