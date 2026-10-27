package com.exemple.reveil.data

data class Alarm(
    val id: String = "",
    val name: String = "",
    val time: String = "07:30",
    val ownerId: String = "",
    val enabled: Boolean = true,
    val participants: List<String> = emptyList(),
    val status: String = "IDLE" // "IDLE", "RINGING", "SNOOZED", "DISMISSED"
)
