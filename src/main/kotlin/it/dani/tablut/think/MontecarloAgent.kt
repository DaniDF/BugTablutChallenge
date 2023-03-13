package it.dani.tablut.think

import com.google.gson.Gson
import it.dani.learn.LearningEpisode
import it.dani.tablut.data.Move
import it.dani.tablut.data.TablutBoard
import it.dani.learn.LearningThinker
import java.io.DataOutputStream
import java.io.OutputStream

class MontecarloAgent : LearningThinker {

    private val memory : StateActionMemory = StateActionMemory()
    override fun learnEpisode() : LearningEpisode {
        return LearningEpisode(memory)
    }

    override fun storeMemory(outputStream: OutputStream) {
        val dataOut = DataOutputStream(outputStream)
        val gson = Gson()
        dataOut.writeChars(gson.toJson(this.memory))
    }

    override fun playMove(board: TablutBoard) : Move {
        return this.memory.getGreedyMove(board)
    }

}