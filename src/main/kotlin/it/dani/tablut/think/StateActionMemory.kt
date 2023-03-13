package it.dani.tablut.think

import it.dani.utils.Utils
import it.dani.tablut.data.Move
import it.dani.tablut.data.Position
import it.dani.tablut.data.TablutBoard
import java.util.Random

class StateActionMemory {
    private val stateAction : MutableMap<TablutBoard,MutableMap<Move, Double>> = HashMap()

    fun getGreedyMove(state : TablutBoard, epsilon : Double = 0.0) : Move {
        this.stateAction.putIfAbsent(state, HashMap())

        val result = if(this.stateAction[state]!!.isEmpty()) {
            val rand = Random()
            val posFrom = Position(rand.nextInt(9), rand.nextInt(9))
            val posTo = Position(rand.nextInt(9), rand.nextInt(9))

            val move = Move(state, posFrom to posTo, state.turn)

            this.stateAction[state]!![move] = 0.0

            move

        } else {
            val randVal = Utils.getDistributedRandomElement(listOf(0, 1), listOf(1-epsilon, epsilon)) == 1

            if(randVal) {
                val rand = Random()
                val posFrom = Position(rand.nextInt(9), rand.nextInt(9))
                val posTo = Position(rand.nextInt(9), rand.nextInt(9))

                val move = Move(state, posFrom to posTo, state.turn)
                this.stateAction[state]!!.putIfAbsent(move,0.0)

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