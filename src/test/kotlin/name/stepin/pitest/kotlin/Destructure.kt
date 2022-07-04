package com.example

data class Result(val a : Int, val b : Int)

class Destructure {
  fun foo(r: Result) {
    val (a, b) = r;
    println(a + b);
  }
}
