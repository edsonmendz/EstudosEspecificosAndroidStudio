package com.edsonmendesfullstack.EstudosEspecificos

import com.google.gson.annotations.SerializedName

data class Subject(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nome")
    val name: String,
    @SerializedName("materia_id") // Se este for o ID do Tema pai
    val themeId: Int
)