package it.dani.tablut.data

import java.util.*
import kotlin.collections.ArrayList

class FakeMove(override val board: TablutBoard, override val move : Pair<Position,Position>, override val role : Role, override var precedent : Optional<Move>, override val eat : MutableList<Eat> = ArrayList()) : Move(board,move,role,precedent,eat) {
    override var evaluationResult: Optional<Int>
        get() = super.evaluationResult
        set(value) {}
}