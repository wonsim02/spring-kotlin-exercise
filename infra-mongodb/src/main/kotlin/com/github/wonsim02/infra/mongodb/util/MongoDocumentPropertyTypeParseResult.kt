package com.github.wonsim02.infra.mongodb.util

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

/**
 * [VariableElement]를 해석하여 해당 속성 하위의 속성에 대한 Mongo 쿼리를 생성할 수 있는지 여부를 파악한다.
 * @property fieldName [VariableElement]가 가리키는 속성의 이름
 */
internal sealed class MongoDocumentPropertyTypeParseResult {

    abstract val fieldName: String

    /**
     * 하위 속성에 대한 Mongo 쿼리를 생성할 수 없음. 다음 중 한 가지 경우에 해당한다.
     * - [fieldName]이 가리키는 속성의 타입이 Java 언어의 primitive 타입이거나 [Enum] 타입이다.
     * - [fieldName]이 가리키는 속성의 타입이 [java.util.Collection] 타입이거나 배열 타입이 아니며, `java` 하위 패키지에 존재한다.
     * - [fieldName]이 가리키는 속성의 타입이 [java.util.Collection] 타입이거나 배열 타입이며, 원소의 타입이 Java 언어의 primitive 타입이거나
     *  [Enum] 타입이거나 `java` 하위 패키지에 존재한다.
     */
    class Primitive(override val fieldName: String) : MongoDocumentPropertyTypeParseResult()

    /**
     * 하위 속성에 대한 Mongo 쿼리를 생성할 수 있음. 다음 중 한 가지 경우에 해당한다.
     * - [fieldName]이 가리키는 속성의 타입이 Java 언어의 primitive 타입이거나 `java` 하위 패키지에 존재하지 않는다.
     * - [fieldName]이 가리키는 속성의 타입이 [java.util.Collection] 타입이거나 배열 타입이며, 원소의 타입이 Java 언어의 primitive 타입이거나
     *  [Enum] 타입이거나 `java` 하위 패키지에 존재하지 않는다.
     */
    class NonPrimitive(
        override val fieldName: String,
        val fieldTypeElement: TypeElement,
    ) : MongoDocumentPropertyTypeParseResult()

    companion object {

        private const val TYPE_PARAMETER_PATTERN = "^(?<className>[^<>]+)(<(?<typeParameter>.*)>)?$"
        private val TYPE_PARAMETER_REGEX = TYPE_PARAMETER_PATTERN.toRegex()

        /**
         * [field]를 해석하여 [MongoDocumentPropertyTypeParseResult]를 생성한다.
         * 만약 [field]의 타입의 판별할 수 없으면 `null`을 반환한다.
         */
        fun of(
            field: VariableElement,
            processingEnv: ProcessingEnvironment,
        ): MongoDocumentPropertyTypeParseResult? {
            val fieldType = field.asType()
            val matchResult = TYPE_PARAMETER_REGEX.find(fieldType.toString()) ?: return null
            val className = matchResult.groups["className"]?.value ?: return null
            val typeParameter = matchResult.groups["typeParameter"]?.value

            val fieldTypeElement = processingEnv
                .elementUtils
                .getAllTypeElements(className)
                .firstOrNull()
                ?: return genPrimitive(field)

            val fieldTypeClass = try {
                Class.forName(className)
            } catch (_: Throwable) {
                // 클래스를 불러오는 데 실패했다는 것은 해당 클래스가 Annotation Processing을 실행하는 모듈에서 정의되어 있다는 의미이므로
                // non-primitive로 간주한다.
                return genNonPrimitive(field, fieldTypeElement)
            }

            return when {
                // 타입이 primitive이거나 Enum이면 primitive로 간주한다.
                fieldTypeClass.isPrimitive || fieldTypeClass.isEnum
                -> genPrimitive(field)
                // 타입이 배열이거나 `Collection`이면 원소의 타입을 검사한다.
                // 만약 원소의 타입을 판별할 수 없으면 primitive로 간주한다.
                fieldTypeClass.isArray || java.util.Collection::class.java.isAssignableFrom(fieldTypeClass)
                -> {
                    if (typeParameter == null) return genPrimitive(field)

                    val typeParameterTypeElement = processingEnv
                        .elementUtils
                        .getAllTypeElements(typeParameter)
                        .firstOrNull()
                        ?: return genPrimitive(field)

                    val typeParameterClass = try {
                        Class.forName(typeParameter)
                    } catch (_: Throwable) {
                        // 클래스를 불러오는 데 실패했다는 것은 해당 클래스가 Annotation Processing을 실행하는 모듈에서 정의되어 있다는 의미이므로
                        // non-primitive로 간주한다.
                        return genNonPrimitive(field, typeParameterTypeElement)
                    }

                    if (typeParameterClass.isPrimitive || typeParameterClass.isEnum || typeParameter.startsWith("java.")) {
                        genPrimitive(field)
                    } else {
                        genNonPrimitive(field, typeParameterTypeElement)
                    }
                }
                className.startsWith("java") -> genPrimitive(field)
                else -> genNonPrimitive(field, fieldTypeElement)
            }
        }

        private fun genPrimitive(field: VariableElement): Primitive {
            return Primitive(fieldName = field.simpleName.toString())
        }

        private fun genNonPrimitive(
            field: VariableElement,
            fieldTypeElement: TypeElement,
        ): NonPrimitive {
            return NonPrimitive(
                fieldName = field.simpleName.toString(),
                fieldTypeElement = fieldTypeElement,
            )
        }
    }
}
