package com.edsonmendesfullstack.EstudosEspecificos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.edsonmendesfullstack.EstudosEspecificos.R
import com.edsonmendesfullstack.EstudosEspecificos.Theme

class ThemesAdapter(
    private val themes: List<Theme>,
    private val onThemeClicked: (Theme) -> Unit
) : RecyclerView.Adapter<ThemesAdapter.ThemeViewHolder>() {

    class ThemeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ID do TextView dentro do item_theme.xml (Criado anteriormente)
        val themeName: TextView = itemView.findViewById(R.id.tv_theme_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme, parent, false) // 🚨 item_theme.xml deve estar criado
        return ThemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val theme = themes[position]
        holder.themeName.text = theme.name

        holder.itemView.setOnClickListener {
            onThemeClicked(theme)
        }
    }

    override fun getItemCount() = themes.size
}