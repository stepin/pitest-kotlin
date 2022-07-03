package name.stepin.pitest.kotlin

import org.pitest.bytecode.analysis.ClassTree
import org.pitest.mutationtest.build.InterceptorType
import org.pitest.mutationtest.build.MutationInterceptor
import org.pitest.mutationtest.engine.Mutater
import org.pitest.mutationtest.engine.MutationDetails

class KotlinInterceptor : MutationInterceptor {
    private var currentKotlinClass: ClassTree? = null

    override fun type(): InterceptorType {
        return InterceptorType.FILTER
    }

    override fun begin(clazz: ClassTree) {
        if (isKotlinClass(clazz)) {
            currentKotlinClass = clazz
        }
    }

    internal fun isKotlinClass(clazz: ClassTree) =
        clazz.annotations().any { it.desc == "Lkotlin/Metadata;" }

    override fun intercept(
        mutations: Collection<MutationDetails>,
        m: Mutater
    ): Collection<MutationDetails> {
        val clazz = currentKotlinClass
            ?: return mutations
        val isKotlinJunkMutation = KotlinJunkMutationDetector.isKotlinJunkMutation(clazz)
        return mutations.filterNot(isKotlinJunkMutation)
    }

    override fun end() {
        currentKotlinClass = null
    }
}
