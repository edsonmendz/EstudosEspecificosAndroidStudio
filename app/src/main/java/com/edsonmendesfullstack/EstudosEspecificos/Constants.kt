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

object AdsKeys {
    const val INTERSTITIAL_ID = "ca-app-pub-3453425099228667/8428258817" // Seu ID real
    const val LAUNCH_COUNT_TRIGGER = 2 // Mostrar anúncio a cada 5 inícios
}