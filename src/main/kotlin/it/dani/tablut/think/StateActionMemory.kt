package it.dani.tablut.think

import it.dani.utils.Utils
import it.dani.tablut.data.Move
import it.dani.tablut.data.Position
import it.dani.tablut.data.TablutBoard
import java.util.HashSet
import java.util.TreeMap

class StateActionMemory {
    private val stateAction : MutableMap<TablutBoard,MutableSet<Pair<Move, Double>>> = TreeMap()

    fun getGreedyMove(state : TablutBoard, epsilon : Double = 0.0) : Move {
        if(!this.stateAction.keys.contains(state)) {
            this.stateAction += state to HashSet()
        }

        val result = if(this.stateAction[state]!!.isEmpty()) {
            Move(state, Position(0,0) to Position(0,0), state.turn) to 0.0 //TODO o scegli tra sole posizioni valide o le generi random

        } else {
            val randVal = Utils.getDistributedRandomElement(listOf(0, 1), listOf(1-epsilon, epsilon)) == 1

            if(randVal) {
                Utils.getRandomElement(this.stateAction[state]!!.toList())
            } else {
                this.stateAction[state]!!.maxByOrNull { move -> move.second }!!
            }
        }

        return result.first
    }

    fun rewardAction(states : Collection<Pair<TablutBoard,Move>>, reward : Int, alpha : Double = 0.5) {
        states.forEach { this.rewardAction(it.first, it.second, reward, alpha) }
    }
    private fun rewardAction(state : TablutBoard, action : Move, reward : Int, alpha : Double = 0.5) {
        this.stateAction[state]?.find { x -> x.first == action }?.let {
            val qn = it.second
            this.stateAction[state]?.add(it.first to qn + alpha * (reward - qn))
        }
    }

}