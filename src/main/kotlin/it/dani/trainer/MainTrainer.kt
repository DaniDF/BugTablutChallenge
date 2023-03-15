package it.dani.trainer

import it.dani.Main
import it.dani.learn.LearningThinker
import it.dani.learnGame
import it.dani.tablut.data.Role
import it.dani.tablut.data.Role.Companion.convertIntoRole
import it.dani.tablut.server.configuration.Configurator
import it.dani.tablut.think.MontecarloAgent
import java.io.File
import java.io.FileWriter
import java.text.ParseException

fun main(args : Array<String>) {
    if(args.size < 3) {
        error("Error no file provided - use <role> <ip> <files>...")
    }

    val role = args[0].convertIntoRole()
    val ip = args[1]
    val filenames = args.copyOfRange(2,args.size)

    val agent : LearningThinker = MontecarloAgent()

    filenames.forEachIndexed { index, filename ->
        val fd = File(filename)
        if(fd.isDirectory) {
            fd.list()?.forEachIndexed { indexDir, filenameDir ->
                println("New game $index $indexDir $filenameDir")
                try {
                    learnFromAFile(agent, "${fd.absolutePath}${File.separator}$filenameDir", role)
                } catch(e : Exception) {
                    println("Error file not well formatted $filenameDir")
                }
            }

        } else {
            try {
                println("New game $index")
                learnFromAFile(agent, filename, role)
            } catch(e : Exception) {
                println("Error file not well formatted $filename")
            }
        }
    }

    FileWriter("weights.json").use { fileOut ->
        agent.storeMemory(fileOut)
        fileOut.close()
    }

    println("Start learning")
    for(count in 0 until 100000) {
        println("New game $count")
        learnGame(ip,Configurator(role),role,agent.learnEpisode(0.1))
        FileWriter("${count / 100}_weights.json").use { fileOut ->
            agent.storeMemory(fileOut)
            fileOut.close()
        }
        Thread.sleep(1000)
    }
    println("End learning")

}

fun learnFromAFile(agent : LearningThinker, filename : String, role : Role) {
    val episode = agent.learnEpisode()

    FileLoader(filename).also { fileLoader ->
        val game = fileLoader.loadGame()

        val roleMoves = game.gameMoves.filter { it.board.turn == role }.map { it.board to it }.toMutableList()
        val antiRoleMoves = game.gameMoves.filter { it.board.turn != role }.map { it.board to it }.toMutableList()

        episode.setPredefinedHistory(roleMoves)
        val reward = try {
            when(game.gameResult) {
                Role.WHITEWIN -> {
                    if(role == Role.WHITE){
                        Main.POSITIVE_REWARD
                    } else {
                        Main.NEGATIVE_REWARD
                    }
                }
                Role.BLACKWIN -> {
                    if(role == Role.BLACK){
                        Main.POSITIVE_REWARD
                    } else {
                        Main.NEGATIVE_REWARD
                    }
                }
                Role.DRAW -> Main.DRAW_REWARD
                else -> Main.NEGATIVE_REWARD
            }
        } catch(e : UninitializedPropertyAccessException) {
            throw ParseException("Error game final state not present",-1)
        }
        episode.rewardEpisode(reward)

        episode.setPredefinedHistory(antiRoleMoves)
        episode.rewardEpisode(-reward)
    }
}