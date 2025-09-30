package com.edsonmendesfullstack.EstudosEspecificos

import com.google.gson.annotations.SerializedName

data class Theme(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nome")
    val name: String // Mantenha a nomenclatura em inglês para variáveis internas (opcional, mas boa prática)
)