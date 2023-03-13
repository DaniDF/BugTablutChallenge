package it.dani.tablut.data

open class Move(open val board: TablutBoard, open val move : Pair<Position,Position>, open val role : Role) {
    override fun toString(): String {
        return "from: ${this.move.first} to ${this.move.second}"
    }

    override fun hashCode(): Int {
        var result = board.hashCode()
        result = 31 * result + move.hashCode()
        result = 31 * result + role.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Move) return false

        if (move != other.move) return false
        if (role != other.role) return false

        return true
    }
}