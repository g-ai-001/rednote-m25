package app.rednote_m25.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import app.rednote_m25.data.repository.AppLocale
import java.util.Locale

object LocaleManager {

    fun applyLocale(context: Context, locale: AppLocale): Context {
        val newLocale = when (locale) {
            AppLocale.SYSTEM -> Locale.getDefault()
            AppLocale.ZH -> Locale.SIMPLIFIED_CHINESE
            AppLocale.EN -> Locale.ENGLISH
        }

        val configuration = Configuration(context.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(newLocale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = newLocale
        }

        configuration.setLayoutDirection(newLocale)

        return context.createConfigurationContext(configuration)
    }

    fun getLocaleDisplayName(locale: AppLocale): String {
        return when (locale) {
            AppLocale.SYSTEM -> "跟随系统"
            AppLocale.ZH -> "简体中文"
            AppLocale.EN -> "English"
        }
    }
}