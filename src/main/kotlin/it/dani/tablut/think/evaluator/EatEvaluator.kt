package it.dani.tablut.think.evaluator

import it.dani.tablut.data.*
import it.dani.think.Evaluator
import kotlin.math.absoluteValue

class EatEvaluator : Evaluator {

    override fun evaluate(move : Move) : Int {
        return if(this.goalTest(move)) {
            GOAL_POINTS
        } else {
            this.encirclement(move)
        }
    }

    private fun goalTest(move: Move) : Boolean {
        return when(move.role) {
            Role.WHITE -> {
                move.board.isKingWin(move.move.second.row,move.move.second.col) &&
                        move.board.board[move.move.first.row][move.move.first.col] == TablutBoardCellValue.KING
            }
            Role.BLACK -> {
                var result = false
                move.eat.forEach {
                    result = result || it.isKing
                }
                result
            }
            else -> false
        }
    }

    private fun encirclement(move: Move) : Int {
        return when(move.role) {
            Role.WHITE -> {
                if(this.around(move.board,move.move.first) >= this.around(move.futureTable(),move.move.second)) {
                    LESS_AROUD_POINTS
                } else {
                    NO_LESS_AROUD_POINTS
                }
            }
            Role.BLACK -> {
                0
            }
            else -> {0}
        }
    }

    private fun around(board: TablutBoard, position: Position) : Int {
        var result = 0
        for(r in -1..1) {
            for(c in -1..1) {
                if(r.absoluteValue != c.absoluteValue &&
                    position.row+r in board.board.indices && position.col+c in board.board[position.row+r].indices &&
                    board.board[position.row][position.col] != board.board[position.row+r][position.col+c] &&
                    board.board[position.row+r][position.col+c] != TablutBoardCellValue.EMPTY) {
                    result++
                }
            }
        }
        return result
    }

    companion object {
        const val GOAL_POINTS = Int.MAX_VALUE
        const val NO_GOAL_POINTS = Int.MAX_VALUE
        const val LESS_AROUD_POINTS = 500
        const val NO_LESS_AROUD_POINTS = 500
    }
}

private enum class State(val dfl : Int, val func : (x : Int, y : Int) -> Int) {
    MIN(Int.MAX_VALUE,{ x,y -> if(x<y) x else y}), MAX(Int.MIN_VALUE,{ x,y -> if(x>y) x else y});

    fun opposite() : State {
        return when(this) {
            MIN -> MAX
            MAX -> MIN
        }
    }
}