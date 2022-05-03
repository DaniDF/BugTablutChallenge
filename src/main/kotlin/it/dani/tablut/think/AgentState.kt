package it.dani.tablut.think

import it.dani.tablut.data.Move
import it.dani.tablut.data.Role
import it.dani.tablut.data.TablutBoard
import java.util.LinkedList

data class AgentState(val role: Role, var board : TablutBoard, var moves : MutableList<Move> = LinkedList(), var toExpand : MutableList<Move> = LinkedList())