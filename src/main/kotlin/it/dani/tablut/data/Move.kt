package it.dani.tablut.data

import java.util.*
import kotlin.collections.ArrayList

open class Move(open val board: TablutBoard, open val move : Pair<Position,Position>, open val role : Role, open var precedent : Optional<Move>, open val eat : MutableList<Eat> = ArrayList()) {
    val following : MutableList<Move> = LinkedList()

    private var futureTableCache = Optional.empty<TablutBoard>()
    open var evaluationResult : Optional<Int> = Optional.empty()

    fun <T> evaluate(evaluator : (Move) -> T) : T {
        val result = evaluator(this)
        this.evaluationResult = Optional.of(result as Int)

        return result
    }

    override fun toString(): String {
        return "from: ${this.move.first} to ${this.move.second} eat: ${this.eat.size}"
    }

    fun futureTable() : TablutBoard {
        return if(this.futureTableCache.isPresent) {
            this.futureTableCache.get()
        } else {
            TablutBoard().apply {
                this.board = Array(9) { r ->
                    Array(9) { c -> this@Move.board.board[r][c] }
                }

                this.board[this@Move.move.second.row][this@Move.move.second.col] = this.board[this@Move.move.first.row][this@Move.move.first.col]
                this.board[this@Move.move.first.row][this@Move.move.first.col] = TablutBoardCellValue.EMPTY

                this@Move.eat.forEach {
                    this.board[it.position.row][it.position.col] = TablutBoardCellValue.EMPTY
                }

                this.turn = this@Move.role.opposite()
            }
        }.also { this.futureTableCache = Optional.of(it) }
    }
}