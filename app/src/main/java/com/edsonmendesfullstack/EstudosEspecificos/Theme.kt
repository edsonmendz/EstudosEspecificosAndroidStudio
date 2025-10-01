// Theme.kt

package com.edsonmendesfullstack.EstudosEspecificos // Seu pacote de modelos

import android.os.Parcelable // 🚨 IMPORT NECESSÁRIO para a interface
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize // 🚨 IMPORT NECESSÁRIO para a anotação

@Parcelize // 🚨 A anotação
data class Theme(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nome")
    val name: String
) : Parcelable // 🚨 A interface