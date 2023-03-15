package it.dani.trainer

import it.dani.tablut.data.Move
import it.dani.tablut.data.Role

class TrainGame {
    val gameMoves : MutableList<Move> = ArrayList()
    lateinit var gameResult: Role
}