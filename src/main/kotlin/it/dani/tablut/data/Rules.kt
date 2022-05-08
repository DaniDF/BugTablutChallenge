package it.dani.tablut.data

import java.util.*
import kotlin.collections.ArrayList

class Rules {
    fun genFeasibleMoves(move: Move, position: Position): List<Move> {
        val result: MutableList<Move> = LinkedList()

        result += this.genFeasibleHorizontalMoves(move, position)
        result += this.genFeasibleVerticalMoves(move, position)

        return result
    }

    private fun genFeasibleHorizontalMoves(move: Move, position: Position): List<Move> {
        val result: MutableList<Move> = LinkedList()
        var count = 1
        var flagStop = false

        while (!flagStop) {
            if (position.col + count in 0..8) {
                flagStop = !this.verifyTableConstraint(
                    move.futureTable(),
                    position.row,
                    position.col,
                    position.row,
                    position.col + count
                )

                if (!flagStop) {
                    result += Move(
                        move.futureTable(),
                        position to Position(position.row, position.col + count),
                        move.futureTable().turn,
                        Optional.of(move)
                    ).apply {
                        eat.addAll(this@Rules.testEat(board, this))
                    }
                    count++
                }
            } else {
                flagStop = true
            }
        }

        flagStop = false
        count = 1

        while (!flagStop) {
            if (position.col - count in 0..8) {
                flagStop = !this.verifyTableConstraint(
                    move.futureTable(),
                    position.row,
                    position.col,
                    position.row,
                    position.col - count
                )

                if (!flagStop) {
                    result += Move(
                        move.futureTable(),
                        position to Position(position.row, position.col - count),
                        move.futureTable().turn,
                        Optional.of(move)
                    ).apply {
                        eat.addAll(this@Rules.testEat(board, this))
                    }
                    count++
                }
            } else {
                flagStop = true
            }
        }

        return result
    }

    private fun genFeasibleVerticalMoves(move: Move, position: Position): List<Move> {
        val result: MutableList<Move> = LinkedList()
        var count = 1
        var flagStop = false

        while (!flagStop) {
            if (position.row + count in 0..8) {
                flagStop = !this.verifyTableConstraint(
                    move.futureTable(),
                    position.row,
                    position.col,
                    position.row + count,
                    position.col
                )

                if (!flagStop) {
                    result += Move(
                        move.futureTable(),
                        position to Position(position.row + count, position.col),
                        move.futureTable().turn,
                        Optional.of(move)
                    ).apply {
                        eat.addAll(this@Rules.testEat(board, this))
                    }
                    count++
                }
            } else {
                flagStop = true
            }
        }

        flagStop = false
        count = 1

        while (!flagStop) {
            if (position.row - count in 0..8) {
                flagStop = !this.verifyTableConstraint(
                    move.futureTable(),
                    position.row,
                    position.col,
                    position.row - count,
                    position.col
                )

                if (!flagStop) {
                    result += Move(
                        move.futureTable(),
                        position to Position(position.row - count, position.col),
                        move.futureTable().turn,
                        Optional.of(move)
                    ).apply {
                        eat.addAll(this@Rules.testEat(board, this))
                    }
                    count++
                }
            } else {
                flagStop = true
            }
        }

        return result
    }

    private fun verifyTableConstraint(board: TablutBoard, oldRow: Int, oldCol: Int, newRow: Int, newCol: Int): Boolean {
        val flagEmpty = board.board[newRow][newCol] == TablutBoardCellValue.EMPTY
        val flagCastle = board.isThrone(newRow, newCol)
        val flagNewBlackStation = board.isBlackStation(newRow, newCol)
        val flagOldBlackStation = board.isBlackStation(oldRow, oldCol)

        var result = flagEmpty && !flagCastle && (flagNewBlackStation && flagOldBlackStation || !flagNewBlackStation)

        if (result && !flagNewBlackStation) {
            for (r in this.genRange(oldRow, newRow)) {
                for (c in this.genRange(oldCol, newCol)) {
                    if (r != oldRow || c != oldCol) {
                        result = result && !board.isBlackStation(r, c)
                    }
                }
            }
        }

        return result
    }

