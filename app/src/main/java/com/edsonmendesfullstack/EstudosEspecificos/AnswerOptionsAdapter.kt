package com.edsonmendesfullstack.EstudosEspecificos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.edsonmendesfullstack.EstudosEspecificos.R
import com.edsonmendesfullstack.EstudosEspecificos.Question
import kotlin.random.Random

class AnswerOptionsAdapter(
    private val question: Question,
    private val onAnswerSelected: (String) -> Unit
) : RecyclerView.Adapter<AnswerOptionsAdapter.AnswerViewHolder>() {

    // 🚨 Combina a resposta correta com as erradas e embaralha a lista
    private val answers: List<String> = (question.incorrectOptions + question.correctAnswer)
        .shuffled(Random(question.id.toLong())) // Usa o ID da questão como semente para embaralhamento consistente

    // 🚨 Armazena o item selecionado (String da resposta)
    private var selectedAnswer: String? = question.userSelectedAnswer

    fun updateSelection(selected: String) {
        // Encontra a posição antiga e a nova
        val oldPosition = answers.indexOf(selectedAnswer)
        val newPosition = answers.indexOf(selected)

        selectedAnswer = selected

        // Notifica o Adapter para redesenhar APENAS os itens afetados
        if (oldPosition != -1) notifyItemChanged(oldPosition)
        if (newPosition != -1) notifyItemChanged(newPosition)

        // Informa a Activity qual foi a resposta selecionada
        onAnswerSelected(selected)
    }

    class AnswerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOption: TextView = itemView.findViewById(R.id.tvAnswerOption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_answer_option, parent, false)
        return AnswerViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) {
        val answer = answers[position]
        holder.tvOption.text = answer

        // Aplica o estado visual: verifica se a resposta atual é a selecionada
        val isSelected = answer == selectedAnswer
        holder.tvOption.isSelected = isSelected

        holder.itemView.setOnClickListener {
            // Se o usuário clicar, atualiza a seleção
            updateSelection(answer)
        }
    }

    override fun getItemCount() = answers.size
}