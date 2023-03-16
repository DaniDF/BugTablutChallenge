package it.dani.tablut.think

import it.dani.utils.Utils
import it.dani.tablut.data.Move
import it.dani.tablut.data.Rules
import it.dani.tablut.data.TablutBoard

class StateActionMemory {
    private val stateAction : MutableMap<TablutBoard,MutableMap<Move, Double>> = HashMap()
    private val rules : Rules = Rules()

    fun addMoves(moves : List<Pair<TablutBoard, Move>>) {
        moves.forEach { move ->
            this.stateAction.putIfAbsent(move.first, HashMap())
            this.stateAction[move.first]!!.putIfAbsent(move.second, 0.0)
        }
    }

    fun getGreedyMove(state : TablutBoard, epsilon : Double = 0.0) : Move {
        this.stateAction.putIfAbsent(state, HashMap())

        val result = if(this.stateAction[state]!!.isEmpty()) {
            this.rules.genFeasibleMoves(state).forEach { newMove ->
                this.stateAction[state]!![newMove] = 0.0
            }

            Utils.getRandomElement(this.stateAction[state]!!.keys)

        } else {
            val randVal = Utils.getDistributedRandomElement(listOf(0, 1), listOf(1-epsilon, epsilon)) == 1

            if(randVal) {
                this.rules.genFeasibleMoves(state).forEach { newMove ->
                    this.stateAction[state]!!.putIfAbsent(newMove,0.0)
                }

                Utils.getRandomElement(this.stateAction[state]!!.keys)
            } else {
                this.stateAction[state]!!.maxByOrNull { (_, value) -> value }!!.key
            }
        }

        return result
    }

    fun rewardAction(states : Collection<Pair<TablutBoard,Move>>, reward : Int, alpha : Double = 0.5) {
        states.forEach { this.rewardAction(it.first, it.second, reward, alpha) }
    }
    private fun rewardAction(state : TablutBoard, action : Move, reward : Int, alpha : Double = 0.5) {
        val qn = this.stateAction[state]!![action]!!
        this.stateAction[state]!![action] = qn + alpha * (reward - qn)
    }

}