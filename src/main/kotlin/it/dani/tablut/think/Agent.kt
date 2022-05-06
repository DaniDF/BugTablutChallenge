package it.dani.tablut.think

import it.dani.tablut.data.*
import it.dani.tablut.think.evaluator.NewEvaluator
import it.dani.think.Evaluator
import java.util.Optional
import java.util.concurrent.Semaphore
import kotlin.collections.ArrayList

class Agent(val state: AgentState) : it.dani.think.Thinker {

    var evaluator : Evaluator = NewEvaluator()

    private var continueThink = true
    private val mutex = Semaphore(1)

    private var daemon = Optional.empty<Thread>()

    private val rules = Rules()

    private val onFindSolution : (List<Move>, (List<Move>) -> Any) -> Any = { l,f ->
        this.mutex.acquire()
        f(l)

        var deep = 0

        var fatherCounter: (Move) -> Unit = {}
        fatherCounter = {
            deep++
            if (it.precedent.isPresent) {
                fatherCounter(it.precedent.get())
            }
        }

        l.firstOrNull()?.let { fatherCounter(it) }

        var toExpand = l

        if (deep >= DEPTH_LIMIT) {
            l.forEach { it.evaluate(this.evaluator::evaluate) }


            val compareFunc: (Role) -> (List<Move>,Int?) -> Pair<Move,Int>? = {
                when (it) {
                    this.state.role -> {
                        { list, threshold ->
                            if(threshold != null) {
                                list.maxOfOrNull(threshold) { move -> move.evaluationResult }
                            } else {
                                list.maxOfOrNull(Int.MAX_VALUE) { move -> move.evaluationResult }
                            }
                        }
                    }   //MAX
                    else -> {
                        { list, threshold ->
                            if(threshold != null) {
                                list.minOfOrNull(threshold) { move -> move.evaluationResult }
                            } else {
                                list.minOfOrNull(Int.MAX_VALUE) { move -> move.evaluationResult }
                            }
                        }
                    }   //MIN
                }
            }

            var writeEvaluations: (Move) -> Unit = {}
            writeEvaluations = {m ->
                val precedent : Move? = if(m.precedent.isPresent) {
                    m.precedent.get()
                } else {
                    null
                }
                compareFunc(m.role)(m.following,precedent?.evaluationResult)?.let {
                    m.evaluationResult = it.second
                    toExpand = listOf(it.first)
                }
                if (m.precedent.isPresent) {
                    writeEvaluations(m.precedent.get())
                }
            }

            writeEvaluations(this.state.currentMove)

        }

        this.state.moves.sortByDescending { it.evaluationResult }
        this.state.toExpand.addAll(toExpand)
        this.mutex.release()
    }

    override fun startThink() {
        if(this.daemon.isPresent) {
            this.daemon.get().interrupt()
        }

        this.mutex.acquire()
        this.continueThink = true
        this.mutex.release()

        this.daemon = Optional.of(Thread {
            this.findSolution(this.state) {
                this.onFindSolution(it) {l ->
                    this.state.moves.addAll(l)
                }
            }

            var countExpanded = 0

            this.mutex.acquire()
            while(this.continueThink && this.state.toExpand.isNotEmpty() && countExpanded < 10000) {
                this.mutex.release()

                this.state.currentMove = this.state.toExpand.first()
                this.state.toExpand.removeAt(0)

                this.findSolution(this.state) {
                    this.onFindSolution(it) {l ->
                        countExpanded += l.size
                        l.forEach { m -> m.precedent = Optional.of(this.state.currentMove) }
                        this.state.currentMove.following.addAll(l)
                    }
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
        val boardData = BoardData.extractData(state.currentMove.board)

        var count = 0
        val positions = BoardData.getTurnPositions(boardData,state.currentMove.role.opposite())

        val futureMoves : MutableList<Move> = ArrayList()

        var flagStop = false
        this.mutex.acquire()
        while(!flagStop && this.continueThink) {
            this.mutex.release()

            if(count in positions.indices && state.moves.size < 200) {
                val partialFutureMoves = this.rules.genFeasibleMoves(state.currentMove,positions[count])
                futureMoves += partialFutureMoves
                count++

            } else {
                onFinished(futureMoves)

                flagStop = true
            }

            this.mutex.acquire()
        }
        this.mutex.release()
    }

    private fun <T, R : Comparable<R>> Iterable<T>.minOfOrNull(stopThreshold : R, selector: (T) -> R): Pair<T,R>? {
        var result : Pair<T,R>? = null

        var count = this.count()
        var list = this.take(count)

        while(count > 0 && (result?.let { it.second > stopThreshold } != false)) {
            result = if(result == null) {
                list.last() to selector(list.last())
            } else {
                if(minOf(result.second,selector(list.last())) == result.second) {
                    result
                } else {
                    list.last() to selector(list.last())
                }
            }

            list = this.take(--count)
        }

        return result
    }

    private fun <T, R : Comparable<R>> Iterable<T>.maxOfOrNull(stopThreshold : R, selector: (T) -> R): Pair<T,R>? {
        var result : Pair<T,R>? = null

        var count = this.count()
        var list = this.take(count)

        while(count > 0 && (result?.let { it.second < stopThreshold } != false)) {
            result = if(result == null) {
                list.last() to selector(list.last())
            } else {
                if(maxOf(result.second,selector(list.last())) == result.second) {
                    result
                } else {
                    list.last() to selector(list.last())
                }
            }

            list = this.take(--count)
        }

        return result
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