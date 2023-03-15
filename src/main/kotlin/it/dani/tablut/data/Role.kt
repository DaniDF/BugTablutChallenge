package it.dani.tablut.data

import java.io.Serializable

enum class Role : Serializable {
    WHITE, BLACK, WHITEWIN, BLACKWIN, DRAW;

    fun getWinRole() : Role {
        return when(this) {
            WHITE -> WHITEWIN
            BLACK -> BLACKWIN
            else -> throw IllegalArgumentException("Error i can not transform $this in winning")
        }
    }

    fun getLoseRole() : Role {
        return when(this) {
            WHITE -> BLACKWIN
            BLACK -> WHITEWIN
            else -> throw IllegalArgumentException("Error i can not transform $this in losing")
        }
    }

    companion object {
        fun String.convertIntoRole() : Role {
            return when(this.uppercase()) {
                "W","WHITE" -> WHITE
                "B","BLACK" -> BLACK
                "WW","WHITEWIN" -> WHITEWIN
                "BW","BLACKWIN" -> BLACKWIN
                "D","DRAW" -> DRAW
                else -> throw IllegalArgumentException("Error: this string $this can not be converted into a Role object")
            }
        }
    }
}