package com.leben.mc_trpg

import org.bukkit.plugin.java.JavaPlugin
//웹 서버
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


class Mc_trpg : JavaPlugin() {
    private var embeddedServer: NettyApplicationEngine? = null

    override fun onEnable() {
        embeddedServer = embeddedServer(Netty, port = 8080) {
            routing {
                post("api/command") {
                    val payload = call.receiveText()
                    server.broadcastMessage("봇으로부터 명령을 받았습니다: $payload")
                    call.respondText("OK")
                }
            }
        }.start(wait = false)

        logger.info("웹 서버가 8080포트에서 시작되었습니다.")
    }

    override fun onDisable() {
        embeddedServer?.stop(1000, 1000)
        logger.info("웹 서버가 종료되었습니다.")
    }
}
