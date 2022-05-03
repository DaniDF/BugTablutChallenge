package it.dani.tablut.time

import it.dani.time.Timer
import java.util.LinkedList

class TablutTimer(override var time: Long) : Timer {
    override val onTikListeners : MutableList<() -> Any> = LinkedList()
    private val daemon = Thread {
        try {
            val oldTime = System.currentTimeMillis()

            while(System.currentTimeMillis() - oldTime < this.time) {
                Thread.sleep(1000)
            }

            this.onTikListeners.forEach {
                it()
            }
        } catch (_: InterruptedException) {}

    }

    override fun start() {
        this.daemon.start()
    }

    override fun stop() {
        this.daemon.interrupt()
    }
}