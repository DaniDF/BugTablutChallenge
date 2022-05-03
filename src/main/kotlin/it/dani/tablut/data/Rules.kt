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
        val flagCastle = board.isCastle(newRow, newCol)
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

    private fun testEat(board: TablutBoard, move: Move): List<Eat> {
        val result: MutableList<Eat> = ArrayList()
        val newPosition = move.move.second

        testEatActive(board, newPosition).forEach {
            when (board.board[it.first.row][it.first.col]) {
                TablutBoardCellValue.KING -> result += Eat(
                    it.first,
                    true
                )   //TODO sistemare controllo di tre pedine attorno al re
                TablutBoardCellValue.WHITE, TablutBoardCellValue.BLACK -> Eat(it.first)
                else -> {}
            }
        }

        return result
    }

    private fun testEatActive(board: TablutBoard, position: Position): List<Pair<Position, Int>> {
        val result: MutableList<Pair<Position, Int>> = ArrayList()

        for (r in -1..1) {
            for (c in -1..1) {
                if ((r == 0 || c == 0) && (position.row + r) in 0..8 && (position.col + c) in 0..8 &&
                    board.board[position.row + r][position.col + c] != TablutBoardCellValue.EMPTY &&
                    board.board[position.row + r][position.col + c] != board.board[position.row][position.col]
                ) {
                    result += Position(position.row + r, position.col + c) to this.testEatPassive(
                        board,
                        Position(position.row + r, position.col + c)
                    )
                }
            }
        }

        return result
    }

    private fun testEatPassive(board: TablutBoard, position: Position): Int {
        var result = 0

        for (r in -1..1) {
            for (c in -1..1) {
                if ((r == 0 || c == 0) && (position.row + r) in 0..8 && (position.col + c) in 0..8 &&
                    board.board[position.row + r][position.col + c] != TablutBoardCellValue.EMPTY &&
                    board.board[position.row + r][position.col + c] != board.board[position.row][position.col]
                ) {
                    result++
                }
            }
        }

        return result
    }
}