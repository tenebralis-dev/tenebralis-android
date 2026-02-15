package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.RememberedCredential
import kotlinx.coroutines.flow.Flow

/**
 * 管理本地“记住我”凭证存储。
 */
interface RememberedCredentialRepository {

    /** 监听当前保存的凭证（未保存返回 null） */
    fun observeRememberedCredential(): Flow<RememberedCredential?>

    /** 保存凭证并标记 remember me 开启 */
    suspend fun saveRememberedCredential(email: String, password: String)

    /** 清除本地凭证并关闭 remember me */
    suspend fun clearRememberedCredential()
}
