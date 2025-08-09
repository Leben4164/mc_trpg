package com.leben.mc_trpg

import org.bukkit.plugin.java.JavaPlugin
//웹 서버
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay

class TRPGPlugin : JavaPlugin() {
    private var embeddedServer: NettyApplicationEngine? = null
    private val pluginScope = CoroutineScope(Dispatchers.Default + Job())

    companion object {
        lateinit var statManager: StatManager
        lateinit var instance: TRPGPlugin
    }

    override fun onEnable() {
        instance = this
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

        statManager = StatManager(dataFolder)

        getCommand("주사위")?.setExecutor(DiceCommand())
        getCommand("판정")?.setExecutor(JudgmentCommand())
        getCommand("캐릭터생성")?.setExecutor(CharacterMakeCommand())
        logger.info("플러그인이 활성화되었습니다.")
    }

    override fun onDisable() {
        embeddedServer?.stop(1000, 1000)
        logger.info("웹 서버가 종료되었습니다.")
        pluginScope.cancel()
    }

    fun sendDelayedMessages(messages: List<String>, delayMillis: Long) {
        pluginScope.launch {
            for (message in messages) {
                withContext(BukkitMainDispatcher) {
                    Bukkit.broadcastMessage(message)
                }
                delay(delayMillis)
            }
        }
    }

    fun showJudgeDisplay(player: Player, messages: List<String>) {

        val location = player.location.add(0.0, 2.0, 0.0)
        val world = player.world
        val textDisplay = world.spawnEntity(location, EntityType.TEXT_DISPLAY) as TextDisplay

        val formattedText = messages.joinToString("\n")
        textDisplay.text(Component.text(formattedText))

        Bukkit.getScheduler().runTaskLater(this, Runnable {
            textDisplay.remove()
        }, 20 * 10L)
    }
}
