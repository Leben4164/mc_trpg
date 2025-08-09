package com.leben.mc_trpg

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import kotlin.random.Random

class DiceCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.isEmpty()) { //argument가 없는 경우
            sender.sendMessage("주사위 nDm : m각 주사위를 n번 돌린다.")
            return false
        }

        val parts = args[0].split("d") //d를 기준으로 숫자 구분
        val count = parts[0].toIntOrNull() ?: 1 //기본값 1
        val sides = parts.getOrNull(1)?.toIntOrNull() ?: 6 //기본값 6
        val results = mutableListOf<Int>() //주사위 결과 모아놓는 리스트
        var total = 0 //주사위 총합

        for (i in 1..count) {
            val roll = Random.nextInt(1, sides + 1)
            results.add(roll)
            total += roll
        }

        sender.sendMessage("§e${sender.name}§f 님이 주사위를 굴렸습니다.")
        sender.sendMessage("§a[주사위 결과]")
        sender.sendMessage(" §f각 주사위 값: §b${results.joinToString(", ")}")
        sender.sendMessage(" §f총합 : §e$total")
        return true
    }
}