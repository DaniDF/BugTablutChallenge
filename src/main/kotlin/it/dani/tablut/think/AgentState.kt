package it.dani.tablut.think

import it.dani.tablut.data.Move
import it.dani.tablut.data.Position
import it.dani.tablut.data.Role
import it.dani.tablut.data.TablutBoard
import java.util.*

data class AgentState(
    val role: Role,
    var board: TablutBoard,
    var moves: MutableList<Move> = LinkedList(),
    var toExpand: MutableList<Move> = LinkedList(),
    var currentMove : Move = Move(board,Position(3,5) to Position(3,5),role, Optional.empty())
)