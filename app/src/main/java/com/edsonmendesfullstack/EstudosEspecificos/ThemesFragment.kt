package com.edsonmendesfullstack.EstudosEspecificos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.edsonmendesfullstack.EstudosEspecificos.R
import com.edsonmendesfullstack.EstudosEspecificos.Theme
import com.edsonmendesfullstack.EstudosEspecificos.ThemesAdapter

class ThemesFragment : Fragment() {

    // Carrega a lista de temas do Bundle, garantindo que Theme seja Parcelable
    private val themes: List<Theme> by lazy {
        arguments?.getParcelableArrayList(ARG_THEMES) ?: emptyList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 🚨 fragment_themes.xml deve ser o layout com o RecyclerView
        return inflater.inflate(R.layout.fragment_themes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_themes)

        // Configuração do Grid de 2 colunas
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        val adapter = ThemesAdapter(themes) { theme ->
            // AÇÃO DE CLIQUE: Delegada para a Activity
            (activity as? MainActivity)?.onThemeSelected(theme)
        }
        recyclerView.adapter = adapter
    }

    companion object {
        private const val ARG_THEMES = "themes_list"
        fun newInstance(themes: List<Theme>): ThemesFragment {
            val fragment = ThemesFragment()
            val args = Bundle().apply {
                putParcelableArrayList(ARG_THEMES, ArrayList(themes))
            }
            fragment.arguments = args
            return fragment
        }
    }
}