package name.stepin.pitest.kotlin

data class Result(val a: Int, val b: Int)

class Destructure2 {
    fun foo(r: Result) {
        val (a, b) = r
        println(a + b)
    }
}
