package com.app.financetracker.parser

import org.junit.Assert.assertThrows
import org.junit.Test

class ParserPurityTest {

    @Test
    fun `android classes are not on the classpath`() {
        assertThrows(ClassNotFoundException::class.java) {
            Class.forName("android.content.Context")
        }
    }
}
