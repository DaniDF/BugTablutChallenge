package it.dani.communication

import java.lang.Exception
import java.net.Socket
import java.util.LinkedList

abstract class ServerCommunicator(serverIp : String, serverPort : Int) {
    internal val server : Socket
    val onReceiveList : MutableList<(String) -> Any> = LinkedList()
    val onReceiveErrorList : MutableList<(Exception) -> Any> = LinkedList()
    val onSendList : MutableList<(String) -> Any> = LinkedList()

    init {
        if(serverPort !in 0..65535) {
            throw IllegalArgumentException("Error: port value must be in 0..65535")
        }

        this.server = Socket(serverIp,serverPort)
    }
}