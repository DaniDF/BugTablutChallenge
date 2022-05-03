package it.myunibo

import java.io.Serializable
import java.security.InvalidParameterException

/**
 * this class represents an action of a player
 *
 * @author A.Piretti
 */
class Action(from: String, to: String, t: State.Turn?) : Serializable {
    var from: String? = null
    var to: String? = null
    var turn: State.Turn? = null

    init {
        if (from.length != 2 || to.length != 2) {
            throw InvalidParameterException("the FROM and the TO string must have length=2")
        } else {
            this.from = from
            this.to = to
            turn = t
        }
    }

    override fun toString(): String {
        return "Turn: $turn Pawn from $from to $to"
    }

    /**
     * @return means the index of the column where the pawn is moved from
     */
    val columnFrom: Int
        get() = (from!![0].lowercaseChar() - 97).code

    /**
     * @return means the index of the column where the pawn is moved to
     */
    val columnTo: Int
        get() = (to!![0].lowercaseChar() - 97).code

    /**
     * @return means the index of the row where the pawn is moved from
     */
    val rowFrom: Int
        get() = (from!![1].toString() + "").toInt() - 1

    /**
     * @return means the index of the row where the pawn is moved to
     */
    val rowTo: Int
        get() = (to!![1].toString() + "").toInt() - 1

    companion object {
        private const val serialVersionUID = 1L
    }
}