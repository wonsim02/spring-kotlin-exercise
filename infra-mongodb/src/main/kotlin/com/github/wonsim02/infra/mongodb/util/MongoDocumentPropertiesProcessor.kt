package com.github.wonsim02.infra.mongodb.util

import org.springframework.data.mongodb.core.mapping.Document
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * [Document] 어노테이션이 첨부된 클래스의 모든 nested 속성을 조회하여 nested 속성의 경로를 상수로 정의한 클래스를 생성하는 어노테이션 프로세서.
 * Mongo 쿼리 작성 시 이 프로세서로 생성된 상수를 사용하게 함으로써 만약 원본 Mongo 엔티티가 변경되었을 때 런타임 단계가 아닌 컴파일 단계에서 에러를
 * 인지할 수 있도록 한다. 예를 들어,
 * ```kotlin
 * @Document
 * class A(
 *     val id: Long,
 *     val a: Int,
 *     val bValue: B,
 * ) {
 *     class B(val c: List<C>)
 *
 *     class C(val d: Instant)
 * }
 * ```
 * 와 같은 Mongo 엔티티가 있으면, Mongo 쿼리로 작성 가능한 A의 nested 속성은 `id`, `a`, `bValue`, `bValue.c`, `bValue.c.d`이고,
 * 이 프로세서로 생성되는 클래스는 아래와 같이 nested 속성 경로를 모두 포함하게 된다.
 * ```java
 * public class AConstants {
 *
 *     public static final string id = "id";
 *
 *     public static final string a = "a";
 *
 *     public static final string bValue = "bValue.";
 *
 *     public static class BValue {
 *
 *         public static final string c = "bValue.c.";
 *
 *         public static class C {
 *
 *             public static final string d = "bValue.c.d";
 *
 *         }
 *
 *     }
 *
 * }
 * ```
 * 그리고 `A` 클래스의 nested 속성에 대한 쿼리를 작성할 때는 다음과 같이 생성된 클래스를 사용하면 된다:
 * - `AConstants.id`의 값은 `id`이다.
 * - `AConstants.a`의 값은 `a`이다.
 * - `AConstants.bValue`의 값은 `bValue.`이다.
 * - `AConstants.BValue.c`의 값은 `bValue.c.`이다.
 * - `AConstants.BValue.C.d`의 값은 `bValue.c.d`이다.
 */
@SupportedAnnotationTypes(MongoDocumentPropertiesProcessor.DOCUMENT_CLASS_NAME)
internal class MongoDocumentPropertiesProcessor : AbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion {
        return processingEnv.sourceVersion
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val targets = annotations
            .asSequence()
            .flatMap { roundEnv.getElementsAnnotatedWith(it) }
            .filterIsInstance<TypeElement>()
            .toSet()

        targets.forEach(::generateSource)
        return true
    }

    private fun generateSource(target: TypeElement) {
        val originalPackage = processingEnv.elementUtils
            .getPackageOf(target)
            .qualifiedName
            .toString()
        val originalSimpleName = target.simpleName.toString()

        val newSimpleName = originalSimpleName + GENERATED_CLASS_POSTFIX
        val newFullName = if (originalPackage.isEmpty()) {
            newSimpleName
        } else {
            "$originalPackage.$newSimpleName"
        }

        val writer = processingEnv.filer
            .createSourceFile(newFullName, target)
            .openWriter()

        MongoDocumentPropertiesPathWriter(writer).use { pathWriter ->
            pathWriter.writePackage(originalPackage)
            val mongoDocumentPropertyType = MongoDocumentPropertyTypes(target, newSimpleName, processingEnv)
            recursiveWriteClass(pathWriter, mongoDocumentPropertyType, 0)
        }
    }

    private fun recursiveWriteClass(
        writer: MongoDocumentPropertiesPathWriter,
        target: MongoDocumentPropertyTypes,
        level: Int,
    ) {
        writer.writeClassStart(target.className, level)
        writer.writeEmptyLine()

        for ((fieldName, fieldStringValue) in target.primitiveFields) {
            writer.writeConstantStringField(fieldName, fieldStringValue, level + 1)
            writer.writeEmptyLine()
        }

        val nestedClasses: MutableMap<String /* class name */, MongoDocumentPropertyTypes> = mutableMapOf()
        for ((fieldName, fieldValue) in target.nonPrimitiveFields) {
            val nestedClassName = fieldValue.className
            val prefix = fieldValue.joinedPrefixes

            if (prefix != null) {
                writer.writeConstantStringField(fieldName, prefix, level + 1)
                writer.writeEmptyLine()
            }

            nestedClasses[nestedClassName] = fieldValue
        }

        for (nestedClass in nestedClasses.values) {
            recursiveWriteClass(writer, nestedClass, level + 1)
            writer.writeEmptyLine()
        }

        writer.writeClassEnd(level)
        writer.flush()
    }

    companion object {

        const val GENERATED_CLASS_POSTFIX = "Constants"
        const val DOCUMENT_CLASS_NAME = "org.springframework.data.mongodb.core.mapping.Document"
    }
}
