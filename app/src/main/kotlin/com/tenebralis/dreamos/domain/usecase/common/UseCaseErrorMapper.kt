package com.tenebralis.dreamos.domain.usecase.common

object UseCaseErrorMapper {

    fun toMessage(error: Throwable): String {
        val message = error.message.orEmpty()
        val lowered = message.lowercase()
        return when {
            "当前未登录" in message || "not authenticated" in lowered -> "请先登录后再操作"
            "worlds_user_name_unique" in lowered -> "该世界名称已存在，请更换名称"
            "identities_world_name_unique" in lowered -> "该世界下身份名称已存在，请更换名称"
            "save_identity_slot_unique" in lowered -> "该身份下此存档槽位已存在"
            "duplicate key" in lowered -> "记录已存在，请检查名称或槽位是否重复"
            "row-level security" in lowered || "rls" in lowered || "permission denied" in lowered ->
                "无权限访问该数据，请确认当前账号和登录状态"
            "timeout" in lowered || "failed to connect" in lowered || "network" in lowered ->
                "网络连接异常，请稍后重试"
            message.isNotBlank() -> message
            else -> "操作失败，请稍后重试"
        }
    }
}
