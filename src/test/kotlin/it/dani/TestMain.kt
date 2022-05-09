package it.dani

import com.google.gson.Gson
import it.dani.tablut.data.TablutBoard

const val string = "{board:[[EMPTY,BLACK,EMPTY,EMPTY,EMPTY,BLACK,EMPTY,EMPTY,EMPTY],[BLACK,EMPTY,BLACK,EMPTY,BLACK,EMPTY,EMPTY,EMPTY,EMPTY],[EMPTY,BLACK,EMPTY,EMPTY,WHITE,EMPTY,EMPTY,WHITE,EMPTY],[EMPTY,EMPTY,EMPTY,EMPTY,WHITE,EMPTY,BLACK,EMPTY,EMPTY],[BLACK,EMPTY,EMPTY,WHITE,THRONE,WHITE,EMPTY,EMPTY,BLACK],[BLACK,EMPTY,EMPTY,EMPTY,EMPTY,WHITE,EMPTY,BLACK,BLACK],[EMPTY,EMPTY,WHITE,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY],[EMPTY,EMPTY,WHITE,EMPTY,EMPTY,EMPTY,EMPTY,BLACK,EMPTY],[EMPTY,KING,EMPTY,EMPTY,BLACK,BLACK,EMPTY,EMPTY,EMPTY]],turn:WHITEWIN}"

fun main() {
    val board = Gson().fromJson(string, TablutBoard::class.java)
    println(board)
}