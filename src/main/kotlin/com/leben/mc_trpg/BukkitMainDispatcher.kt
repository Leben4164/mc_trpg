package com.leben.mc_trpg

import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

// 마인크래프트 메인 스레드에서 코드를 실행하기 위한 디스패처
object BukkitMainDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        // 작업을 서버의 스케줄러로 보냄
        Bukkit.getScheduler().runTask(TRPGPlugin.instance, block)
    }
}