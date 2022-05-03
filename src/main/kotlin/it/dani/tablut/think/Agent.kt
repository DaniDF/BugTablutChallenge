package it.dani.tablut.think

import it.dani.tablut.data.*
import it.dani.tablut.think.evaluator.NewEvaluator
import it.dani.think.Evaluator
import java.util.LinkedList
import java.util.Optional
import java.util.concurrent.Semaphore
import kotlin.collections.ArrayList

class Agent(val state: AgentState) : it.dani.think.Thinker {

    var evaluator : Evaluator = NewEvaluator()

    private var continueThink = true
    private val mutex = Semaphore(1)

    private var daemon = Optional.empty<Thread>()

    private val rules = Rules()

    override fun startThink() {
        if(this.daemon.isPresent) {
            this.daemon.get().interrupt()
        }

        this.mutex.acquire()
        this.continueThink = true
        this.mutex.release()

        this.daemon = Optional.of(Thread {
            this.findSolution(this.state) { l ->
                this.mutex.acquire()
                this.state.moves.addAll(l)
                this.state.moves.sortByDescending { it.evaluationResult }
                this.state.toExpand.addAll(l)
                //this.state.toExpand.sortByDescending { it.evaluate(this.evaluator::evaluate) }
                this.mutex.release()
            }

            var countExpanded = 0

            this.mutex.acquire()
            while(this.continueThink && this.state.toExpand.isNotEmpty() && countExpanded < 10000) {
                this.mutex.release()
                val nextMove = this.state.toExpand.first()

                val newRole = when(nextMove.role) {
                    Role.WHITE -> Role.BLACK
                    else -> Role.WHITE
                }
                val newBoard = nextMove.futureTable().apply { turn = newRole }
                val newState = AgentState(newRole,newBoard,LinkedList())

                this.state.toExpand.removeAt(0)

                this.findSolution(newState) { l ->
                    countExpanded += l.size
                    nextMove.following.addAll(l)
                    this.state.toExpand.addAll(l)
                }

                this.mutex.acquire()
            }

            this.mutex.release()
        })
        this.daemon.get().start()
    }

    override fun stopThink() {
        this.mutex.acquire()
        this.continueThink = false
        this.mutex.release()

        if(this.daemon.isPresent && !this.daemon.get().isInterrupted){
            this.daemon.get().interrupt()
        }
    }

    private fun findSolution(state: AgentState, onFinished : (List<Move>) -> Any) {
        val boardData = BoardData.extractData(state.board)

        var count = 0
        val positions = BoardData.getTurnPositions(boardData,state.role)

        val futureMoves : MutableList<Move> = ArrayList()

        var flagStop = false
        this.mutex.acquire()
        while(!flagStop && this.continueThink) {
            this.mutex.release()

            if(count in positions.indices && state.moves.size < 200) {
                val partialFutureMoves = this.rules.genFeasiblePositions(state.board,positions[count])
                futureMoves += partialFutureMoves
                count++

            } else {
                var deep = 0

                var fatherCounter : (List<Move>) -> Unit = {}
                fatherCounter = { list ->
                    list.firstOrNull()?.let {
                        if(deep < DEPTH_LIMIT) {
                            deep++
                            //fatherCounter(it.)
                        }
                    }
                }

                /*
                non hai bisogno di valutare tutte le mossse su quel libello
                min max mischiato con alfa beta
                 */



                futureMoves.forEach { it.evaluate(this.evaluator::evaluate) }
                onFinished(futureMoves)

                flagStop = true
            }

            this.mutex.acquire()
        }
        this.mutex.release()
    }

    companion object {
        private const val DEPTH_LIMIT = 4
    }
}

class BoardData {
    val whitePositions : MutableList<Position> = ArrayList()
    val blackPositions : MutableList<Position> = ArrayList()
    var kingPosition : Position = Position(0,0)

    companion object {
        fun extractData(board: TablutBoard) : BoardData {
            val result = BoardData()

            board.board.forEachIndexed { indexR, valR ->
                valR.forEachIndexed { indexC, valC ->
                    when(valC) {
                        TablutBoardCellValue.WHITE -> result.whitePositions += Position(indexR,indexC)
                        TablutBoardCellValue.BLACK -> result.blackPositions += Position(indexR,indexC)
                        TablutBoardCellValue.KING -> result.kingPosition = Position(indexR,indexC)
                        else -> {}
                    }
                }
            }

            return result
        }

        fun getTurnPositions(boardData: BoardData,role: Role): List<Position> {
            return when(TablutBoardCellValue.valueOf(role.toString())) {
                TablutBoardCellValue.WHITE -> boardData.whitePositions.apply { add(boardData.kingPosition) }
                TablutBoardCellValue.BLACK -> boardData.blackPositions
                else -> boardData.blackPositions
            }
        }
    }
}