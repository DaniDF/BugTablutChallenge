package it.dani.tablut.data

import java.io.Serializable

enum class Role : Serializable {
    WHITE, BLACK, WHITEWIN, BLACKWIN, DRAW;

    companion object {
        fun String.convertIntoRole() : Role {
            return when(this) {
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