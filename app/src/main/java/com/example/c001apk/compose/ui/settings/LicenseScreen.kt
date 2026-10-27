package com.example.c001apk.compose.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixTopBar as TopAppBar
import com.example.c001apk.compose.ui.component.settings.BasicListItem
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.util.openInBrowser

/** Created by bggRGjQaUbCoE on 2024/6/2 */
@Composable
fun LicenseScreen(
    onBackClick: () -> Unit,
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onBackClick)
                },
                title = "Open Source License",
            )
        },
    ) { paddingValues ->
        val topBarListState = rememberLazyListState()
        TopBarScrollEffect { topBarListState.animateScrollToItem(0) }
        LazyColumn(
            state = topBarListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
        ) {
            items(
                items = licenseList,
                key = { it.url },
            ) { item ->
                LicenseRow(item = item)
            }
        }
    }
}

private val licenseList =
    listOf(
        LicenseItem(
            "commonmark-java contributors",
            "CommonMark",
            "https://github.com/commonmark/commonmark-java",
            LicenseType.BSD3,
        ),
        LicenseItem(
            "daimiaopeng",
            "coolapk-desktop protocol compatibility",
            "https://github.com/daimiaopeng/coolapk-desktop",
            LicenseType.MIT,
        ),
        LicenseItem(
            "Google LLC",
            "Flutter profile icon glyphs (local vectors)",
            "https://creativecommons.org/licenses/by/4.0/",
            LicenseType.CCBY4,
        ),
        LicenseItem(
            "Google",
            "Jetpack Compose",
            "https://github.com/androidx/androidx",
            LicenseType.Apache2,
        ),
        LicenseItem(
            "JetBrains",
            "Kotlin",
            "https://github.com/JetBrains/kotlin",
            LicenseType.Apache2,
        ),
        LicenseItem(
            "Google",
            "Accompanist",
            "https://github.com/google/accompanist",
            LicenseType.Apache2,
        ),
        LicenseItem(
            "YuKongA / compose-miuix-ui contributors",
            "miuix (UI, Preference, Blur, Icons)",
            "https://github.com/compose-miuix-ui/miuix",
            LicenseType.Apache2,
        ),
        LicenseItem(
            "Google",
            "AOSP community action icons (local vectors)",
            "https://github.com/google/material-design-icons",
            LicenseType.Apache2,
        ),
        LicenseItem(
            "square",
            "okhttp",
            "https://github.com/square/okhttp",
            LicenseType.Apache2,
        ),
        LicenseItem(
            "square",
            "retrofit",
            "https://github.com/square/retrofit",
            LicenseType.Apache2,
        ),
        LicenseItem(
            "mikaelzero",
            "mojito",
            "https://github.com/mikaelzero/mojito",
            LicenseType.Apache2,
        ),
        LicenseItem(
            "plain-dev",
            "NineGridImageView",
            "https://github.com/plain-dev/NineGridImageView",
            LicenseType.MIT,
        ),
        LicenseItem(
            "coil-kt",
            "coil",
            "https://github.com/coil-kt/coil",
            LicenseType.Apache2,
        ),
        LicenseItem(
            "wasabeef",
            "transformers",
            "https://github.com/wasabeef/transformers",
            LicenseType.Apache2,
        ),
        LicenseItem(
            "jeremyh",
            "jBCrypt",
            "https://github.com/jeremyh/jBCrypt",
            LicenseType.Apache2,
        ),
        LicenseItem(
            "jhy",
            "jsoup",
            "https://github.com/jhy/jsoup",
            LicenseType.MIT,
        ),
        LicenseItem(
            "zhanghai",
            "AppIconLoader",
            "https://github.com/zhanghai/AppIconLoader",
            LicenseType.MIT,
        ),
        LicenseItem(
            "onebone",
            "compose-collapsing-toolbar",
            "https://github.com/onebone/compose-collapsing-toolbar",
            LicenseType.MIT,
        ),
        LicenseItem(
            "AlexMofer",
            "SmoothInputLayout",
            "https://github.com/AlexMofer/SmoothInputLayout",
            LicenseType.Apache2,
        ),
        LicenseItem(
            "jordond",
            "MaterialKolor",
            "https://github.com/jordond/MaterialKolor",
            LicenseType.MIT,
        ),
    )

data class LicenseItem(
    val author: String,
    val name: String,
    val url: String,
    val type: LicenseType,
)

enum class LicenseType {
    Apache2,
    MIT,
    GPL3,
    CCBY4,
    BSD3,
}

private fun getLicense(type: LicenseType): String =
    when (type) {
        LicenseType.BSD3 -> "BSD 3-Clause License"
        LicenseType.CCBY4 -> "Creative Commons Attribution 4.0 International"
        LicenseType.Apache2 -> "Apache Software License 2.0"
        LicenseType.MIT -> "MIT License"
        LicenseType.GPL3 -> "GNU general public license Version 3"
    }

@Composable
fun LicenseRow(item: LicenseItem) {
    val context = LocalContext.current
    BasicListItem(
        headlineText = "${item.name} - ${item.author}",
        supportingText = "${item.url}\n${getLicense(item.type)}",
    ) {
        context.openInBrowser(item.url)
    }
}
