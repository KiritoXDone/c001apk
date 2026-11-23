package com.example.c001apk.compose.ui.component.cards

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.theme.cardBg
import com.example.c001apk.compose.util.DateUtils.fromToday
import com.example.c001apk.compose.util.Utils.getAppVersion
import com.example.c001apk.compose.util.noRippleClickable
import top.yukonga.miuix.kmp.basic.Button as FilledTonalButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Created by bggRGjQaUbCoE on 2024/6/12 */
@Composable
fun AppUpdateCard(
    modifier: Modifier = Modifier,
    data: HomeFeedResponse.Data,
    onViewApp: (String) -> Unit,
    onDownloadApk: (String, String, String, String, String) -> Unit,
) {

    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    ConstraintLayout(
        modifier =
            modifier
                .fillMaxWidth()
                .squircleSurface(cardBg(), 16.dp)
                .clickable {
                    onViewApp(data.packageName.orEmpty())
                }
                .padding(10.dp)
                .animateContentSize(),
    ) {
        val (icon, title, bit, version, time, size, log, download) = createRefs()

        CoilLoader(
            url = data.logo,
            modifier =
                Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).aspectRatio(1f).constrainAs(
                    icon,
                ) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                },
        )

        Text(
            text = data.title.orEmpty(),
            modifier =
                Modifier.padding(start = 10.dp).constrainAs(title) {
                    start.linkTo(icon.end)
                    top.linkTo(parent.top)
                },
        )

        Text(
            text =
                when (data.pkgBitType) {
                    1 -> stringResource(R.string.app_arch_32)
                    2,
                    3 -> stringResource(R.string.app_arch_64)
                    else -> EMPTY_STRING
                },
            modifier =
                Modifier.padding(start = 10.dp).constrainAs(bit) {
                    start.linkTo(title.end)
                    bottom.linkTo(title.bottom)
                    top.linkTo(title.top)
                },
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        Text(
            text =
                run {
                    if (data.localVersionName.isNullOrEmpty()) {
                        val ver = getAppVersion(context, data.packageName.orEmpty())
                        data.localVersionName = ver.first
                        data.localVersionCode = ver.second
                    }
                    "${data.localVersionName}(${data.localVersionCode}) > ${data.apkversionname}(${data.apkversioncode})"
                },
            modifier =
                Modifier.padding(horizontal = 10.dp).constrainAs(version) {
                    start.linkTo(icon.end)
                    top.linkTo(title.bottom)
                    end.linkTo(download.start)
                    width = Dimension.fillToConstraints
                },
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        Text(
            text = fromToday(data.lastupdate ?: 0),
            modifier =
                Modifier.padding(start = 10.dp).constrainAs(time) {
                    start.linkTo(icon.end)
                    top.linkTo(version.bottom)
                },
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        Text(
            text = data.apksize.orEmpty(),
            modifier =
                Modifier.padding(start = 10.dp).constrainAs(size) {
                    start.linkTo(time.end)
                    top.linkTo(version.bottom)
                },
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        Text(
            text = data.changelog.orEmpty(),
            modifier =
                Modifier.padding(start = 10.dp)
                    .constrainAs(log) {
                        start.linkTo(icon.end)
                        top.linkTo(time.bottom)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                    .noRippleClickable {
                        expanded = !expanded
                    },
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            maxLines = if (expanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis,
        )

        FilledTonalButton(
            onClick = {
                onDownloadApk(
                    data.packageName.orEmpty(),
                    data.id.orEmpty(),
                    data.title.orEmpty(),
                    data.apkversionname.orEmpty(),
                    data.apkversioncode.orEmpty(),
                )
            },
            modifier =
                Modifier.constrainAs(download) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                },
        ) {
            Text(text = stringResource(R.string.action_download))
        }
    }
}
