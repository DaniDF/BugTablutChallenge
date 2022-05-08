package it.dani.tablut.think

import it.dani.tablut.data.*
import java.util.*

class AgentState(
    val role: Role,
    private var board: TablutBoard,
    var moves: MutableList<Move> = LinkedList(),
    var toExpand: MutableList<Move> = LinkedList(),
    var currentMove : Move = FakeMove(board,Position(3,5) to Position(3,5),role.opposite(), Optional.empty())
) {
    fun updateBoard(board: TablutBoard) {
        this.board = board
        this.currentMove = Move(board,this.currentMove.move,this.currentMove.role,this.currentMove.precedent,this.currentMove.eat)
    }
}