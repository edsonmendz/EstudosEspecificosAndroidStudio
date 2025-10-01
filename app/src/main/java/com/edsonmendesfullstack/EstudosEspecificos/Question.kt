package com.edsonmendesfullstack.EstudosEspecificos

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Question(
    @SerializedName("id")
    val id: Int,
    @SerializedName("materia_id")
    val subjectId: Int, // Agora sabemos que é Int
    @SerializedName("texto_pergunta")
    val questionText: String,
    @SerializedName("resposta_correta")
    val correctAnswer: String,
    @SerializedName("opcoes_erradas")
    val incorrectOptions: List<String>, // Array de 3 Strings

    // Adicionar um campo para rastrear a resposta do usuário
    var userSelectedAnswer: String? = null // Resposta do usuário (null se não respondeu)
) : Parcelable