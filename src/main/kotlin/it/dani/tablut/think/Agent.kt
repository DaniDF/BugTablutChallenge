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

            val compDfl : (Role) -> Int = {
                when(it) {
                    this.state.role -> Int.MIN_VALUE
                    else -> Int.MAX_VALUE
                }
            }

            val compareFuncDfl: (Role) -> (List<Move>) -> Move? = {
                when (it) {
                    this.state.role -> {
                        { list ->
                            list.maxOfOrNull { m ->
                                if(m.evaluationResult.isPresent) {
                                    m.evaluationResult.get()
                                } else {
                                    compDfl(it)
                                }
                            }
                        }
                    }   //MAX
                    else -> {
                        { list ->
                            list.minOfOrNull { m ->
                                if(m.evaluationResult.isPresent) {
                                    m.evaluationResult.get()
                                } else {
                                    compDfl(it)
                                }
                            }
                        }
                    }   //MIN
                }
            }

            val compareFunc: (Role) -> (Move,Optional<Move>) -> Move = {
                val selector : (Move) -> Optional<Int> = { m -> m.evaluationResult }

                when (it) {
                    this.state.role -> {
                        { a, b ->
                            max(a,b,selector)
                        }
                    }   //MAX
                    else -> {
                        { a, b ->
                            min(a,b,selector)
                        }
                    }   //MIN
                }
            }

            var writeEvaluations: (Move) -> Unit = {}
            writeEvaluations = { m ->

                if(m.precedent.isPresent && m.precedent.get().evaluationResult.isEmpty) {
                    m.following.forEach { it.evaluate(this.evaluator::evaluate) }
                    compareFuncDfl(m.role)(m.following)?.let {
                        m.evaluationResult = it.evaluationResult
                        m.precedent.get().evaluationResult = it.evaluationResult
                        m.precedent.get().following.forEach { bm ->
                            if(bm.evaluationResult.isEmpty) {
                                bm.evaluationResult = Optional.of(compDfl(m.role.opposite()))
                            }
                        }
                    }

                } else if(m.precedent.isPresent) {
                    var continueEvaluation = true
                    var localBest : Optional<Move> = Optional.empty()

                    m.following.forEach {
                        if(continueEvaluation) {
                            it.evaluate(this.evaluator::evaluate)
                            localBest = Optional.of(compareFunc(m.role)(it,localBest))

                            continueEvaluation = compareFunc(m.precedent.get().role)(m.precedent.get(),localBest) == localBest.get()
                        }
                    }

                    if(localBest.isPresent) {
                        m.evaluationResult = localBest.get().evaluationResult
                    }

                    if(continueEvaluation && localBest.isPresent) {
                        m.precedent.get().evaluationResult = compareFunc(m.precedent.get().role)(m.precedent.get(),localBest).evaluationResult
                    }
                }


                if (m.precedent.isPresent) {
                    writeEvaluations(m.precedent.get())
                }
            }

            writeEvaluations(this.state.currentMove)

        }

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

    companion object {
        private const val DEPTH_LIMIT = 3

        private fun <T, R : Comparable<R>> max(a : T, b : Optional<T>, selector : (T) -> Optional<R>) : T {
            return if(b.isEmpty) {
                a
            } else if(b.isPresent && selector(b.get()).isEmpty) {
                a
            } else {
                if(maxOf(selector(a).get(),selector(b.get()).get()) == selector(a).get()) {
                    a
                } else {
                    b.get()
                }
            }
        }

        private fun <T, R : Comparable<R>> min(a : T, b : Optional<T>, selector : (T) -> Optional<R>) : T {
            return if(b.isEmpty) {
                a
            } else if(b.isPresent && selector(b.get()).isEmpty) {
                a
            } else {
                if(minOf(selector(a).get(),selector(b.get()).get()) == selector(a).get()) {
                    a
                } else {
                    b.get()
                }
            }
        }

        private fun <T,R : Comparable<R>> List<T>.maxOfOrNull(selector : (T) -> R) : T? {
            var result : T? = null

            this.forEach {
                result = if(result == null) {
                    it
                } else if(maxOf(selector(it),selector(result!!)) == selector(result!!)) {
                    result
                } else {
                    it
                }
            }

            return result
        }

        private fun <T,R : Comparable<R>> List<T>.minOfOrNull(selector : (T) -> R) : T? {
            var result : T? = null

            this.forEach {
                result = if(result == null) {
                    it
                } else if(minOf(selector(it),selector(result!!)) == selector(result!!)) {
                    result
                } else {
                    it
                }
            }

            return result
        }
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