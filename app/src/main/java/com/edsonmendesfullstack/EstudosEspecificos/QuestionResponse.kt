package com.edsonmendesfullstack.EstudosEspecificos

import com.google.gson.annotations.SerializedName

// Este modelo mapeia o OBJETO JSON COMPLETO retornado pela sua API de perguntas
data class QuestionResponse(
    @SerializedName("count")
    val count: Int, // O número total de perguntas retornadas (2, no seu exemplo)

    // 🚨 A lista de perguntas está aninhada neste campo
    @SerializedName("perguntas")
    val questions: List<Question> // Usa o modelo Question.kt que já definimos
)