    private fun genRange(val1: Int, val2: Int): IntRange {
        return if (val1 < val2) {
            val1..val2
        } else {
            val2..val1
        }
    }

    private fun testEat(board: TablutBoard,move : Move) : List<Eat> {
        val result : MutableList<Eat> = ArrayList()
        val newPosition = move.move.second

        testEatActive(board,newPosition).forEach {
            when(board.board[it.first.row][it.first.col]) {
                TablutBoardCellValue.KING -> result += Eat(it.first,true)
                TablutBoardCellValue.WHITE, TablutBoardCellValue.BLACK -> Eat(it.first)
                else -> {}
            }
        }

        return result
    }

    private fun testEatActive(board: TablutBoard,position : Position) : List<Pair<Position,Int>> {
        val result : MutableList<Pair<Position,Int>> = ArrayList()

        if (board.board[position.row][position.col] == TablutBoardCellValue.WHITE || board.board[position.row][position.col] == TablutBoardCellValue.KING) { //sono bianco o re

            // mangio nero a destra
            if ((position.col+2) in 0..8 && board.board[position.row][position.col+1] == TablutBoardCellValue.BLACK
                && (board.board[position.row][position.col+2] == TablutBoardCellValue.WHITE || board.board[position.row][position.col+2] == TablutBoardCellValue.THRONE || board.board[position.row][position.col+2] == TablutBoardCellValue.KING
                        || board.isBlackStation(position.row,position.col+2))) {
                result += Position(position.row,position.col) to 1
            }

            // mangio nero a sinistra
            if ((position.col-2) in 0..8 && board.board[position.row][position.col-1] == TablutBoardCellValue.BLACK
                && (board.board[position.row][position.col-2] == TablutBoardCellValue.WHITE || board.board[position.row][position.col-2] == TablutBoardCellValue.THRONE || board.board[position.row][position.col-2] == TablutBoardCellValue.KING
                        || board.isBlackStation(position.row,position.col-2))) {
                result += Position(position.row,position.col) to 1
            }

            // mangio nero sotto
            if ((position.row+2) in 0..8 && board.board[position.row+1][position.col] == TablutBoardCellValue.BLACK
                && (board.board[position.row+2][position.col] == TablutBoardCellValue.WHITE || board.board[position.row+2][position.col] == TablutBoardCellValue.THRONE || board.board[position.row+2][position.col] == TablutBoardCellValue.KING
                        || board.isBlackStation(position.row+2,position.col))) {
                result += Position(position.row,position.col) to 1
            }

            // mangio nero sopra
            if ((position.row-2) in 0..8 && board.board[position.row-1][position.col] == TablutBoardCellValue.BLACK
                && (board.board[position.row-2][position.col] == TablutBoardCellValue.WHITE || board.board[position.row-2][position.col] == TablutBoardCellValue.THRONE || board.board[position.row-2][position.col] == TablutBoardCellValue.KING
                        || board.isBlackStation(position.row-2,position.col))) {
                result += Position(position.row,position.col) to 1
            }
        } //fine IF sono bianco

        if (board.board[position.row][position.col] == TablutBoardCellValue.BLACK) { //sono nero

            // mangio bianco a destra
            if ((position.col+2) in 0..8 && board.board[position.row][position.col+1] == TablutBoardCellValue.WHITE
                && (board.board[position.row][position.col+2] == TablutBoardCellValue.BLACK || board.board[position.row][position.col+2] == TablutBoardCellValue.THRONE
                        || board.isBlackStation(position.row,position.col+2))) {
                result += Position(position.row,position.col) to 1
            }

            // mangio bianco a sinistra
            else if ((position.col-2) in 0..8 && board.board[position.row][position.col-1] == TablutBoardCellValue.WHITE
                && (board.board[position.row][position.col-2] == TablutBoardCellValue.BLACK || board.board[position.row][position.col-2] == TablutBoardCellValue.THRONE
                        || board.isBlackStation(position.row,position.col-2))) {
                result += Position(position.row,position.col) to 1
            }

            // mangio bianco sotto
            else if ((position.row+2) in 0..8 && board.board[position.row+1][position.col] == TablutBoardCellValue.WHITE
                && (board.board[position.row+2][position.col] == TablutBoardCellValue.BLACK || board.board[position.row+2][position.col] == TablutBoardCellValue.THRONE
                        || board.isBlackStation(position.row+2,position.col))) {
                result += Position(position.row,position.col) to 1
            }

            // mangio bianco sopra
            else if ((position.row-2) in 0..8 && board.board[position.row-1][position.col] == TablutBoardCellValue.WHITE
                && (board.board[position.row-2][position.col] == TablutBoardCellValue.BLACK || board.board[position.row-2][position.col] == TablutBoardCellValue.THRONE
                        || board.isBlackStation(position.row-2,position.col))) {
                result += Position(position.row,position.col) to 1
            }


            //sono nero e circondo il re
            if ((position.row == 3 && position.col == 4) || (position.row == 4 && position.col == 3) || (position.row == 5 && position.col == 4) || (position.row == 4 && position.col == 5)) {
                    var count = 0
                    if (board.board[3][4] == TablutBoardCellValue.BLACK) count++
                    if (board.board[4][3] == TablutBoardCellValue.BLACK) count++
                    if (board.board[5][4] == TablutBoardCellValue.BLACK) count++
                    if (board.board[4][5] == TablutBoardCellValue.BLACK) count++
                    if (count == 3) {
                        result += Position(position.row,position.col) to 1
                    }
                }

                if ((position.row == 2 && position.col == 4) || (position.row == 3 && position.col == 5) || (position.row == 3 && position.col == 3) && board.board[3][4] == TablutBoardCellValue.KING) {
                    var count = 0
                    if (board.board[2][4]== TablutBoardCellValue.BLACK) count++
                    if (board.board[3][3]== TablutBoardCellValue.BLACK) count++
                    if (board.board[3][5]== TablutBoardCellValue.BLACK) count++
                    if (count == 2) {
                        result += Position(position.row,position.col) to 1
                    }
                }

            if ((position.row == 5 && position.col == 3) || (position.row == 4 && position.col == 2) || (position.row == 3 && position.col == 3) && board.board[4][3] == TablutBoardCellValue.KING) {
                var count = 0
                if (board.board[4][2]== TablutBoardCellValue.BLACK) count++
                if (board.board[5][3]== TablutBoardCellValue.BLACK) count++
                if (board.board[3][3]== TablutBoardCellValue.BLACK) count++
                if (count == 2) {
                    result += Position(position.row,position.col) to 1
                }
            }

            if ((position.row == 5 && position.col == 5) || (position.row == 5 && position.col == 3) || (position.row == 6 && position.col == 4) && board.board[5][4] == TablutBoardCellValue.KING) {
                var count = 0
                if (board.board[5][5]== TablutBoardCellValue.BLACK) count++
                if (board.board[5][3]== TablutBoardCellValue.BLACK) count++
                if (board.board[6][4]== TablutBoardCellValue.BLACK) count++
                if (count == 2) {
                    result += Position(position.row,position.col) to 1
                }
            }

            if ((position.row == 3 && position.col == 5) || (position.row == 4 && position.col == 6) || (position.row == 5 && position.col == 5) && board.board[4][5] == TablutBoardCellValue.KING) {
                var count = 0
                if (board.board[3][5]== TablutBoardCellValue.BLACK) count++
                if (board.board[4][6]== TablutBoardCellValue.BLACK) count++
                if (board.board[5][5]== TablutBoardCellValue.BLACK) count++
                if (count == 2) {
                    result += Position(position.row,position.col) to 1
                }
            }

        } //fine IF sono nero

        return result
    }
}