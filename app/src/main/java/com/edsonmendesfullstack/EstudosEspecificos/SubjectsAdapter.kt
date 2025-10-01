package com.edsonmendesfullstack.EstudosEspecificos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.edsonmendesfullstack.EstudosEspecificos.R
 // Ajuste o import do seu modelo

class SubjectsAdapter(
    private val subjects: List<Subject>,
    private val onSubjectClicked: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder>() {

    class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subjectName: TextView = itemView.findViewById(R.id.tv_subject_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position]
        holder.subjectName.text = subject.name

        // AÇÃO DE CLIQUE: Passa a Matéria clicada
        holder.itemView.setOnClickListener {
            onSubjectClicked(subject)
        }
    }

    override fun getItemCount() = subjects.size
}