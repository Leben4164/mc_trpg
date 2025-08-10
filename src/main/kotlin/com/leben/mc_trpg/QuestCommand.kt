package com.leben.mc_trpg

import com.leben.mc_trpg.Quest
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

class QuestCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§c플레이어만 이 명령어를 사용할 수 있습니다.")
            return true
        }

        if (args.size < 4) {
            sender.sendMessage("§c사용법: /퀘스트요청 <대상> <제목> <내용...> <보상...>")
            return true
        }

        val targetPlayer = Bukkit.getPlayer(args[0])
        if (targetPlayer == null || !targetPlayer.isOnline) {
            sender.sendMessage("§c플레이어 '${args[0]}'를 찾을 수 없습니다.")
            return true
        }

        // 퀘스트 제목과 내용을 분리 (예시: 제목은 첫 번째 인자, 내용은 나머지 인자)
        val title = args[1]
        val description = args.slice(2 until args.size - 1).joinToString(" ")
        val reward = args.last()

        val uniqueId = UUID.randomUUID()
        val questRequest = Quest(
            uniqueId = uniqueId,
            requesterName = sender.name,
            targetName = targetPlayer.name,
            title = title,
            description = description,
            reward = reward
        )

        // TRPGPlugin에 퀘스트 요청을 처리하는 함수를 호출
        TRPGPlugin.instance.showQuestRequestUI(targetPlayer, questRequest)

        sender.sendMessage("§a${targetPlayer.name}님에게 퀘스트를 요청했습니다.")
        return true
    }
}