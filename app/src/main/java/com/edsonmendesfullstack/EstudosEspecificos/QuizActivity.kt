package com.edsonmendesfullstack.EstudosEspecificos

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.edsonmendesfullstack.EstudosEspecificos.databinding.ActivityQuizBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import androidx.fragment.app.commit
import android.view.View

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding

    // Lista para armazenar as perguntas e as respostas do usuário
    private var questions: MutableList<Question> = mutableListOf()
    private var currentQuestionIndex: Int = 0 // Índice da pergunta atual

    // ID da matéria recebido via Intent
    private val subjectId: Int by lazy { intent.getIntExtra("SUBJECT_ID", -1) }

    // Você deve definir a quantidade de perguntas a serem carregadas (ex: 10)
    private val questionQuantity = 10

    private fun showLoading() {
        // Usa o FragmentManager para exibir o LoadingFragment
        supportFragmentManager.commit {
            // Usa REPLACE para garantir que qualquer coisa que estivesse lá seja removida
            // O conteúdo principal está por baixo.
            replace(binding.loadingFragmentContainer.id, LoadingFragment())
        }
    }

    private fun hideLoading() {
        val fragment = supportFragmentManager.findFragmentById(binding.loadingFragmentContainer.id)

        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commitNow()
        }

        // 🚨 Plano B: Forçar o FrameLayout a sumir
        binding.loadingFragmentContainer.visibility = View.GONE
        // Certifique-se de importar android.view.View
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoading()

        // 1. Inicializa Ads
        MobileAds.initialize(this) {}
        binding.adViewBannerQuiz.loadAd(AdRequest.Builder().build())

        // 2. Carrega as perguntas
        if (subjectId != -1) {
            loadQuestionsAsync()
        } else {
            Toast.makeText(this, "Erro: ID da matéria não encontrado.", Toast.LENGTH_LONG).show()
            finish()
        }

        // 3. Configura os botões de navegação
        setupNavigationButtons()
    }


    // -------------------------------------------------------------------------
    // 🚨 BLOCKS 1 & 2: EXIBIÇÃO DA PERGUNTA E RESPOSTAS
    // -------------------------------------------------------------------------
    private fun displayQuestion(question: Question) {
        // Bloco 1: Título e Texto
        binding.tvQuestionNumber.text = getString(
            R.string.question_number_format, // Defina este recurso de string: "Questão %1$d de %2$d"
            currentQuestionIndex + 1,
            questions.size
        )
        binding.tvQuestionText.text = question.questionText

        // Bloco 2: Respostas (RecyclerView)
        val adapter = AnswerOptionsAdapter(question) { selectedAnswer ->
            // Atualiza o estado da pergunta com a resposta do usuário
            questions[currentQuestionIndex].userSelectedAnswer = selectedAnswer
        }

        binding.rvAnswerOptions.layoutManager = LinearLayoutManager(this)
        binding.rvAnswerOptions.adapter = adapter
    }

    // -------------------------------------------------------------------------
    // 🚨 BLOCO 3: BOTÕES DE NAVEGAÇÃO
    // -------------------------------------------------------------------------
    private fun setupNavigationButtons() {
        binding.btnNext.setOnClickListener { navigateQuestion(1) }
        binding.btnPrevious.setOnClickListener { navigateQuestion(-1) }
        binding.btnFinish.setOnClickListener { showFinishConfirmation() }
    }

    private fun navigateQuestion(direction: Int) {
        val newIndex = currentQuestionIndex + direction

        if (questions.isEmpty()) return

        // Lógica Circular de Navegação
        if (newIndex >= questions.size) {
            currentQuestionIndex = 0 // Volta para a primeira
        } else if (newIndex < 0) {
            currentQuestionIndex = questions.size - 1 // Volta para a última
        } else {
            currentQuestionIndex = newIndex
        }

        displayQuestion(questions[currentQuestionIndex])
    }

    private fun showFinishConfirmation() {
        // Confirmação para Finalizar e Corrigir
        AlertDialog.Builder(this)
            .setTitle("Finalizar Quiz")
            .setMessage("Tem certeza que deseja finalizar e ver seu resultado? Você não poderá mais responder.")
            .setPositiveButton("Finalizar") { dialog, _ ->
                evaluateQuiz()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun evaluateQuiz() {
        var correctCount = 0
        questions.forEach { question ->
            if (question.userSelectedAnswer == question.correctAnswer) {
                correctCount++
            }
        }

        val totalQuestions = questions.size
        val resultMessage = "Parabéns! Você acertou $correctCount de $totalQuestions perguntas."

        // Exibe o resultado e fecha a Activity (ou inicia a Activity de Resultado)
        AlertDialog.Builder(this)
            .setTitle("Resultado")
            .setMessage(resultMessage)
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    // -------------------------------------------------------------------------
    // 🚨 CARREGAMENTO DE DADOS
    // -------------------------------------------------------------------------
    private fun loadQuestionsAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Endpoint: https://eeappads.onrender.com/perguntas/3?qtd=1
                val response: QuestionResponse = RetrofitClient.instance.getQuestionsBySubject(
                    subjectId = subjectId,
                    quantity = questionQuantity // A quantidade que você definiu
                )

                withContext(Dispatchers.Main) {
                    if (response.questions.isNotEmpty()) {
                        questions.addAll(response.questions)
                        // ✅ AQUI ESCONDEMOS O LOADING E MOSTRAMOS O CONTEÚDO
                        hideLoading()
                        displayQuestion(questions.first())
                    } else {
                        // Tratar lista vazia
                        withContext(Dispatchers.Main) {
                            hideLoading() // Esconde em caso de lista vazia
                            Toast.makeText(this@QuizActivity, "Nenhuma pergunta encontrada.", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("API_QUIZ_ERROR", "Falha ao carregar perguntas: ${e.message}", e)

                withContext(Dispatchers.Main) {
                    hideLoading() // Esconde em caso de erro de rede
                    Toast.makeText(this@QuizActivity, "Erro de rede ao carregar o quiz.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
}