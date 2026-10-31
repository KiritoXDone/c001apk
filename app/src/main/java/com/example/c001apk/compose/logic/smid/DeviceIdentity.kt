package com.example.c001apk.compose.logic.smid

import android.os.Build
import com.example.c001apk.compose.constant.Constants
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.util.TokenDeviceUtils.encode
import java.util.Base64

/** Change only the DID in an existing device header; initialize missing fields from this device. */
fun UserPreferencesCompat.withDigitalUnionId(id: String): UserPreferencesCompat {
    val maker = manufacturer.ifBlank { Build.MANUFACTURER.orEmpty() }
    val deviceBrand = brand.ifBlank { Build.BRAND.orEmpty() }
    val deviceModel = model.ifBlank { Build.MODEL.orEmpty() }
    val build = buildNumber.ifBlank { Build.ID.orEmpty() }
    val release = androidVersion.ifBlank { Build.VERSION.RELEASE.orEmpty() }
    val migrateVersion =
        versionName.isBlank() || (!protocolVersionCustomized && versionName == "13.4.1" && versionCode == "2312121" && apiVersion == "13")
    val officialVersion = if (migrateVersion) Constants.VERSION_NAME else versionName
    val officialCode = if (migrateVersion) Constants.VERSION_CODE else versionCode
    val original = runCatching {
        String(Base64.getDecoder().decode(xAppDevice.reversed()), Charsets.UTF_8)
    }
        .getOrNull()
    require(xAppDevice.isBlank() || original?.split(';')?.size == 9) { "现有设备参数无法读取，请先恢复本机参数" }
    val header =
        if (original != null && original.split(';').size == 9) "$id;${original.substringAfter(';')}"
        else "$id; ; ; ; $maker; $deviceBrand; $deviceModel; $build; null"
    return copy(
        szlmId = id,
        manufacturer = maker,
        brand = deviceBrand,
        model = deviceModel,
        buildNumber = build,
        androidVersion = release,
        sdkInt = sdkInt.ifBlank { Build.VERSION.SDK_INT.toString() },
        versionName = officialVersion,
        versionCode = officialCode,
        apiVersion = if (migrateVersion) Constants.API_VERSION else apiVersion,
        xAppDevice = encode(header),
        userAgent =
            if (userAgent.isNotBlank() && !migrateVersion) userAgent
            else
                "Dalvik/2.1.0 (Linux; U; Android $release; $deviceModel $build) (#Build; $deviceBrand; $deviceModel; $build; $release) +CoolMarket/$officialVersion-$officialCode-${Constants.MODE}",
    )
}

fun UserPreferencesCompat.withCurrentDevice(): UserPreferencesCompat =
    copy(
            manufacturer = Build.MANUFACTURER.orEmpty(),
            brand = Build.BRAND.orEmpty(),
            model = Build.MODEL.orEmpty(),
            buildNumber = Build.ID.orEmpty(),
            sdkInt = Build.VERSION.SDK_INT.toString(),
            androidVersion = Build.VERSION.RELEASE.orEmpty(),
            userAgent = "",
            xAppDevice = "",
        )
        .withDigitalUnionId(szlmId)
