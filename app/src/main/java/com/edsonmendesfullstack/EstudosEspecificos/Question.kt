package com.edsonmendesfullstack.EstudosEspecificos

import com.google.gson.annotations.SerializedName

data class Question(
    @SerializedName("id")
    val id: Int,
    @SerializedName("materia_id")
    val subjectId: Int,
    @SerializedName("texto_pergunta")
    val questionText: String,
    @SerializedName("resposta_correta")
    val correctAnswer: String,

    // 🚨 ATENÇÃO: Array de Strings no JSON deve ser mapeado como List<String> no Kotlin
    @SerializedName("opcoes_erradas")
    val wrongOptions: List<String>,

    // Opcional, mas recomendado: Mapeie o campo mesmo que não use.
    // Isso evita que o GSON/Retrofit falhe se o campo estiver presente.
    @SerializedName("created_at")
    val createdAt: String? // Use String? se for uma data/hora ou Nullable
)