package it.dani.tablut.server.configuration

import it.dani.tablut.data.Role

class Configurator(private val role : Role) {
    val port : Int
    get() {
        return when(this.role) {
            Role.BLACK -> 5801
            else -> 5800
        }
    }
}