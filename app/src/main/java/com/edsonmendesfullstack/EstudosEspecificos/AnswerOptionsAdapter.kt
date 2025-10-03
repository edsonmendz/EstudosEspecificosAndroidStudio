package com.edsonmendesfullstack.EstudosEspecificos

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.edsonmendesfullstack.EstudosEspecificos.R
import com.edsonmendesfullstack.EstudosEspecificos.Question
import kotlin.random.Random
import androidx.core.content.ContextCompat // 🚨 CORREÇÃO 1: Adicionado ContextCompat
import com.edsonmendesfullstack.EstudosEspecificos.databinding.ItemAnswerOptionBinding // 🚨 CORREÇÃO 2: Adicione o import do View Binding

class AnswerOptionsAdapter(
    private val question: Question,
    private val onAnswerSelected: (String) -> Unit,
    private val isFinished: Boolean
) : RecyclerView.Adapter<AnswerOptionsAdapter.ViewHolder>() {

    // 🚨 CORREÇÃO 3: Mantemos o ViewHolder que usa Binding
    inner class ViewHolder(val binding: ItemAnswerOptionBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Combina a resposta correta com as erradas e embaralha a lista
    private val answers: List<String> = (question.incorrectOptions + question.correctAnswer)
        .shuffled(Random(question.id.toLong()))

    // Armazena o item selecionado (String da resposta)
    private var selectedAnswer: String? = question.userSelectedAnswer


    fun updateSelection(selected: String) {
        // ... (lógica de atualização permanece inalterada) ...
        val oldPosition = answers.indexOf(selectedAnswer)
        val newPosition = answers.indexOf(selected)

        selectedAnswer = selected

        if (oldPosition != -1) notifyItemChanged(oldPosition)
        if (newPosition != -1) notifyItemChanged(newPosition)

        onAnswerSelected(selected)
    }

    // ❌ CORREÇÃO 4: REMOVA a classe AnswerViewHolder antiga, pois não a usaremos mais
    /* class AnswerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOption: TextView = itemView.findViewById(R.id.tvAnswerOption)
    }
    */

    // 🚨 CORREÇÃO 5: Atualiza para usar o View Binding e retornar o ViewHolder correto
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAnswerOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // 🚨 CORREÇÃO 6: Atualiza o onBindViewHolder com a lógica de seleção normal (não finalizada)
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val answer = answers[position] // Use 'answers' que é a lista embaralhada

        holder.binding.tvAnswerOption.text = answer

        // -------------------------------------------------------------
        // LÓGICA DE CORREÇÃO VISUAL (Se o Quiz Terminou)
        // -------------------------------------------------------------
        if (isFinished) {
            holder.binding.root.isClickable = false

            val context = holder.itemView.context
            val colorCorrect = ContextCompat.getColor(context, R.color.green_correct)
            val colorWrong = ContextCompat.getColor(context, R.color.red_wrong)
            val colorSelected = ContextCompat.getColor(context, R.color.purple_200)

            when {
                // A) Resposta CORRETA
                answer == question.correctAnswer -> {
                    holder.binding.root.setBackgroundColor(colorCorrect)
                }
                // B) Resposta MARCADA e ERRADA
                answer == question.userSelectedAnswer && answer != question.correctAnswer -> {
                    holder.binding.root.setBackgroundColor(colorSelected)
                }
                // C) Resposta não marcada / Outra opção errada
                else -> {
                    holder.binding.root.setBackgroundColor(colorWrong)
                }
            }
        }
        // -------------------------------------------------------------
        // LÓGICA DE SELEÇÃO NORMAL (Se o Quiz NÃO Terminou)
        // -------------------------------------------------------------
        else {
            val isSelected = answer == selectedAnswer

            // 🚨 Use o estado 'isSelected' para aplicar o background de seleção.
            // Aqui você deve usar um setBackgroundResource para o 'selected'/'default'
            holder.binding.root.setOnClickListener {
                updateSelection(answer)
            }

            // Exemplo de como aplicar o estilo de seleção (depende do seu XML)
            holder.binding.root.isSelected = isSelected
        }
    }

    override fun getItemCount() = answers.size
}