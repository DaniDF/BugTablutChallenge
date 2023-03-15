package it.dani.tablut.data

import com.google.gson.annotations.Expose
import java.io.Serializable

data class Position(@Expose val row : Int, @Expose val col : Int) : Serializable {
    override fun toString(): String {
        return "${ALPHABET[col]}${row+1}"
    }

    companion object {
        private const val ALPHABET = "abcdefghi"
        fun fromString(str: String) : Position {
            val charAlphabet = ALPHABET.toCharArray()

            if(str.length != 2 || str[0] !in charAlphabet) {
                throw IllegalArgumentException("Error string must be like [a-i][1-9]")
            }

            val num = str[1].digitToInt()

            if(num !in 1..9) {
                throw IllegalArgumentException("Error string must be like [a-i][1-9]")
            }

            val row = charAlphabet.indexOfFirst { x -> str[0] == x }

            if(row == -1) {
                throw IllegalArgumentException("Error string must be like [a-i][1-9]")
            }

            val col = num - 1

            return Position(col, row)
        }
    }
}
