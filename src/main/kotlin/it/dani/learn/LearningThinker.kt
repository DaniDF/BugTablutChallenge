package it.dani.learn

import it.dani.tablut.data.Move
import it.dani.tablut.data.TablutBoard
import java.io.OutputStream

interface LearningThinker  {
    fun learnEpisode() : LearningEpisode
    fun storeMemory(outputStream: OutputStream)
    fun playMove(board: TablutBoard) : Move
}