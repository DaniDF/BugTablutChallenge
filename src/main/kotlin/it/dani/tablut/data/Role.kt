package it.dani.tablut.data

enum class Role {
    WHITE, BLACK, WHITEWIN, BLACKWIN, DRAW;

    fun opposite() : Role {
        return when(this) {
            WHITE -> BLACK
            BLACK -> WHITE
            WHITEWIN -> BLACKWIN
            BLACKWIN -> WHITEWIN
            DRAW -> DRAW
        }
    }
}