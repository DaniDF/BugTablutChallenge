package it.dani.tablut.server

import com.google.gson.Gson
import it.dani.communication.ServerCommunicator
import it.unibo.ai.didattica.competition.tablut.util.StreamUtils
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.concurrent.Semaphore

class ServerTablut(serverIp : String,serverPort : Int) : ServerCommunicator(serverIp,serverPort) {

    private val sockOut = DataOutputStream(super.server.getOutputStream())
    private val gson = Gson()

    val mutex = Semaphore(0)

    init {
        Thread { this.receive() }.also { it.start() }
    }

    private fun receive() {
        try {
            val serverIn = DataInputStream(this.server.getInputStream())

            while(!this.server.isInputShutdown) {
                val lines = StreamUtils.readString(serverIn)
                super.onReceiveList.forEach { it(lines) }

            }
        } catch (e : IOException) {
            super.onReceiveErrorList.forEach { it(e) }
        }

        this.mutex.release()
    }

    fun respond(response : Any)  {
        val outString = this.gson.toJson(response)
        StreamUtils.writeString(this.sockOut,outString)
        super.onSendList.forEach { it(outString) }
    }
}