package it.dani.think

import it.dani.tablut.data.Move

interface Evaluator {
    fun evaluate(move : Move) : Int
}