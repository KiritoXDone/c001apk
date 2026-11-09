package com.example.c001apk.compose.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.Gravity
import coil.drawable.CrossfadeDrawable
import com.example.c001apk.compose.util.dp


/*
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

// mod from https://github.com/klinker24/Android-BadgedImageView
class BadgedImageView(
    context: Context,
) : RoundedImageView(context) {
    private var badgeBoundsSet = false
    private var badge: Drawable? = null
    private var badgeWidth = 0
    private var badgeHeight = 0
    private var badgeGravity: Int = Gravity.END or Gravity.BOTTOM
    private var topStartBadge: Drawable? = null
    private var topStartBadgeWidth = 0
    private var topStartBadgeHeight = 0
    private var badgePadding: Int = 4.dp
    var colorPrimaryContainer: Int = Color.BLACK
    var colorOnPrimaryContainer: Int = Color.WHITE
    var isAnimationEnabled: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            val animation = imageAnimation() ?: return
            if (value && isAttachedToWindow && isShown) animation.start() else animation.stop()
        }

    private fun imageAnimation(): Animatable? =
        ((drawable as? CrossfadeDrawable)?.end ?: drawable) as? Animatable

    fun pauseAnimationIfNeeded() {
        if (!isAnimationEnabled) imageAnimation()?.stop()
    }

    fun setBadge(text: String) {
        val drawable = BadgeDrawable(text, colorPrimaryContainer, colorOnPrimaryContainer)
        setBadge(drawable, drawable.intrinsicWidth, drawable.intrinsicHeight)
    }

    fun setBadge(drawable: Drawable, width: Int, height: Int) {
        badge = drawable
        badgeWidth = width
        badgeHeight = height
        badgeBoundsSet = false
        invalidate()
    }

    fun setTopStartBadge(text: String) {
        val drawable = BadgeDrawable(text, colorPrimaryContainer, colorOnPrimaryContainer)
        topStartBadge = drawable
        topStartBadgeWidth = drawable.intrinsicWidth
        topStartBadgeHeight = drawable.intrinsicHeight
        badgeBoundsSet = false
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        // Coil 加载完成或恢复生命周期时会自动播放，绘制前同步列表的暂停状态。
        pauseAnimationIfNeeded()
        super.draw(canvas)
        if (!badgeBoundsSet && (badge != null || topStartBadge != null)) {
            layoutBadges()
        }
        badge?.draw(canvas)
        topStartBadge?.draw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (badge != null || topStartBadge != null) {
            layoutBadges()
        }
    }

    private fun layoutBadges() {
        badge?.layoutBadge(badgeGravity, badgeWidth, badgeHeight)
        topStartBadge?.layoutBadge(
            Gravity.START or Gravity.TOP,
            topStartBadgeWidth,
            topStartBadgeHeight,
        )
        badgeBoundsSet = true
    }

    private fun Drawable.layoutBadge(gravity: Int, badgeWidth: Int, badgeHeight: Int) {
        val badgeBounds = bounds
        Gravity.apply(
            gravity,
            badgeWidth,
            badgeHeight,
            Rect(0, 0, this@BadgedImageView.width, this@BadgedImageView.height),
            badgePadding,
            badgePadding,
            badgeBounds,
        )
        bounds = badgeBounds
    }
}
