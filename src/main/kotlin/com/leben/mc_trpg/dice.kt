package com.leben.mc_trpg

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import kotlin.random.Random

class dice : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) { //argument가 없는 경우
            sender.sendMessage("주사위 nDm : m각 주사위를 n번 돌린다.")
            return false
        }

        val parts = args[0].split("D")
        val count = parts[0].toIntOrNull() ?: 1
        val sides = parts.getOrNull(1)?.toIntOrNull() ?: 6
        val results = mutableListOf<Int>()
        var total = 0

        for (i in 1..count) {
            val roll = Random.nextInt(1, sides + 1)
            results += roll
            total += roll
        }

        sender.sendMessage("주사위 결과");
        for (i in 1..count) sender.sendMessage("${i+1} 번째 주사위 : ${results[i]}")
        sender.sendMessage("총합 : $total")
        return true
    }
}