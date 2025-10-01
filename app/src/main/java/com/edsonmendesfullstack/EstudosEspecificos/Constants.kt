package com.edsonmendesfullstack.EstudosEspecificos

object PrefsKeys {
    // Nome do arquivo de SharedPreferences
    const val PREFS_FILE = "app_prefs"

    // Chaves das preferências
    const val QUESTION_QUANTITY = "question_quantity"
    const val LAUNCH_COUNT = "launch_count"
    const val LAST_AD_TIME = "last_ad_time"

    // Valores padrão
    const val DEFAULT_QUESTION_QUANTITY = 10
    const val DEFAULT_AD_TIME = 0L // 0L para timestamps/contadores de tempo
}