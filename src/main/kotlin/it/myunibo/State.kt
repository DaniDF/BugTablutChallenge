package it.myunibo

import java.lang.StringBuffer
import java.lang.NoSuchMethodException
import java.lang.SecurityException
import java.lang.InstantiationException
import java.lang.IllegalAccessException
import java.lang.IllegalArgumentException
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException

/**
 * Abstract class for a State of a game We have a representation of the board
 * and the turn
 *
 * @author Andrea Piretti
 */
abstract class State {
    /**
     * Turn represent the player that has to move or the end of the game(A win
     * by a player or a draw)
     *
     * @author A.Piretti
     */
    enum class Turn(private val turn: String) {
        WHITE("W"), BLACK("B"), WHITEWIN("WW"), BLACKWIN("BW"), DRAW("D");

        fun equalsTurn(otherName: String?): Boolean {
            return if (otherName == null) false else turn == otherName
        }

        override fun toString(): String {
            return turn
        }
    }

    /**
     *
     * Pawn represents the content of a box in the board
     *
     * @author A.Piretti
     */
    enum class Pawn(private val pawn: String) {
        EMPTY("O"), WHITE("W"), BLACK("B"), THRONE("T"), KING("K");

        fun equalsPawn(otherPawn: String?): Boolean {
            return if (otherPawn == null) false else pawn == otherPawn
        }

        override fun toString(): String {
            return pawn
        }
    }

    var board: Array<Array<Pawn>>? = null
    var turn: Turn? = null
    fun boardString(): String {
        val result = StringBuffer()
        for (i in board!!.indices) {
            for (j in board!!.indices) {
                result.append(board!![i][j].toString())
                if (j == 8) {
                    result.append("\n")
                }
            }
        }
        return result.toString()
    }

    override fun toString(): String {
        val result = StringBuffer()

        // board
        result.append("")
        result.append(boardString())
        result.append("-")
        result.append("\n")

        // TURNO
        result.append(turn.toString())
        return result.toString()
    }

    fun toLinearString(): String {
        val result = StringBuffer()

        // board
        result.append("")
        result.append(boardString().replace("\n", ""))
        result.append(turn.toString())
        return result.toString()
    }

    /**
     * this function tells the pawn inside a specific box on the board
     *
     * @param row
     * represents the row of the specific box
     * @param column
     * represents the column of the specific box
     * @return is the pawn of the box
     */
    fun getPawn(row: Int, column: Int): Pawn {
        return board!![row][column]
    }

    /**
     * this function remove a specified pawn from the board
     *
     * @param row
     * represents the row of the specific box
     * @param column
     * represents the column of the specific box
     */
    fun removePawn(row: Int, column: Int) {
        board!![row][column] = Pawn.EMPTY
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null) return false
        if (this.javaClass != obj.javaClass) return false
        val other = obj as State
        if (board == null) {
            if (other.board != null) return false
        } else {
            if (other.board == null) return false
            if (board!!.size != other.board!!.size) return false
            if (board!![0].size != other.board!![0].size) return false
            for (i in other.board!!.indices) for (j in other.board!![i].indices) if (board!![i][j] != other.board!![i][j]) return false
        }
        return if (turn != other.turn) false else true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (board == null) 0 else board.hashCode()
        result = prime * result + if (turn == null) 0 else turn.hashCode()
        return result
    }

    fun getBox(row: Int, column: Int): String {
        val ret: String
        val col = (column + 97).toChar()
        ret = col.toString() + "" + (row + 1)
        return ret
    }

    fun clone(): State {
        val stateclass: Class<out State> = this.javaClass
        var cons: Constructor<out State>? = null
        var result: State? = null
        try {
            cons = stateclass.getConstructor(stateclass)
            result = cons.newInstance(*arrayOfNulls(0))
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        val oldboard = board
        val newboard = result!!.board
        for (i in board!!.indices) {
            for (j in board!![i].indices) {
                newboard!![i][j] = oldboard!![i][j]
            }
        }
        result.board = newboard
        result.turn = turn
        return result
    }

    /**
     * Counts the number of checkers of a specific color on the board. Note: the king is not taken into account for white, it must be checked separately
     * @param color The color of the checker that will be counted. It is possible also to use EMPTY to count empty cells.
     * @return The number of cells of the board that contains a checker of that color.
     */
    fun getNumberOf(color: Pawn): Int {
        var count = 0
        for (i in board!!.indices) {
            for (j in board!![i].indices) {
                if (board!![i][j] == color) count++
            }
        }
        return count
    }
}