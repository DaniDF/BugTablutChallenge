package it.dani.learn

import it.dani.tablut.data.Move
import it.dani.tablut.data.TablutBoard
import java.io.Reader
import java.io.Writer

interface LearningThinker  {
    fun learnEpisode() : LearningEpisode

    fun learnEpisode(epsilon : Double) : LearningEpisode

    fun loadMemory(reader: Reader)
    fun storeMemory(writer: Writer)

    fun playMove(board: TablutBoard) : Move
}