package name.stepin.pitest.kotlin

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class LibFunctionTest {

    @Test
    fun someLibraryMethodReturnsTrue() {
        val classUnderTest = LibFunction()
        assertTrue(classUnderTest.someFunc(), "someLibraryMethod should return 'true'")
    }
}
