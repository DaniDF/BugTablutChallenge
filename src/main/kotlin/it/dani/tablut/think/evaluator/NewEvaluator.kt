package it.dani.tablut.think.evaluator

import it.dani.tablut.data.Move
import it.dani.tablut.data.Position
import it.dani.tablut.think.BoardData
import it.dani.think.Evaluator

class NewEvaluator : Evaluator {
    override fun evaluate(move: Move): Int {
        return this.surrounding(move)
    }

    private fun surrounding(move: Move) : Int {
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
                            move.futureTable().board[oppositePosition.row][oppositePosition.col].opposite() == move.futureTable().board[pos.row][pos.col]
                        ) {
                            autokillSurround++
                        } else {
                            countSurroundBase++
                        }
                    }
                }
            }
        }

        return countSurroundBase + autokillSurround
    }
}