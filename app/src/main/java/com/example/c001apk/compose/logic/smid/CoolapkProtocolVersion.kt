package com.example.c001apk.compose.logic.smid

import com.example.c001apk.compose.constant.Constants
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat

data class CoolapkProtocolVersion(
    val name: String = Constants.VERSION_NAME,
    val code: String = Constants.VERSION_CODE,
    val api: String = Constants.API_VERSION,
)

internal fun UserPreferencesCompat.protocolVersion() = CoolapkProtocolVersion(versionName, versionCode, apiVersion)

/** Change protocol headers and invalidate pending registration results in one transaction. */
internal fun UserPreferencesCompat.withProtocolVersion(version: CoolapkProtocolVersion, customized: Boolean = true): UserPreferencesCompat {
    if (protocolVersion() == version) return this
    return copy(
        versionName = version.name,
        versionCode = version.code,
        apiVersion = version.api,
        userAgent = userAgent.replace("+CoolMarket/$versionName-$versionCode-", "+CoolMarket/${version.name}-${version.code}-"),
        deviceIdentityRevision = deviceIdentityRevision + 1,
        digitalUnionRefreshPending = true,
        protocolVersionCustomized = customized,
    )
}

/** Upgrade only known bundled defaults; deliberately configured versions stay unchanged. */
internal fun UserPreferencesCompat.upgradeDefaultProtocol(): UserPreferencesCompat =
    if (protocolVersionCustomized) this
    else if ((versionName == "16.1.3" && versionCode == "2604022" && apiVersion == "16") ||
        (versionName == "13.4.1" && versionCode == "2312121" && apiVersion == "13")) {
        withProtocolVersion(CoolapkProtocolVersion(), customized = false)
    } else this
