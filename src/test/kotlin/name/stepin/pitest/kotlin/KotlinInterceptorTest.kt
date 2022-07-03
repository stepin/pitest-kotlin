package name.stepin.pitest.kotlin

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.objectweb.asm.tree.AnnotationNode
import org.pitest.bytecode.analysis.ClassTree
import org.pitest.classinfo.ClassName
import org.pitest.mutationtest.build.InterceptorType
import org.pitest.mutationtest.engine.Location
import org.pitest.mutationtest.engine.Mutater
import org.pitest.mutationtest.engine.MutationDetails
import org.pitest.mutationtest.engine.MutationIdentifier
import kotlin.test.assertFalse

internal class KotlinInterceptorTest {

    @AfterEach
    fun afterTests() {
        unmockkAll()
    }

    @Test
    fun `type should be filter`() {
        val kotlinInterceptor = KotlinInterceptor()

        val type = kotlinInterceptor.type()

        assertEquals(InterceptorType.FILTER, type)
    }

    @Test
    fun `isKotlinClass should return false for empty annotations`() {
        val kotlinInterceptor = KotlinInterceptor()
        val clazz = mockk<ClassTree>()
        every { clazz.annotations() } returns emptyList()

        val isKotlinClass = kotlinInterceptor.isKotlinClass(clazz)

        assertFalse(isKotlinClass)
    }

    @Test
    fun `isKotlinClass should return false for non-Lkotlin annotations provided`() {
        val kotlinInterceptor = KotlinInterceptor()
        val clazz = mockk<ClassTree>()
        every { clazz.annotations() } returns mutableListOf(
            AnnotationNode("one"),
            AnnotationNode("three"),
        )
        val isKotlinClass = kotlinInterceptor.isKotlinClass(clazz)

        assertFalse(isKotlinClass)
    }

    @Test
    fun `isKotlinClass should return true if Lkotlin annotation provided`() {
        val kotlinInterceptor = KotlinInterceptor()
        val clazz = mockk<ClassTree>()
        every { clazz.annotations() } returns mutableListOf(
            AnnotationNode("one"),
            AnnotationNode("Lkotlin/Metadata;"),
            AnnotationNode("three"),
        )

        val isKotlinClass = kotlinInterceptor.isKotlinClass(clazz)

        assertTrue(isKotlinClass)
    }

    @Test
    fun `intercept should not call KotlinJunkMutationDetector if non-Kotlin class provided`() {
        val kotlinInterceptor = KotlinInterceptor()
        val clazz = mockk<ClassTree>()
        every { clazz.annotations() } returns mutableListOf(
            AnnotationNode("one"),
        )
        val mutations = emptyList<MutationDetails>()
        val mutater = mockk<Mutater>()
        mockkObject(KotlinJunkMutationDetector)
        every { KotlinJunkMutationDetector.isKotlinJunkMutation(clazz) } returns { false }

        kotlinInterceptor.begin(clazz)
        val mutationsResult = kotlinInterceptor.intercept(mutations, mutater)
        kotlinInterceptor.end()

        assertTrue(mutationsResult.isEmpty())
        verify(exactly = 0) { KotlinJunkMutationDetector.isKotlinJunkMutation(clazz) }
    }

    @Test
    fun `intercept should call KotlinJunkMutationDetector if Kotlin class provided`() {
        val kotlinInterceptor = KotlinInterceptor()
        val clazz = mockk<ClassTree>()
        every { clazz.annotations() } returns mutableListOf(
            AnnotationNode("Lkotlin/Metadata;"),
        )
        val mutations = emptyList<MutationDetails>()
        val mutater = mockk<Mutater>()
        mockkObject(KotlinJunkMutationDetector)
        every { KotlinJunkMutationDetector.isKotlinJunkMutation(clazz) } returns { false }

        kotlinInterceptor.begin(clazz)
        val mutationsResult = kotlinInterceptor.intercept(mutations, mutater)
        kotlinInterceptor.end()

        assertTrue(mutationsResult.isEmpty())
        verify(exactly = 1) { KotlinJunkMutationDetector.isKotlinJunkMutation(clazz) }
    }

    @Test
    fun `intercept should filter junk mutations`() {
        val kotlinInterceptor = KotlinInterceptor()
        val clazz = mockk<ClassTree>()
        every { clazz.annotations() } returns mutableListOf(
            AnnotationNode("Lkotlin/Metadata;"),
        )
        val mutationNorm = MutationDetails(
            MutationIdentifier(
                Location.location(
                    ClassName.fromString("com.Clazz1"),
                    "m1",
                    "md1"
                ),
                0, "0"
            ),
            "filename1.kt",
            "description1",
            1,
            1,
        )
        val mutationJunk = MutationDetails(
            MutationIdentifier(
                Location.location(
                    ClassName.fromString("com.Clazz2"),
                    "m2",
                    "md2"
                ),
                1, "1"
            ),
            "filenameJunk2.kt",
            "description2",
            2,
            2,
        )
        val mutations = listOf(mutationNorm, mutationJunk)
        val mutater = mockk<Mutater>()
        mockkObject(KotlinJunkMutationDetector)
        every { KotlinJunkMutationDetector.isKotlinJunkMutation(clazz) } returns {
            it.filename == "filenameJunk2.kt"
        }

        kotlinInterceptor.begin(clazz)
        val mutationsResult = kotlinInterceptor.intercept(mutations, mutater)
        kotlinInterceptor.end()

        assertEquals(listOf(mutationNorm), mutationsResult)
        verify(exactly = 1) { KotlinJunkMutationDetector.isKotlinJunkMutation(clazz) }
    }

    @Test
    fun `intercept should not call KotlinJunkMutationDetector after end`() {
        val kotlinInterceptor = KotlinInterceptor()
        val clazz = mockk<ClassTree>()
        every { clazz.annotations() } returns mutableListOf(
            AnnotationNode("Lkotlin/Metadata;"),
        )
        val mutations = emptyList<MutationDetails>()
        val mutater = mockk<Mutater>()
        mockkObject(KotlinJunkMutationDetector)
        every { KotlinJunkMutationDetector.isKotlinJunkMutation(clazz) } returns { false }

        kotlinInterceptor.begin(clazz)
        kotlinInterceptor.end()
        val mutationsResult = kotlinInterceptor.intercept(mutations, mutater)

        assertTrue(mutationsResult.isEmpty())
        verify(exactly = 0) { KotlinJunkMutationDetector.isKotlinJunkMutation(clazz) }
    }

    @Test
    fun `intercept should not call KotlinJunkMutationDetector without begin`() {
        val kotlinInterceptor = KotlinInterceptor()
        val clazz = mockk<ClassTree>()
        every { clazz.annotations() } returns mutableListOf(
            AnnotationNode("Lkotlin/Metadata;"),
        )
        val mutations = emptyList<MutationDetails>()
        val mutater = mockk<Mutater>()
        mockkObject(KotlinJunkMutationDetector)
        every { KotlinJunkMutationDetector.isKotlinJunkMutation(clazz) } returns { false }

        val mutationsResult = kotlinInterceptor.intercept(mutations, mutater)

        assertTrue(mutationsResult.isEmpty())
        verify(exactly = 0) { KotlinJunkMutationDetector.isKotlinJunkMutation(clazz) }
    }
}
