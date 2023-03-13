package it.dani.tablut.think

import com.google.gson.Gson
import it.dani.learn.LearningEpisode
import it.dani.tablut.data.Move
import it.dani.tablut.data.TablutBoard
import it.dani.learn.LearningThinker
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.lang.StringBuilder

class MontecarloAgent : LearningThinker {

    private var memory : StateActionMemory = StateActionMemory()
    override fun learnEpisode() : LearningEpisode {
        return LearningEpisode(memory)
    }

    override fun loadMemory(inputStream: InputStream) {
        val dataIn = BufferedReader(InputStreamReader(inputStream))
        val gson = Gson()

        val lines = StringBuilder()
        dataIn.readLines().forEach { lines.append(it) }

        this.memory = gson.fromJson(lines.toString(), StateActionMemory::class.java)
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