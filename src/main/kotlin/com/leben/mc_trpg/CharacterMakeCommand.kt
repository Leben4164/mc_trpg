package com.leben.mc_trpg

import com.leben.mc_trpg.data.CharacterStat
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.random.Random

class CharacterMakeCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§c이 명령어는 플레이어만 사용할 수 있습니다.")
            return false
        }

        // 스탯 무작위 생성 로직
        val playerStat = CharacterStat(
            playerName = sender.name,
            strength = Random.nextInt(3, 19),      // 3d6 주사위를 굴리는 효과
            dexterity = Random.nextInt(3, 19),
            intelligence = Random.nextInt(3, 19),
            luck = Random.nextInt(3, 19),
            hp = Random.nextInt(15, 31),             // 기본 체력 무작위
            mp = Random.nextInt(10, 21),             // 기본 마나 무작위
        )

        // StatManager를 통해 스탯 저장
        TRPGPlugin.statManager.saveStat(sender, playerStat)

        sender.sendMessage("§a[캐릭터 생성]§f 스탯이 무작위로 결정되었습니다!")
        sender.sendMessage("§f체력: §c${playerStat.hp} §f마나: §9${playerStat.mp}")
        sender.sendMessage("§f근력: §e${playerStat.strength} §f재주: §e${playerStat.dexterity}")
        sender.sendMessage("§f지능: §e${playerStat.intelligence} §f행운: §e${playerStat.luck}")

        return true
    }
}