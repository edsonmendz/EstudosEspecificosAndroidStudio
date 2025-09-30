package com.edsonmendesfullstack.EstudosEspecificos

import com.google.gson.annotations.SerializedName

// Estrutura para mapear o JSON da API
data class Post(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String
    // Adicione outros campos conforme a estrutura real da sua API
)