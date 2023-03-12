package it.dani

import com.google.gson.Gson
import it.dani.Main.Companion.PLAYERNAME
import it.dani.tablut.data.Move
import it.dani.tablut.data.Role
import it.dani.tablut.data.TablutBoard
import it.dani.tablut.server.ServerTablut
import it.dani.tablut.server.configuration.Configurator
import it.myunibo.Action
import it.myunibo.State

fun main(args: Array<String>) {

    val role : Role
    val ip : String
    val timeout : Int

    when(args.size) {
        3 -> {
            role = Role.valueOf(args[0].uppercase())
            ip = args[1]
            timeout = (args[2].toInt() - 5).coerceAtLeast(5)
            if(timeout < 0) {
                throw IllegalArgumentException("Error: timeout[${args[2]}] must be not negative")
            }
        }
        else -> throw IllegalArgumentException("Error: not enough parameters, usage <role> <ip> <timeout>")
    }

    val configurator = Configurator(role)

    val gson = Gson()

    val server = ServerTablut(ip,configurator.port).also { server ->
        server.onReceiveList += {
            val board = gson.fromJson(it,TablutBoard::class.java)
            println(board)

            when(board.turn){
                role -> {
                    /*
                    println("I play $move")
                    server.respond(move.toAction())
                    */
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