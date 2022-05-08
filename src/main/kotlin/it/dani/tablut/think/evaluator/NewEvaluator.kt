package it.dani.tablut.think.evaluator

import it.dani.tablut.data.*
import it.dani.tablut.think.BoardData
import it.dani.think.Evaluator

class NewEvaluator : Evaluator {
    override fun evaluate(move: Move): Int {
        return when(move.role) {
            Role.WHITE -> { this.whiteHeuristic(move) }
            Role.BLACK -> { this.blackHeuristic(move) }
            else -> Int.MAX_VALUE
        }
    }

    private fun blackHeuristic(move: Move) : Int {
        val boardData = BoardData.extractData(move.futureTable())

        var result = this.surroundingForKillOrBeKilled(move) * BLACK_SURROUND

        result += (this.surroundingKing(move, boardData) * BLACK_SURROUNDING_KING_VALUE).coerceAtMost(Int.MAX_VALUE)
        val freeLines = this.freeLines(move,boardData)
        result += (freeLines.first * BLACK_FREE_LINES_KING_VALUE + freeLines.second * BLACK_FREE_LINES_VALUE).coerceAtMost(Int.MAX_VALUE)

        if(this.isGoingToKillKing(move)) {
            result = (result + BLACK_KING_EAT).coerceAtMost(Int.MAX_VALUE)
        }

        return result
    }

    private fun whiteHeuristic(move: Move) : Int {
        val boardData = BoardData.extractData(move.futureTable())

        var result = this.surroundingForKillOrBeKilled(move) * WHITE_SURROUND
        result += (this.surroundingKing(move, boardData) * WHITE_SURROUNDING_KING_VALUE).coerceAtMost(Int.MAX_VALUE)
        val freeLines = this.freeLines(move,boardData)
        result += (freeLines.first * WHITE_FREE_LINES_KING_VALUE + freeLines.second * WHITE_FREE_LINES_VALUE).coerceAtMost(Int.MAX_VALUE)

        if(this.isGoingToKillKing(move)) {
            result = (result + WHITE_KING_EAT).coerceAtMost(Int.MAX_VALUE)
        }

        return result
    }

    private fun surroundingKing(move: Move, boardData: BoardData) : Int {
        var result = 0
        val newPositions = listOf( -1 to 0, 1 to 0, 0 to -1, 0 to 1)

        newPositions.forEach { newPos ->
            val evaluatePosition = Position(boardData.kingPosition.row + newPos.first,boardData.kingPosition.col + newPos.second)

            if(evaluatePosition.row in 0 until 9 && evaluatePosition.col in 0 until 9) {
                if(move.futureTable().board[evaluatePosition.row][evaluatePosition.col] == TablutBoardCellValue.BLACK ||
                    evaluatePosition.row to evaluatePosition.col in TablutBoardCells.blackStation) {
                    result++
                }
            }
        }

        return result
    }

    private fun isGoingToKillKing(move : Move) : Boolean {
        var result = false

        move.eat.forEach {
            result = result || it.isKing
        }

        return result
    }

    private fun freeLines(move: Move, boardData: BoardData) : Pair<Int,Int> {
        val freeLinesKing = this.freeLines(move, listOf(boardData.kingPosition))

        val positions = if(move.role == Role.WHITE) {
            boardData.whitePositions + boardData.kingPosition
        } else {
            boardData.blackPositions
        }

        return freeLinesKing to this.freeLines(move, positions)
    }
    private fun freeLines(move: Move, positions : List<Position>) : Int {
        var result = 0

        positions.forEach { pos ->
            for(row in 0 until 9) {
                var empty = true

                for(col in 0 until 9) {
                    if(pos.row to pos.col != row to col) {
                        empty = empty && move.futureTable().board[row][col] == TablutBoardCellValue.EMPTY
                    } else if(empty) {
                        result++
                    } else {
                        empty = true
                    }
                }
            }

        }

        return result
    }

    private fun surroundingForKillOrBeKilled(move: Move) : Int {
        val positions = BoardData.getTurnPositions(BoardData.extractData(move.futureTable()),move.role)

        var countSurroundBase = 0
        var autokillSurround = 0

        val newPositions = listOf( -1 to 0, 1 to 0, 0 to -1, 0 to 1)
        val newOppositePositions = listOf( 1 to 0, -1 to 0, 0 to 1, 0 to -1)

        positions.forEach { pos ->
            newPositions.forEachIndexed { index, newPos ->
                val evaluatePosition = Position(pos.row + newPos.first,pos.col + newPos.second)
                val oppositePosition = Position(pos.row + newOppositePositions[index].first,pos.col + newOppositePositions[index].second)

                if(evaluatePosition.row in 0 until 9 && evaluatePosition.col in 0 until 9) { //Se posizione nuova in scacchiera
                    if(move.futureTable().board[evaluatePosition.row][evaluatePosition.col].opposite() == move.futureTable().board[pos.row][pos.col]) {
                        if(oppositePosition.row in 0 until 9 && oppositePosition.col in 0 until 9 && //Se posizione opposta e' in scacchiera
                            move.futureTable().board[oppositePosition.row][oppositePosition.col].opposite() == move.futureTable().board[pos.row][pos.col] ||
                            oppositePosition.row to oppositePosition.col in TablutBoardCells.blackStation
                        ) {
                            autokillSurround++
                        } else {
                            countSurroundBase++
                        }
                    }
                }
            }
        }

        return countSurroundBase
    }

    companion object {
        private const val WHITE_SURROUND = -25
        private const val WHITE_SURROUNDING_KING_VALUE = -5
        private const val WHITE_FREE_LINES_VALUE = 500
        private const val WHITE_FREE_LINES_KING_VALUE = Int.MAX_VALUE / 2
        private const val WHITE_KING_EAT = Int.MIN_VALUE

        private const val BLACK_SURROUND = -20
        private const val BLACK_SURROUNDING_KING_VALUE = 10
        private const val BLACK_FREE_LINES_VALUE = -500
        private const val BLACK_FREE_LINES_KING_VALUE = Int.MAX_VALUE / 2
        private const val BLACK_KING_EAT = Int.MAX_VALUE
    }
}