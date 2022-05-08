package it.dani

import com.google.gson.Gson
import it.dani.Main.Companion.PLAYERNAME
import it.dani.tablut.data.Move
import it.dani.tablut.data.Role
import it.dani.tablut.data.TablutBoard
import it.dani.tablut.server.ServerTablut
import it.dani.tablut.server.configuration.Configurator
import it.dani.tablut.think.Agent
import it.dani.tablut.think.AgentState
import it.dani.tablut.time.TablutTimer
import it.myunibo.Action
import it.myunibo.State
import kotlin.collections.ArrayList

fun main(args: Array<String>) {

    val role : Role
    val ip : String
    val timeout : Int

    when(args.size) {
        3 -> {
            role = Role.valueOf(args[0].uppercase())
            ip = args[1]
            timeout = args[2].toInt()
            if(timeout < 0) {
                throw IllegalArgumentException("Error: timeout[${args[2]}] must be not negative")
            }
        }
        else -> throw IllegalArgumentException("Error: not enough parameters, usage <role> <ip> <timeout>")
    }

    val configurator = Configurator(role)

    val gson = Gson()

    var agentState = AgentState(role, TablutBoard())

    val server = ServerTablut(ip,configurator.port).also { server ->
        server.onReceiveList += {
            val board = gson.fromJson(it,TablutBoard::class.java)
            println(board)

            when(board.turn){
                role -> {
                    agentState.updateBoard(board)

                    agentState.moves.clear()

                    if(agentState.moves.isNotEmpty()) {
                        var flagStop = false
                        var count = -1
                        while(!flagStop && ++count !in agentState.moves.first().following.indices) {
                            flagStop = agentState.moves.first().following[count].futureTable() == board
                        }

                        if(flagStop) {
                            agentState.moves = agentState.moves.first().following[count].following
                        } else {
                            agentState.moves = ArrayList()
                        }
                    }

                    val agent = Agent(agentState)
                    agent.startThink()
                    TablutTimer(timeout.toLong() * 1000).apply {
                        onTikListeners += {
                            agent.stopThink()
                            agentState = agent.state
                            agentState.moves.sortByDescending { m ->
                                if(m.evaluationResult.isPresent) {
                                    m.evaluationResult.get()
                                } else {
                                    Int.MIN_VALUE
                                }
                            }
                            val move = agentState.moves.first()
                            println("I play $move")
                            server.respond(move.toAction())
                        }
                    }.start()
                }
                Role.BLACKWIN, Role.WHITEWIN -> {
                    if(role == Role.valueOf(it)) {
                        println("My name in ${PLAYERNAME}, I'm $role and I WIN :)")
                    } else {
                        println("My name in ${PLAYERNAME}, I'm $role and I LOSE :(")
                    }
                }
                Role.DRAW -> {
                    println("My name in ${PLAYERNAME}, I'm $role and this game was a DRAW :(")
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
    }
}