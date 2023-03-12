package it.dani.tablut.data

open class Move(open val board: TablutBoard, open val move : Pair<Position,Position>, open val role : Role) {
    override fun toString(): String {
        return "from: ${this.move.first} to ${this.move.second}"
    }

    fun futureTable() : TablutBoard {
        return TablutBoard().apply {
            this.board = Array(9) { r ->
                Array(9) { c -> this@Move.board.board[r][c] }
            }

            this.board[this@Move.move.second.row][this@Move.move.second.col] = this.board[this@Move.move.first.row][this@Move.move.first.col]
            this.board[this@Move.move.first.row][this@Move.move.first.col] = TablutBoardCellValue.EMPTY

            this.turn = this@Move.role.opposite()
        }
    }
}