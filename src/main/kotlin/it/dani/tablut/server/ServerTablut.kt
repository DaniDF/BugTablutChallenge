package it.dani.tablut.server

import com.google.gson.Gson
import it.dani.communication.ServerCommunicator
import it.unibo.ai.didattica.competition.tablut.util.StreamUtils
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.concurrent.Semaphore

class ServerTablut(serverIp : String,serverPort : Int) : ServerCommunicator(serverIp,serverPort) {

    private val daemon = Thread { this.daemonIn() }.also { it.start() }
    private val sockOut = DataOutputStream(super.server.getOutputStream())
    private val gson = Gson()

    val mutex = Semaphore(0)

    private fun daemonIn() {
        try {
            val serverIn = DataInputStream(this.server.getInputStream())

            while(!this.server.isInputShutdown) {
                val lines = StreamUtils.readString(serverIn)
                super.onReceiveList.forEach {
                    it(lines)
                }

            }
        } catch (e : IOException) {
            System.err.println(e.message)
        }

        this.mutex.release()
    }

    fun respond(response : Any)  {
        StreamUtils.writeString(this.sockOut,this.gson.toJson(response))
    }
}