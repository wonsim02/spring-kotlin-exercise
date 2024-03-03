package com.github.wonsim02.infra.mongodb.util

import com.github.wonsim02.infra.mongodb.testcase.document.Document6
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.lang.reflect.Modifier

class MongoDocumentPropertiesProcessorTest {

    @Test
    fun `processing Document6 works as expected`() {
        val document6ConstantsClass = assertDoesNotThrow {
            Class.forName(Document6::class.java.name + "Constants")
        }

        document6ConstantsClass.verifyHasStaticFinalStringFields(
            // verify for primitive fields
            "id" to "id",
            "a" to "a",
            "b" to "b",
            "c" to "c",
            "subDocumentsLists" to "subDocumentsLists",
            // verify for non-primitive fields
            "subDocument" to "subDocument.",
            "subDocuments" to "subDocuments.",
        )

        val document6ConstantsMemberClasses = document6ConstantsClass.classes
        assertEquals(2, document6ConstantsMemberClasses.size)

        val document6ConstantsSubDocumentClass = assertDoesNotThrow {
            document6ConstantsMemberClasses.find { it.simpleName == "SubDocument" }!!
        }
        document6ConstantsSubDocumentClass.verifyHasStaticFinalStringFields(
            // verify for primitive fields
            "a" to "subDocument.a",
            "b" to "subDocument.b",
            "c" to "subDocument.c",
            // verify for non-primitive fields
            "subSubDocuments" to "subDocument.subSubDocuments.",
        )

        val document6ConstantsSubDocumentMemberClasses = document6ConstantsSubDocumentClass.classes
        assertEquals(1, document6ConstantsSubDocumentMemberClasses.size)
        val document6ConstantsSubDocumentSubSubDocumentsClass = document6ConstantsSubDocumentMemberClasses[0]
        assertEquals("SubSubDocuments", document6ConstantsSubDocumentSubSubDocumentsClass.simpleName)

        document6ConstantsSubDocumentSubSubDocumentsClass.verifyHasStaticFinalStringFields(
            // verify for primitive fields
            "a" to "subDocument.subSubDocuments.a",
            "b" to "subDocument.subSubDocuments.b",
        )

        val document6ConstantsSubDocumentsClass = assertDoesNotThrow {
            document6ConstantsMemberClasses.find { it.simpleName == "SubDocuments" }!!
        }
        document6ConstantsSubDocumentsClass.verifyHasStaticFinalStringFields(
            // verify for primitive fields
            "a" to "subDocuments.a",
            "b" to "subDocuments.b",
            "c" to "subDocuments.c",
            // verify for non-primitive fields
            "subSubDocuments" to "subDocuments.subSubDocuments.",
        )

        val document6ConstantsSubDocumentsMemberClasses = document6ConstantsSubDocumentsClass.classes
        assertEquals(1, document6ConstantsSubDocumentsMemberClasses.size)
        val document6ConstantsSubDocumentsSubSubDocumentsClass = document6ConstantsSubDocumentsMemberClasses[0]
        assertEquals("SubSubDocuments", document6ConstantsSubDocumentsSubSubDocumentsClass.simpleName)

        document6ConstantsSubDocumentsSubSubDocumentsClass.verifyHasStaticFinalStringFields(
            // verify for primitive fields
            "a" to "subDocuments.subSubDocuments.a",
            "b" to "subDocuments.subSubDocuments.b",
        )
    }

    private fun Class<*>.verifyHasStaticFinalStringFields(
        vararg fieldNameAndValuePairs: Pair<String, String>,
    ) {
        assertEquals(fieldNameAndValuePairs.size, fields.size)

        for ((fieldName, fieldValue) in fieldNameAndValuePairs) {
            assertTrue(
                fields.any { field ->
                    val modifiers = field.modifiers
                    if (!Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers)) {
                        return@any false
                    }
                    if (field.name != fieldName) return@any false

                    val actualStringValue = field.get(null) as? String ?: return@any false
                    actualStringValue == fieldValue
                },
                "$name should have static final String field of name=$fieldName and value=\"$fieldValue\".",
            )
        }
    }
}
