package com.edsonmendesfullstack.EstudosEspecificos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.os.Build
import android.os.Parcelable

class SubjectsFragment : Fragment() {

    // Carrega a lista de matérias do Bundle
    private val subjects: List<Subject> by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Usa o método moderno (seguro a partir do API 33)
            arguments?.getParcelableArrayList(ARG_SUBJECTS, Subject::class.java) ?: emptyList()
        } else {
            // Usa o método antigo (funciona até o API 32), mas requer um cast
            @Suppress("DEPRECATION")
            arguments?.getParcelableArrayList<Subject>(ARG_SUBJECTS) ?: emptyList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_subjects, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_subjects)

        // Configuração: Lista vertical (uma coluna)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = SubjectsAdapter(subjects) { subject ->
            // AÇÃO DE CLIQUE: Delegada para a Activity
            (activity as? MainActivity)?.onSubjectSelected(subject)
        }
        recyclerView.adapter = adapter
    }

    companion object {
        private const val ARG_SUBJECTS = "subjects_list"
        fun newInstance(subjects: List<Subject>): SubjectsFragment {
            val fragment = SubjectsFragment()

            // 🚨 CORREÇÃO AQUI: Converter a lista para ArrayList<out Parcelable>
            val args = Bundle().apply {
                putParcelableArrayList(
                    ARG_SUBJECTS,
                    // Cria o ArrayList e aplica o cast na mesma linha
                    ArrayList(subjects) as ArrayList<out Parcelable>
                )
            }

            fragment.arguments = args
            return fragment
        }
    }
}