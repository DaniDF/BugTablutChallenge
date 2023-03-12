package it.dani.tablut.data

class TablutBoard : java.io.Serializable {
    var board : Array<Array<TablutBoardCellValue>> = Array(9) {
        Array(9) { TablutBoardCellValue.EMPTY }
    }

    var turn : Role = Role.WHITE

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

enum class TablutBoardCellValue {
    EMPTY,WHITE,KING,BLACK,THRONE;

    fun opposite() : TablutBoardCellValue {
        return when(this) {
            WHITE -> BLACK
            BLACK -> WHITE
            THRONE -> THRONE
            KING -> KING
            EMPTY -> EMPTY
        }
    }
}