package com.leben.mc_trpg

import io.ktor.events.Events
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
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.UUID
import org.bukkit.util.Vector

class TRPGPlugin : JavaPlugin(), Listener {
    private var embeddedServer: NettyApplicationEngine? = null
    private val pluginScope = CoroutineScope(Dispatchers.Default + Job()) //비동기를 위한 설정

    companion object {
        lateinit var statManager: StatManager
        lateinit var instance: TRPGPlugin
    }

    val pendingQuestRequests = mutableMapOf<UUID, Quest>()

    override fun onEnable() { //플러그인이 활성화 되었을 때 실행
        instance = this
        embeddedServer = embeddedServer(Netty, port = 8080) { //8080포트에서 웹서버 시작
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

        //명령어 로드
        getCommand("주사위")?.setExecutor(DiceCommand())
        getCommand("판정")?.setExecutor(JudgmentCommand())
        getCommand("캐릭터생성")?.setExecutor(CharacterMakeCommand())
        getCommand("상태창")?.setExecutor(StatusWindowCommand())
        getCommand("퀘스트")?.setExecutor(QuestCommand())
        logger.info("플러그인이 활성화되었습니다.")
        server.pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {
        embeddedServer?.stop(1000, 1000)
        logger.info("웹 서버가 종료되었습니다.")
        pluginScope.cancel()
        statManager.saveStatInFile()
    }

    fun showJudgeDisplay(player: Player, messages: List<String>) {
        val location = player.location //플레이어 위치
        val world = player.world //플레이어가 접속해 있는 맵

        val direction = location.direction //플레이어가 바라보고 있는 방향
        val offset = direction.clone().multiply(1.5) //1.5 블록 앞에서 소환
        val displayLocation = location.add(offset).add(0.0, 0.5, 0.0) //창 소환 위치

        val textDisplay = world.spawnEntity(displayLocation, EntityType.TEXT_DISPLAY) as TextDisplay
        textDisplay.setRotation(player.location.yaw + 180f, 0f)

        val uniqueId = UUID.randomUUID()
        textDisplay.addScoreboardTag("judgment-$uniqueId")

        pluginScope.launch {
            var currentText = ""
            messages.forEachIndexed { index, line ->
                if (index > 0) {
                    currentText += "\n"
                    withContext(BukkitMainDispatcher) {
                        textDisplay.text(Component.text(currentText))
                    }
                    delay(30)
                }

                line.forEach { char ->
                    currentText += char
                    withContext(BukkitMainDispatcher) {
                        textDisplay.text(Component.text(currentText))
                    }
                    delay(30)
                }
            }


            withContext(BukkitMainDispatcher) {
                val formattedText = messages.joinToString("\n") //모든 메세지 합침

                val firstLineWithBracket = messages.firstOrNull { it.contains("]") }
                val estimatedWidth = if (firstLineWithBracket != null) {
                    val trimmedText = firstLineWithBracket.substringBefore("]")
                    getEstimatedTextWidth(trimmedText)
                } else {
                    getEstimatedTextWidth(formattedText)
                }

                val heightOffset = when (messages.size) {
                    7 -> 0.0
                    8 -> 0.3
                    9 -> 0.5
                    else -> 0.0
                }

                val rightDirection = Vector(-direction.z, 0.0, direction.x).normalize()
                val buttonOffset = rightDirection.clone().multiply(estimatedWidth / 2.0 + 1.1)

                val closeButtonLocation = displayLocation.add(buttonOffset).add(0.0, 1.5 + heightOffset, 0.0)
                val closeButtonText = world.spawnEntity(closeButtonLocation, EntityType.TEXT_DISPLAY) as TextDisplay
                closeButtonText.text(Component.text("§c§lX"))
                closeButtonText.setRotation(player.location.yaw + 180f, 0f)
                closeButtonText.addScoreboardTag("judgment-$uniqueId")

                val interaction = world.spawnEntity(closeButtonLocation, EntityType.INTERACTION) as Interaction
                interaction.interactionWidth = 0.7f
                interaction.interactionHeight = 0.7f
                interaction.addScoreboardTag("judgment-interaction-$uniqueId")
            }
        }

    }

    fun showStatusWindow(player: Player, messages: List<String>) {
        val location = player.location
        val world = player.world
        val formattedText = messages.joinToString("\n")

        val direction = location.direction
        val offset = direction.clone().multiply(1.5)
        val windowCenterLocation = location.clone().add(offset).add(0.0, 1.8, 0.0)

        val uniqueId = UUID.randomUUID()
        val playerYawDegrees = location.yaw

        // 2. 상태창 내용 생성
        val contentLocation = windowCenterLocation.clone().add(0.0, 0.2, 0.0)
        val contentDisplay = world.spawnEntity(contentLocation, EntityType.TEXT_DISPLAY) as TextDisplay
        contentDisplay.text(Component.text(formattedText))
        contentDisplay.setRotation(playerYawDegrees + 180f, 0f)
        contentDisplay.addScoreboardTag("status-window-$uniqueId")

        // 3. 닫기 버튼 생성
        val rightDirection = Vector(-direction.z, 0.0, direction.x).normalize()
        val buttonOffset = rightDirection.clone().multiply(4.0)
        val closeButtonLocation = windowCenterLocation.clone().add(buttonOffset).add(0.0, 1.5, 0.0)

        val closeButtonText = world.spawnEntity(closeButtonLocation, EntityType.TEXT_DISPLAY) as TextDisplay
        closeButtonText.text(Component.text("§c§lX"))
        closeButtonText.setRotation(playerYawDegrees + 180f, 0f)
        closeButtonText.isShadowed = true
        closeButtonText.addScoreboardTag("status-window-$uniqueId")

        val interaction = world.spawnEntity(closeButtonLocation, EntityType.INTERACTION) as Interaction
        interaction.interactionWidth = 1.0f
        interaction.interactionHeight = 1.0f
        interaction.addScoreboardTag("status-window-interaction-$uniqueId")
    }

    fun showQuestUI(player: Player, quest: Quest) {
        val location = player.location
        val world = player.world
        val direction = location.direction

        val offset = direction.clone().multiply(1.5)
        val uiLocation = location.clone().add(offset).add(0.0, 1.8, 0.0)

        pendingQuestRequests[quest.uniqueId] = quest

        val messages = listOf(
            "§l---퀘스트 요청---",
            "§6요청자: §f${quest.requesterName}",
            "",
            "§6제목: §f${quest.title}",
            "§6내용: §f${quest.description}",
            "",
            "§6보상: §f${quest.reward}"
        )
        val formattedText = messages.joinToString("\n")

        val textDisplay = world.spawnEntity(uiLocation, EntityType.TEXT_DISPLAY) as TextDisplay
        textDisplay.setRotation(player.location.yaw + 180f, 0f)
        textDisplay.text(Component.text(formattedText))
        textDisplay.addScoreboardTag("quest-request-${quest.uniqueId}")

        // 수락 버튼과 거절 버튼 생성
        val acceptButtonLocation = uiLocation.clone().add(-0.5, -0.8, 0.0) // 버튼 위치 조정
        val declineButtonLocation = uiLocation.clone().add(0.5, -0.8, 0.0)

        // 수락 버튼
        val acceptButton = world.spawnEntity(acceptButtonLocation, EntityType.TEXT_DISPLAY) as TextDisplay
        acceptButton.setRotation(player.location.yaw + 180f, 0f)
        acceptButton.text(Component.text("§a[ 수락 ]"))
        acceptButton.addScoreboardTag("quest-request-accept-${quest.uniqueId}")

        val acceptInteraction = world.spawnEntity(acceptButtonLocation, EntityType.INTERACTION) as Interaction
        acceptInteraction.interactionWidth = 1.0f
        acceptInteraction.interactionHeight = 1.0f
        acceptInteraction.addScoreboardTag("quest-request-accept-${quest.uniqueId}")

        // 거절 버튼
        val declineButton = world.spawnEntity(declineButtonLocation, EntityType.TEXT_DISPLAY) as TextDisplay
        declineButton.setRotation(player.location.yaw + 180f, 0f)
        declineButton.text(Component.text("§c[ 거절 ]"))
        declineButton.addScoreboardTag("quest-request-decline-${quest.uniqueId}")

        val declineInteraction = world.spawnEntity(declineButtonLocation, EntityType.INTERACTION) as Interaction
        declineInteraction.interactionWidth = 1.0f
        declineInteraction.interactionHeight = 1.0f
        declineInteraction.addScoreboardTag("quest-request-decline-${quest.uniqueId}")
    }

    @EventHandler
    fun onPlayerInteractAtEntity(event: PlayerInteractEntityEvent) {
        val clickedEntity = event.rightClicked
        val player = event.player

        if (clickedEntity is Interaction) {
            clickedEntity.scoreboardTags.forEach { tag ->
                if (tag.startsWith("judgment-interaction-")) {
                    val uniqueId = tag.removePrefix("judgment-interaction-")

                    clickedEntity.world.entities.forEach { entity ->
                        if (entity.scoreboardTags.contains("judgment-$uniqueId")) {
                            entity.remove()
                        } else if (entity.scoreboardTags.contains("judgment-interaction-$uniqueId")) {
                            entity.remove()
                        }
                    }
                    event.isCancelled = true
                    return
                }

                if (tag.startsWith("status-window-interaction-")) {
                    val uniqueId = tag.removePrefix("status-window-interaction-")
                    clickedEntity.world.entities.forEach { entity ->
                        entity.remove()
                    }
                }
            }
        }
    }

    private fun getEstimatedTextWidth(text: String): Double {
        val averageCharWidth = 0.07

        var totalWidth = 0.0
        var inColorCode = false

        text.forEach { char ->
            if (char == '§') {
                inColorCode = true
            } else if (inColorCode) {
                inColorCode = false
            } else {
                totalWidth += averageCharWidth
            }
        }
        return totalWidth
    }
}