package com.example.c001apk.compose.ui.feed.reply

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.content.IntentCompat
import com.example.c001apk.compose.logic.model.VOTE_COMMENT_TYPE
import com.example.c001apk.compose.util.CookieUtil
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class VoteCommentTarget(
    val feedId: String,
    val voteId: String,
    val anonymous: Boolean,
    val accountRevision: Long,
) : Parcelable {
    fun isCurrentAccount(): Boolean = CookieUtil.accountSession.let {
        it.isLogin && it.revision == accountRevision
    }

    fun resultIntent(): Intent = Intent().putExtra(EXTRA, this)

    fun editorIntent(context: Context): Intent =
        Intent(context, ReplyActivity::class.java)
            .putExtra("type", VOTE_COMMENT_TYPE)
            .putExtra(EXTRA, this)

    companion object {
        private const val EXTRA = "vote_comment_target"

        fun fromIntent(intent: Intent): VoteCommentTarget? =
            IntentCompat.getParcelableExtra(intent, EXTRA, VoteCommentTarget::class.java)
                ?.takeIf { it.feedId.isNotBlank() && it.voteId.isNotBlank() }
    }
}
