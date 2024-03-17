package com.github.wonsim02.infra.mongodb.util

import java.io.Closeable
import java.io.Flushable
import java.io.Writer

/**
 * 대상 클래스의 모든 nested 속성을 조회한 결과를 JAVA 파일로 작성하는 writer.
 */
internal class MongoDocumentPropertiesPathWriter(
    private val originalWriter: Writer,
) : Flushable by originalWriter, Closeable by originalWriter {

    private fun writeLine(line: String, numIndents: Int = 0) {
        originalWriter.write(INDENT.repeat(numIndents))
        originalWriter.write(line)
        originalWriter.write(NEW_LINE)
    }

    fun writeEmptyLine() {
        writeLine(line = "", numIndents = 0)
    }

    fun writePackage(packageName: String) {
        writeLine("package $packageName$SEMICOLON")
        originalWriter.write(NEW_LINE)
    }

    fun writeConstantStringField(fieldName: String, fieldStringValue: String, numIndents: Int) {
        writeLine("public static final String $fieldName = \"$fieldStringValue\"$SEMICOLON", numIndents)
    }

    fun writeClassStart(
        className: String,
        level: Int,
    ) {
        if (level == 0) {
            writeLine("public class $className $BRACKET_START", level)
        } else {
            writeLine("public static class $className $BRACKET_START", level)
        }
    }

    fun writeClassEnd(level: Int) {
        writeLine(BRACKET_END, level)
    }

    companion object {
        private const val INDENT = "    "
        private const val SEMICOLON = ";"
        private const val NEW_LINE = "\n"
        private const val BRACKET_START = "{"
        private const val BRACKET_END = "}"
    }
}
