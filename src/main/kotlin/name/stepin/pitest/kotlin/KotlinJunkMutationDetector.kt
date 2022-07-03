package name.stepin.pitest.kotlin

import org.pitest.bytecode.analysis.ClassTree
import org.pitest.mutationtest.engine.MutationDetails

object KotlinJunkMutationDetector {
    fun isKotlinJunkMutation(currentClass: ClassTree): (MutationDetails) -> Boolean {
        return { mutationDetails: MutationDetails ->
            println(currentClass)
            println(mutationDetails)
            false
        }
    }
}
