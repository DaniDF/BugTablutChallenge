package it.dani

import com.google.gson.Gson
import it.dani.Main.Companion.DRAW_REWARD
import it.dani.Main.Companion.NEGATIVE_REWARD
import it.dani.Main.Companion.PLAYERNAME
import it.dani.Main.Companion.POSITIVE_REWARD
import it.dani.learn.LearningEpisode
import it.dani.learn.LearningThinker
import it.dani.tablut.data.Move
import it.dani.tablut.data.Role
import it.dani.tablut.data.TablutBoard
import it.dani.tablut.server.ServerTablut
import it.dani.tablut.server.configuration.Configurator
import it.dani.tablut.think.MontecarloAgent
import it.myunibo.Action
import it.myunibo.State
import java.io.FileReader
import java.io.FileWriter

fun main(args: Array<String>) {

    val role : Role
    val ip : String
    val timeout : Int
    val weightFile : String
    val flagLearn: Boolean

    val agent : LearningThinker = MontecarloAgent()

    when(args.size) {
        4,5 -> {
            role = Role.valueOf(args[0].uppercase())
            ip = args[1]
            timeout = (args[2].toInt() - 5).coerceAtLeast(5)
            if(timeout < 0) {
                throw IllegalArgumentException("Error: timeout[${args[2]}] must be not negative")
            }

            flagLearn = when(args[3]) {
                "learn" -> {
                    if(args.size == 5) {
                        weightFile = args[4]
                        agent.loadMemory(FileReader(weightFile))
                    }
                    true
                }
                "play" -> {
                    if(args.size != 5) {
                        throw IllegalArgumentException("Error: not enough parameters, usage <role> <ip> <timeout> <learn | play> <weights_file>")
                    }

                    weightFile = args[4]
                    agent.loadMemory(FileReader(weightFile))
                    false
                }
                else -> throw IllegalArgumentException("Error: not enough parameters, usage <role> <ip> <timeout> <learn | play> <weights_file>")
            }
        }
        else -> throw IllegalArgumentException("Error: not enough parameters, usage <role> <ip> <timeout> <learn | play> <weights_file>")
    }

    val configurator = Configurator(role)

    when(flagLearn) {
        true -> {
            println("Start learning")
            for(count in 0 until 101) {
                println("New game $count")
                learnGame(ip,configurator,role,agent.learnEpisode())
                FileWriter("${count / 100}_weights.json").use { fileOut ->
                    agent.storeMemory(fileOut)
                    fileOut.close()
                }
                Thread.sleep(1000)
            }
            println("End learning")
        }
        false -> {
            println("Start playing")
            playGame(ip, configurator, role, agent)
        }
    }
}

fun learnGame(ip : String, configurator : Configurator, role : Role, learningEpisode: LearningEpisode) {
    val gson = Gson()

    val server = ServerTablut(ip,configurator.port).also { server ->
        server.onReceiveErrorList += { _ ->
            println("My name in ${PLAYERNAME}, I'm $role and I LOSE :(")
            learningEpisode.rewardEpisode(NEGATIVE_REWARD)
        }

        server.onReceiveList += { receivedString ->
            val board = gson.fromJson(receivedString,TablutBoard::class.java)

            when(board.turn){
                role -> {
                    val move = learningEpisode.playOneMove(board)

                    println("I play $move")
                    server.respond(move.toAction())

                }
                Role.BLACKWIN, Role.WHITEWIN -> {
                    if(role.getWinRole() == board.turn) {
                        println("My name is ${PLAYERNAME}, I'm $role and I WIN :)")
                        learningEpisode.rewardEpisode(POSITIVE_REWARD)
                    } else {
                        println("My name is ${PLAYERNAME}, I'm $role and I LOSE :(")
                        learningEpisode.rewardEpisode(NEGATIVE_REWARD)
                    }
                }
                Role.DRAW -> {
                    println("My name is ${PLAYERNAME}, I'm $role and this game was a DRAW :(")
                    learningEpisode.rewardEpisode(DRAW_REWARD)
                }
                else -> {}
            }
        }
    }

    server.respond(PLAYERNAME)

    server.mutex.acquire()
}

fun playGame(ip : String, configurator : Configurator, role : Role, agent: LearningThinker) {
    val gson = Gson()

    val server = ServerTablut(ip,configurator.port).also { server ->
        server.onReceiveErrorList += { _ ->
            println("My name in ${PLAYERNAME}, I'm $role and I LOSE :(")
        }

        server.onReceiveList += { receivedString ->
            val board = gson.fromJson(receivedString,TablutBoard::class.java)
            println(board)

            when(board.turn){
                role -> {
                    val move = agent.playMove(board)

                    println("I play $move")
                    server.respond(move.toAction())
                }
                Role.BLACKWIN, Role.WHITEWIN -> {
                    if(role.getWinRole() == board.turn) {
                        println("My name is ${PLAYERNAME}, I'm $role and I WIN :)")
                    } else {
                        println("My name is ${PLAYERNAME}, I'm $role and I LOSE :(")
                    }
                }
                Role.DRAW -> {
                    println("My name is ${PLAYERNAME}, I'm $role and this game was a DRAW :(")
                }
                else -> {}
            }
        }
    }

    server.respond(PLAYERNAME)

    server.mutex.acquire()
}

fun Move.toAction() : Action {
    return Action("${this.move.first}", "${this.move.second}", when(role) {
        Role.WHITE -> State.Turn.BLACK
        else -> State.Turn.WHITE
    })
}

class Main {
    companion object {
        const val PLAYERNAME = "Bug Tablut Theory"
        const val POSITIVE_REWARD = 1
        const val NEGATIVE_REWARD = -1
        const val DRAW_REWARD = 0
    }
}