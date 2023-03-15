package it.dani.trainer

import it.dani.tablut.data.*
import it.dani.tablut.data.Role.Companion.convertIntoRole
import java.io.BufferedReader
import java.io.FileReader
import java.util.regex.Pattern

class FileLoader(private val filename : String) {
    fun loadGame() : TrainGame {
        val result = TrainGame()

        val gameLinePattern = Pattern.compile("(O|K|T|W|B){9}")
        val roleLinePattern = Pattern.compile("^(B|WW|W|BW|D)$") //It is not an ambiguous grammar LL(1)
        val moveLinePattern = Pattern.compile("(a|b|c|d|e|f|g|h|i)\\d", Pattern.CASE_INSENSITIVE)

        var gameRole = ""

        BufferedReader(FileReader(this.filename)).use { fileIn ->
            var gameString = StringBuilder()

            fileIn.readLines().forEach { line ->
                val matchLine = gameLinePattern.matcher(line)
                val matchRole = roleLinePattern.matcher(line)
                val matchMove = moveLinePattern.matcher(line)

                if(matchLine.find()) {
                    gameString.append(matchLine.group())

                } else if(matchRole.find()) {
                    gameRole = matchRole.group()

                } else if(matchMove.find()) {
                    val from = matchMove.group(0).lowercase()

                    if(matchMove.find()) {
                        val to = matchMove.group(0).lowercase()
                        this.computeGamePhase(gameString.toString(), gameRole, from to to, result)
                    }

                    gameString = StringBuilder()
                    gameRole = ""
                }
            }
        }

        this.computeGameResult(gameRole, result)

        return result
    }

    private fun computeGamePhase(gameString : String, role : String, move : Pair<String,String>, game : TrainGame) {
        val board = TablutBoard()

        gameString.forEachIndexed { index, c ->
            val row = index / 9
            val col = index % 9
            board.apply {
                this.board[row][col] = c.convertToTablutCellValue()
            }
        }

        board.turn = role.convertIntoRole()
        game.gameMoves += Move(board,Position.fromString(move.first) to Position.fromString(move.second),board.turn)
    }

    private fun computeGameResult(role: String, game: TrainGame) {
        val gameResult = role.convertIntoRole()
        if(gameResult in arrayOf(Role.WHITEWIN, Role.BLACKWIN, Role.DRAW)) {
            game.gameResult = gameResult
        }
    }

    companion object {
        private fun Char.convertToTablutCellValue() : TablutBoardCellValue {
            return when(this) {
                'O' -> TablutBoardCellValue.EMPTY
                'W' -> TablutBoardCellValue.WHITE
                'B' -> TablutBoardCellValue.BLACK
                'T' -> TablutBoardCellValue.THRONE
                'K' -> TablutBoardCellValue.KING
                else -> throw IllegalArgumentException("Error: char \'$this\' is not a valid char")
            }
        }
    }
}