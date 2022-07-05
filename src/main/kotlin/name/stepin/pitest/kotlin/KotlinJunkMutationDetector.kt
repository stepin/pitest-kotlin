package name.stepin.pitest.kotlin

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodInsnNode
import org.pitest.bytecode.analysis.ClassTree
import org.pitest.bytecode.analysis.InstructionMatchers
import org.pitest.bytecode.analysis.MethodMatchers
import org.pitest.classinfo.ClassName
import org.pitest.mutationtest.engine.MutationDetails
import org.pitest.sequence.Context
import org.pitest.sequence.Match
import org.pitest.sequence.QueryParams
import org.pitest.sequence.QueryStart
import org.pitest.sequence.SequenceQuery
import org.pitest.sequence.Slot
import java.util.regex.Pattern

object KotlinJunkMutationDetector {

    private const val DEBUG = false
    private val MUTATED_INSTRUCTION = Slot.create(AbstractInsnNode::class.java)
    private val FOUND = Slot.create(Boolean::class.java)
    private val KOTLIN_JUNK = QueryStart
        .match(Match.never<AbstractInsnNode>())
        .zeroOrMore(QueryStart.match(InstructionMatchers.anyInstruction()))
        .or(destructuringCall())
        .or(nullCast())
        .or(safeNullCallOrElvis())
        .or(safeCast())
        .then(containMutation(FOUND))
        .zeroOrMore(QueryStart.match(InstructionMatchers.anyInstruction()))
        .compile(
            QueryParams.params(AbstractInsnNode::class.java)
                .withIgnores(InstructionMatchers.notAnInstruction())
                .withDebug(DEBUG)
        )

    fun isKotlinJunkMutation(currentClass: ClassTree): (MutationDetails) -> Boolean {
        return { a: MutationDetails ->
            val instruction = a.instructionIndex
            val method = currentClass.methods().stream()
                .filter(MethodMatchers.forLocation(a.id.location))
                .findFirst()
                .get()
            val mutatedInstruction = method.instruction(instruction)
            val context = Context.start(method.instructions(), DEBUG)
//            val context = Context.start(DEBUG)
            context.store(MUTATED_INSTRUCTION.write(), mutatedInstruction)
            KOTLIN_JUNK.matches(method.instructions(), context)
        }
    }

    private fun nullCast(): SequenceQuery<AbstractInsnNode> {
        return QueryStart
            .any(AbstractInsnNode::class.java)
            .then(InstructionMatchers.opCode(Opcodes.IFNONNULL).and(mutationPoint()))
            .then(
                InstructionMatchers.methodCallTo(
                    ClassName.fromString("kotlin/jvm/internal/Intrinsics"),
                    "throwNpe"
                ).and(mutationPoint())
            )
    }

    private fun safeCast(): SequenceQuery<AbstractInsnNode> {
        val nullJump = Slot.create(LabelNode::class.java)
        return QueryStart
            .any(AbstractInsnNode::class.java)
            .then(InstructionMatchers.opCode(Opcodes.INSTANCEOF).and(mutationPoint()))
            .then(
                InstructionMatchers.opCode(Opcodes.IFNE)
                    .and(InstructionMatchers.jumpsTo(nullJump.write()).and(mutationPoint()))
            )
            .then(InstructionMatchers.opCode(Opcodes.POP))
            .then(InstructionMatchers.opCode(Opcodes.ACONST_NULL))
            .then(InstructionMatchers.labelNode(nullJump.read()))
    }

    private fun destructuringCall(): SequenceQuery<AbstractInsnNode> {
        return QueryStart
            .any(AbstractInsnNode::class.java)
            .then(aComponentNCall().and(mutationPoint()))
    }

    private fun safeNullCallOrElvis(): SequenceQuery<AbstractInsnNode> {
        val nullJump = Slot.create(LabelNode::class.java)
        return QueryStart
            .any(AbstractInsnNode::class.java)
            .then(
                InstructionMatchers.opCode(Opcodes.IFNULL)
                    .and(InstructionMatchers.jumpsTo(nullJump.write())).and(mutationPoint())
            )
            .oneOrMore(QueryStart.match(InstructionMatchers.anyInstruction()))
            .then(InstructionMatchers.opCode(Opcodes.GOTO))
            .then(InstructionMatchers.labelNode(nullJump.read()))
            .then(InstructionMatchers.opCode(Opcodes.POP))
            .then(aConstant().and(mutationPoint()))
    }

    private fun aConstant(): Match<AbstractInsnNode> {
        return InstructionMatchers.opCode(Opcodes.ACONST_NULL).or(
            InstructionMatchers.anIntegerConstant().or(InstructionMatchers.opCode(Opcodes.SIPUSH))
                .or(InstructionMatchers.opCode(Opcodes.LDC))
        )
    }

    private fun aComponentNCall(): Match<AbstractInsnNode> {
        val componentPattern = Pattern.compile("component\\d")
        return object : Match<AbstractInsnNode> {
            override fun test(
                c: Context<AbstractInsnNode>,
                abstractInsnNode: AbstractInsnNode?
            ): Boolean {
                return if (abstractInsnNode is MethodInsnNode) {
                    val call = abstractInsnNode
                    isDestructuringCall(call) && takesNoArgs(call)
                } else false
            }

            private fun isDestructuringCall(call: MethodInsnNode): Boolean {
                return takesNoArgs(call) && isComponentNCall(call)
            }

            private fun isComponentNCall(call: MethodInsnNode): Boolean {
                return componentPattern.matcher(call.name).matches()
            }

            private fun takesNoArgs(call: MethodInsnNode): Boolean {
                return call.desc.startsWith("()")
            }
        }
    }

    private fun mutationPoint(): Match<AbstractInsnNode> {
        return InstructionMatchers.recordTarget(MUTATED_INSTRUCTION.read(), FOUND.write())
    }

    private fun containMutation(found: Slot<Boolean>): Match<AbstractInsnNode> {
        return Match { context: Context<AbstractInsnNode>, _: AbstractInsnNode? ->
            context.retrieve(found.read()).isPresent
        }
    }
}
