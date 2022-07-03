package name.stepin.pitest.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pitest.classpath.ClassloaderByteArraySource
import org.pitest.coverage.NoCoverage
import org.pitest.mutationtest.build.InterceptorParameters
import org.pitest.mutationtest.config.ReportOptions
import org.pitest.plugin.FeatureSetting
import org.pitest.plugin.ToggleStatus

internal class KotlinInterceptorFactoryTest {

    @Test
    fun `createInterceptor should create KotlinInterceptor`() {
        val kotlinInterceptorFactory = KotlinInterceptorFactory()
        val interceptorParameters = InterceptorParameters(
            FeatureSetting("feature1", ToggleStatus.ACTIVATE, emptyMap()),
            ReportOptions(),
            NoCoverage(),
            ClassloaderByteArraySource(this.javaClass.classLoader)
        )

        val interceptor = kotlinInterceptorFactory.createInterceptor(interceptorParameters)

        assertEquals(KotlinInterceptor::class, interceptor::class)
    }

    @Test
    fun `description should be about Kotlin`() {
        val kotlinInterceptorFactory = KotlinInterceptorFactory()

        val description = kotlinInterceptorFactory.description()

        assertTrue(description.contains("kotlin", ignoreCase = true))
    }

    @Test
    fun `provides should be about Kotlin and on by default`() {
        val kotlinInterceptorFactory = KotlinInterceptorFactory()

        val feature = kotlinInterceptorFactory.provides()

        assertTrue(feature.name().contains("kotlin", ignoreCase = true))
        assertTrue(feature.description().contains("kotlin", ignoreCase = true))
        assertTrue(feature.isOnByDefault)
    }
}
