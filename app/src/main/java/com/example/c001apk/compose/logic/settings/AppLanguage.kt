package com.example.c001apk.compose.logic.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.LocaleList
import androidx.annotation.StringRes
import com.example.c001apk.compose.c001Application
import java.util.Locale

/** 应用内语言：空标签跟随系统，其余按 BCP 47 标签覆盖。 */
object AppLanguage {
    const val SYSTEM = ""
    const val CHINESE = "zh"
    const val ENGLISH = "en"

    private const val PREFS = "app_language"
    private const val KEY = "tag"

    fun current(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, SYSTEM).orEmpty()

    fun set(context: Context, tag: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, tag).apply()
    }

    fun wrap(base: Context, tag: String = current(base)): Context {
        if (tag.isEmpty()) return base
        val locale = Locale.forLanguageTag(tag)
        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLocales(LocaleList(locale))
        return base.createConfigurationContext(configuration)
    }

    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String =
        wrap(c001Application).getString(resId, *formatArgs)

    fun Context.findActivity(): Activity? =
        generateSequence(this) { (it as? ContextWrapper)?.baseContext }
            .firstOrNull { it is Activity } as? Activity
}
