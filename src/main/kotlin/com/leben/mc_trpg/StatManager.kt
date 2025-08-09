package com.leben.mc_trpg

import com.leben.mc_trpg.data.CharacterStat
import com.google.gson.GsonBuilder
import org.bukkit.entity.Player
import java.io.File

class StatManager(private val dataFolder: File) {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val statFile = File(dataFolder, "stats.json")
    private val playerStats = mutableMapOf<String, CharacterStat>() // 닉네임 -> 스탯 정보

    init {
        // 플러그인이 시작될 때 파일에서 스탯을 불러옴
        loadStats()
    }

    fun getStat(player: Player): CharacterStat {
        // 맵에 스탯이 없으면 새로 생성
        return playerStats.getOrPut(player.name) {
            CharacterStat(player.name)
        }
    }

    fun saveStatInFile() {
        val json = gson.toJson(playerStats.values)
        statFile.writeText(json)
    }

    fun saveStat(player: Player, stat: CharacterStat) {
        playerStats[player.name] = stat
        saveStatInFile()
    }

    private fun loadStats() {
        if (!statFile.exists()) {
            // 파일이 없으면 새로 생성
            statFile.parentFile.mkdirs()
            statFile.createNewFile()
            return
        }

        // 파일에서 JSON 데이터를 읽어와서 Map에 저장
        val json = statFile.readText()
        val stats = gson.fromJson(json, Array<CharacterStat>::class.java)?.toList() ?: listOf()
        stats.forEach { playerStats[it.playerName] = it }
    }
}