package it.dani.tablut.data

data class Position(val row : Int, val col : Int) {
    override fun toString(): String {
        val alphabet = "abcdefghi"
        return "${alphabet[col]}${row+1}"
    }
}
