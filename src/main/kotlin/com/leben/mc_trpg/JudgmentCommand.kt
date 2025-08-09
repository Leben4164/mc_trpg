package com.leben.mc_trpg

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import kotlin.random.Random

class JudgmentCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.size < 3) {
            sender.sendMessage("§b[판정]§f 사용법: /판정 <플레이어> <판정종류> <기준스탯>")
            return false
        }

        val targetPlayer = Bukkit.getPlayer(args[0])
        val judgmentType = args[1].lowercase() // 판정 종류
        val statName = args[2] // 기준 스탯

        if (targetPlayer == null) {
            sender.sendMessage("§c[오류]§f 해당 플레이어를 찾을 수 없습니다.")
            return false
        }

        // 스탯 값 가져오기 (실제 구현 필요)
        val playerStat = getPlayerStat(targetPlayer, statName)
        if (playerStat == null) {
            sender.sendMessage("§c[오류]§f '$statName' 스탯을 찾을 수 없습니다.")
            return false
        }

        // 판정 종류에 따른 주사위 횟수와 면 결정
        val (count, sides) = when (judgmentType) {
            "공격", "스탯" -> 1 to 20 // 1d20 주사위
            "능력치" -> 3 to 6 // 3d6 주사위
            "백분율" -> 2 to 10 // 2d10으로 1d100 효과
            else -> {
                sender.sendMessage("§c[오류]§f 지원하지 않는 판정 종류입니다. (공격, 스탯, 능력치, 백분율 중 선택)")
                return false
            }
        }

        // 주사위 굴리기
        val results = mutableListOf<Int>()
        var totalRollResult = 0
        var finalResult = ""
        for (i in 1..count) {
            val roll = when (judgmentType) {
                "백분율" -> {
                    if (i == 1) Random.nextInt(0, sides) * 10
                    else Random.nextInt(1, 11)
                }
                else -> Random.nextInt(1, sides + 1)
            }
            results.add(roll)
            totalRollResult += roll
        }

        if (totalRollResult > playerStat) {
            finalResult = "실패"
        } else {
            if (totalRollResult == 1) {
                finalResult = "대성공"
            } else {
                finalResult = "성공"
            }
        }
        val messages = mutableListOf<String>()
        messages.add("[§e${targetPlayer.name}§f 님의 $judgmentType 판정 시작!]")
        messages.add("§f - 주사위: §b$sides§f면체 주사위 §b$count§f개")

        results.forEachIndexed { index, roll ->
            messages.add("§f - ${index + 1}번째 굴린 값: §b$roll")
        }

        messages.add("§f - $statName : §a$playerStat")
        messages.add("§f - 주사위 총합: §b$totalRollResult")
        messages.add("§f - 최종 결과: §e$finalResult")



        // 메인 클래스의 함수를 호출하여 메시지를 시간 간격을 두고 전송
        TRPGPlugin.instance.sendDelayedMessages(messages, 1000L) // 1초 간격으로 메시지 전송


        return true
    }

    // 플레이어 스탯을 가져오는 더미 함수
    private fun getPlayerStat(player: org.bukkit.entity.Player, statName: String): Int? {
        val stat = TRPGPlugin.statManager.getStat(player)
        return when (statName.lowercase()) {
            "힘" -> stat.strength
            "민첩" -> stat.dexterity
            "지능" -> stat.intelligence
            "hp" -> stat.hp
            "mp" -> stat.mp
            else -> null // 해당 스탯이 없을 경우
        }
    }
}