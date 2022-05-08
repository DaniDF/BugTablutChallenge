package it.dani

import java.util.*

class MinMaxFuncTest {

    fun testMax() {
        /*
        val obj2 = MyObject(Optional.of(1))
        val obj3 = Optional.empty<MyObject>()
        val obj4 = Optional.of(MyObject(Optional.empty()))
        val obj5 = Optional.of(MyObject(Optional.of(2)))

        println("Result4 = ${max(obj2,obj3) { o -> o.myVal}}")
        println("Result5 = ${max(obj2,obj4) { o -> o.myVal}}")
        println("Result6 = ${max(obj2,obj5) { o -> o.myVal}}")
        */

        println("Result1 0 ${listOf(MyObject(Optional.of(1)), MyObject(Optional.empty()), MyObject(Optional.of(3))).minOfOrNull { o -> if(o.myVal.isPresent) {
            o.myVal.get()
        } else {
            Int.MAX_VALUE
        } }}")
    }


    companion object {
        private fun <T, R : Comparable<R>> max(a : T, b : Optional<T>, selector : (T) -> Optional<R>) : T {
            return if(b.isEmpty) {
                a
            } else if(b.isPresent && selector(b.get()).isEmpty) {
                a
            } else {
                if(maxOf(selector(a).get(),selector(b.get()).get()) == selector(a).get()) {
                    a
                } else {
                    b.get()
                }
            }
        }

        private fun <T, R : Comparable<R>> min(a : T, b : Optional<T>, selector : (T) -> Optional<R>) : T {
            return if(b.isEmpty) {
                a
            } else if(b.isPresent && selector(b.get()).isEmpty) {
                a
            } else {
                if(minOf(selector(a).get(),selector(b.get()).get()) == selector(a).get()) {
                    a
                } else {
                    b.get()
                }
            }
        }

        private fun <T,R : Comparable<R>> List<T>.maxOfOrNull(selector : (T) -> R) : T? {
            var result : T? = null

            this.forEach {
                result = if(result == null) {
                    it
                } else if(maxOf(selector(it),selector(result!!)) == selector(result!!)) {
                    result
                } else {
                    it
                }
            }

            return result
        }

        private fun <T,R : Comparable<R>> List<T>.minOfOrNull(selector : (T) -> R) : T? {
            var result : T? = null

            this.forEach {
                result = if(result == null) {
                    it
                } else if(minOf(selector(it),selector(result!!)) == selector(result!!)) {
                    result
                } else {
                    it
                }
            }

            return result
        }
    }
}

data class MyObject(val myVal : Optional<Int>)

fun main() {
    MinMaxFuncTest().also {
        it.testMax()
    }
}