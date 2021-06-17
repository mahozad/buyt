package com.pleon.buyt.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.pleon.buyt.ui.fragment.PREF_LANG
import java.util.*

fun setLocale(context: Context): Context {
    val locale = getCurrentLocale(context)
    return setLocale(context, locale)
}

fun setLocale(context: Context, newLocale: Locale): Context {
    Locale.setDefault(newLocale)
    val config = Configuration(context.resources.configuration)
    config.setLocale(newLocale)
    val cxt = context.createConfigurationContext(config)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
    return cxt
}

fun getCurrentLocale(context: Context): Locale {
    val lang = getDefaultSharedPreferences(context).getString(PREF_LANG, "auto")!!
    return if (lang == "auto") Resources.getSystem().configuration.locale else Locale(lang)
}
