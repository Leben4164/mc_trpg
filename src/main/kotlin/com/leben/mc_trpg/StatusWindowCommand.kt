package com.leben.mc_trpg

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class StatusWindowCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): Boolean {
        if (sender is Player) {
            val player = sender

            val stats = TRPGPlugin.statManager.getStat(player)
            // 여기에 상태창에 표시할 내용을 작성합니다.
            val statusMessages = listOf(
                "§l--- 상태창 ---",
                "§6이름: §f${stats.playerName}",
                "",
                "§b체력: §f${stats.hp} §b마나: §f${stats.mp}",
                "",
                "§a힘: §f${stats.strength} §재주: §f${stats.dexterity}",
                "§a지능: §f${stats.intelligence} §a행운: §f${stats.luck}"
            )

            // TRPGPlugin에 구현된 상태창 함수를 호출
            TRPGPlugin.instance.showStatusWindow(player, statusMessages)

            return true
        }

        sender.sendMessage("§c플레이어만 이 명령어를 사용할 수 있습니다.")
        return false
    }
}