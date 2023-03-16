package it.dani.tablut.data

import com.google.gson.annotations.Expose
import java.io.Serializable

open class TablutBoard : Serializable {
    @Expose
    open var board : Array<Array<TablutBoardCellValue>> = Array(9) {
        Array(9) { TablutBoardCellValue.EMPTY }
    }

    @Expose
    open var turn : Role = Role.WHITE

    fun isThrone(row : Int, col : Int) : Boolean {
        return TablutBoardCells.throne.contains(row to col)
    }

    fun isBlackStation(row : Int, col : Int) : Boolean {
        return TablutBoardCells.blackStation.contains(row to col)
    }

    override fun toString(): String {
        val result = StringBuilder("___________________________\n")

        this.board.forEach { valR ->
            valR.forEach { valC ->
                result.append(when(valC) {
                    TablutBoardCellValue.EMPTY -> " . "
                    TablutBoardCellValue.WHITE -> " # "
                    TablutBoardCellValue.KING -> " W "
                    TablutBoardCellValue.BLACK -> " § "
                    TablutBoardCellValue.THRONE -> " + "
                })
            }

            result.append("\n")
        }

        result.append("___________________________")

        return result.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TablutBoard

        if (!board.contentDeepEquals(other.board)) return false
        if (turn != other.turn) return false

        return true
    }

    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + turn.hashCode()
        return result
    }


}

enum class TablutBoardCellValue : Serializable {
    EMPTY,WHITE,KING,BLACK,THRONE;
}

class TablutBoardCells {
    companion object {
        val throne = hashSetOf(4 to 4)
        val blackStation = hashSetOf(
            3 to 0, 0 to 3, 3 to 8, 8 to 3,
            4 to 0, 0 to 4, 4 to 8, 8 to 4,
            5 to 0, 0 to 5, 5 to 8, 8 to 5,
            4 to 1, 1 to 4, 4 to 7, 7 to 4
        )
    }
}