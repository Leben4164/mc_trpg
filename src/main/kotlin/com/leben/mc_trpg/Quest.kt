package com.leben.mc_trpg

import java.util.UUID

data class Quest(
    val uniqueId: UUID,
    val requesterName: String,
    val targetName: String,
    val title: String,
    val description: String,
    val reward: String
)

