package com.edsonmendesfullstack.EstudosEspecificos

import android.os.Parcelable // 🚨 Necessário para a interface
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize // 🚨 Necessário para a anotação (Requer plugin no Gradle)

// 🚨 ADICIONE @Parcelize e : Parcelable
@Parcelize
data class Subject(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nome")
    val name: String,
    @SerializedName("tema_id") // Se este for o ID do Tema pai
    val themeId: Int
) : Parcelable // 🚨 Implementação da interface