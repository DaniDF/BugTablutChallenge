package it.dani.time

import java.util.LinkedList

interface Timer {
    var time : Long
    val onTikListeners : MutableList<() -> Any>
    fun start()
    fun stop()
}