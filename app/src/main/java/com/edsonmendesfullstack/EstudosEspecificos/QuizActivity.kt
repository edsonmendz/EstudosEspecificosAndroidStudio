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
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback


class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding

    // Lista para armazenar as perguntas e as respostas do usuário
    private var questions: MutableList<Question> = mutableListOf()
    private var currentQuestionIndex: Int = 0 // Índice da pergunta atual

    // ID da matéria recebido via Intent
    private val subjectId: Int by lazy { intent.getIntExtra("SUBJECT_ID", -1) }
    private var mInterstitialAd: InterstitialAd? = null
    private var isQuizFinished: Boolean = false

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
        loadInterstitialAd()

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
        val adapter = AnswerOptionsAdapter(
            question,
            onAnswerSelected = { selectedAnswer ->
                // Atualiza o estado da pergunta com a resposta do usuário
                questions[currentQuestionIndex].userSelectedAnswer = selectedAnswer
            },
            isFinished = isQuizFinished // 🚨 PASSA O NOVO ESTADO AQUI!
        )

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
        AlertDialog.Builder(this)
            .setTitle("Finalizar Quiz")
            .setMessage("Tem certeza que deseja finalizar e ver seu resultado?")
            .setPositiveButton("Finalizar") { dialog, _ ->

                // 🚨 1. LER O CONTADOR DE INICIALIZAÇÕES
                val prefs = getSharedPreferences(PrefsKeys.PREFS_FILE, MODE_PRIVATE)
                val launchCount = prefs.getInt(PrefsKeys.LAUNCH_COUNT, 0)

                val trigger = AdsKeys.LAUNCH_COUNT_TRIGGER
                val shouldShowAd = (launchCount % AdsKeys.LAUNCH_COUNT_TRIGGER == 0) && (mInterstitialAd != null)


                if (shouldShowAd) {
                    // 2. EXIBIR O ANÚNCIO (E ir para o resultado no callback)
                    mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            showFinalResultDialog() // 🚨 Continua para o resultado após fechar
                            loadInterstitialAd() // Recarrega para o próximo uso
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            super.onAdFailedToShowFullScreenContent(adError)
                            showFinalResultDialog() // 🚨 Vai para o resultado se falhar
                        }
                    }
                    mInterstitialAd?.show(this)
                } else {
                    // 3. VAI DIRETO PARA O RESULTADO
                    showFinalResultDialog()
                }
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


    private fun getQuestionCountPreference(): Int {
        val prefs = getSharedPreferences(PrefsKeys.PREFS_FILE, MODE_PRIVATE)

        // Retorna o valor salvo (5, 10 ou 15), ou o padrão (10) se não houver valor.
        return prefs.getInt(
            PrefsKeys.QUESTION_QUANTITY,
            PrefsKeys.DEFAULT_QUESTION_QUANTITY // Lê o 10 se a chave não for encontrada
        )
    }
    // -------------------------------------------------------------------------
    // 🚨 CARREGAMENTO DE DADOS
    // -------------------------------------------------------------------------
    private fun loadQuestionsAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestedQuantity = getQuestionCountPreference()
                Log.d("QUIZ_DEBUG", "Quantidade de perguntas solicitada: $requestedQuantity") // 🚨 NOVO LOG
                val response: QuestionResponse = RetrofitClient.instance.getQuestionsBySubject(
                    subjectId = subjectId,
                    quantity = requestedQuantity // A quantidade que você definiu
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

                withContext(Dispatchers.Main) {
                    hideLoading() // Esconde em caso de erro de rede
                    Toast.makeText(this@QuizActivity, "Erro de rede ao carregar o quiz.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            AdsKeys.INTERSTITIAL_ID, // Use a chave do seu Intersticial
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            }
        )
    }

    private fun showFinalResultDialog() {
        // 1. Calcula os acertos
        val correctCount = questions.count { it.userSelectedAnswer == it.correctAnswer }
        val totalQuestions = questions.size

        // 2. Define a mensagem de feedback
        val resultMessage = when (correctCount) {
            totalQuestions -> "Excelente! Você acertou todas as $totalQuestions perguntas. 🏆"
            in (totalQuestions * 0.75).toInt()..totalQuestions -> "Muito bom! Você acertou $correctCount de $totalQuestions perguntas."
            in (totalQuestions * 0.5).toInt()..(totalQuestions * 0.75).toInt() -> "Bom! Você acertou $correctCount de $totalQuestions perguntas. Continue estudando."
            else -> "Você acertou $correctCount de $totalQuestions perguntas. Revise o material! 📚"
        }

        AlertDialog.Builder(this)
            .setTitle("Resultado Final")
            .setMessage(resultMessage)
            // 🚨 NOVO BOTÃO: CONFERIR
            .setPositiveButton("Conferir") { _, _ ->
                isQuizFinished = true // 🚨 Entra no modo de correção
                displayQuestion(questions.first()) // Redesenha a primeira para iniciar a correção
                setupFinishedQuizButtons() // Ajusta os botões de navegação
            }
            // 🚨 NOVO BOTÃO: MENU (Comportamento de OK/Encerrar)
            .setNegativeButton("Menu") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    // NOVO: Ajusta o comportamento dos botões após a conferência
    private fun setupFinishedQuizButtons() {
        // Agora que o quiz está conferido, o botão "Finalizar" passa a fechar a Activity.
        binding.btnFinish.setOnClickListener { finish() }
    }
}