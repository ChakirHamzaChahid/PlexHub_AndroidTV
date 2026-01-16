package com.chakir.aggregatorhubplex.data

enum class ServerStatus { ONLINE, SLOW, OFFLINE, CHECKING }

data class ServerHealth(
    val name: String,
    val url: String,
    val latencyMs: Long = 0,
    val status: ServerStatus = ServerStatus.CHECKING,
    val isPreferred: Boolean = false
)