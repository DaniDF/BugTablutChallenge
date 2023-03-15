package it.dani.tablut.think

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import it.dani.learn.LearningEpisode
import it.dani.tablut.data.Move
import it.dani.tablut.data.TablutBoard
import it.dani.learn.LearningThinker
import java.io.*

class MontecarloAgent : LearningThinker {
    @Expose
    private var memory : StateActionMemory = StateActionMemory()
    override fun learnEpisode() : LearningEpisode {
        return LearningEpisode(this.memory)
    }

    override fun loadMemory(reader: Reader) {
        val gson = GsonBuilder().create()
        this.memory = gson.fromJson(reader, StateActionMemory::class.java)
    }

    override fun storeMemory(writer: Writer) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        gson.toJson(this.memory,writer)
        writer.flush()
    }

    override fun playMove(board: TablutBoard) : Move {
        return this.memory.getGreedyMove(board)
    }

}