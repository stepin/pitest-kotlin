package name.stepin.pitest.kotlin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class LibFunctionTest {

    @Test
    fun someLibraryMethodReturnsTrue() {
        val classUnderTest = LibFunction()
        assertTrue(classUnderTest.someFunc(), "someLibraryMethod should return 'true'")
    }
}
