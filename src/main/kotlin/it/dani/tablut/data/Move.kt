package it.dani.tablut.data

import com.google.gson.annotations.Expose
import java.io.Serializable

open class Move(@Expose open val board: TablutBoard, @Expose open val move : Pair<Position,Position>, @Expose open val role : Role, @Expose open val eat : MutableList<Eat> = ArrayList()) : Serializable {
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