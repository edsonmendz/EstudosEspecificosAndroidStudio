package com.edsonmendesfullstack.EstudosEspecificos

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat // Adicionado para lidar com a Sidebar
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.edsonmendesfullstack.EstudosEspecificos.databinding.ActivityMainBinding // 🚨 IMPORTANTE: Seu arquivo de Binding
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.activity.OnBackPressedCallback
import android.content.SharedPreferences
import android.content.Context
import android.view.MenuItem
import android.widget.Toast
import com.edsonmendesfullstack.EstudosEspecificos.RetrofitClient

import com.edsonmendesfullstack.EstudosEspecificos.Theme // 🚨 Novo import
import com.edsonmendesfullstack.EstudosEspecificos.Subject // 🚨 Novo import
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import androidx.fragment.app.commit

import androidx.appcompat.app.ActionBarDrawerToggle // 🚨 NOVO: Resolve ActionBarDrawerToggle no setupNavigationDrawer


import com.edsonmendesfullstack.EstudosEspecificos.ThemesFragment

// Adicionado: É uma boa prática usar o Binding para acesso a Views,
// inclusive para a AdView, se ela estiver no layout principal.



class MainActivity : AppCompatActivity() {

    // Utiliza o View Binding para acessar todas as Views do layout
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPrefs: SharedPreferences
    private var allThemes: List<Theme>? = null
    private var allSubjects: List<Subject>? = null

    // Variável para a AdView (mantida, mas acessada via binding)
    // private lateinit var adView: AdView // Não é mais necessário se usar binding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inflar o layout e configurar o View Binding
        // Note que o R.id.main do seu código anterior deve ser o ID do DrawerLayout ou do ConstraintLayout.
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(binding.mainFragmentContainer.id, LoadingFragment())
            }
        }

        loadThemesAndSubjectsAsync()

        // -----------------------------------------------------------------------
        // Inicializa o SharedPreferences
        sharedPrefs = getSharedPreferences(PrefsKeys.PREFS_FILE, Context.MODE_PRIVATE)

        // A) Lógica do Contador de Inicializações (Launch Count)
        val currentLaunchCount = sharedPrefs.getInt(PrefsKeys.LAUNCH_COUNT, 0)
        val newLaunchCount = currentLaunchCount + 1

        // Salva o novo valor
        sharedPrefs.edit().putInt(PrefsKeys.LAUNCH_COUNT, newLaunchCount).apply()

        Log.i("PREFS_INFO", "App inicializado pela $newLaunchCount" + "ª vez.")


        // B) Lógica para Leitura da Quantidade de Perguntas
        val questionQuantity = sharedPrefs.getInt(
            PrefsKeys.QUESTION_QUANTITY,
            PrefsKeys.DEFAULT_QUESTION_QUANTITY
        )
        Log.i("PREFS_INFO", "Quantidade de perguntas a buscar: $questionQuantity")

        // A coroutine de API agora usará este valor lido:
        // fetchDataFromApi(questionQuantity)

        // C) Lógica para Anúncios (Leitura da Última Exibição)
        val lastAdTime = sharedPrefs.getLong(PrefsKeys.LAST_AD_TIME, PrefsKeys.DEFAULT_AD_TIME)
        // Você usará lastAdTime para decidir se deve ou não mostrar o anúncio
        Log.i("PREFS_INFO", "Último anúncio exibido em: $lastAdTime (Timestamp)")

        // -----------------------------------------------------------------------

        val callback = object : OnBackPressedCallback(true) { // Habilitado por padrão
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    // Se a Sidebar estiver aberta, feche-a e consuma o evento.
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Se a Sidebar estiver fechada, queremos o comportamento padrão (fechar a Activity).
                    // Para isso, desabilitamos este callback e chamamos o dispatcher novamente.
                    isEnabled = false // Desabilita esta lógica customizada
                    onBackPressedDispatcher.onBackPressed() // Aciona o comportamento padrão do sistema
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)



        // 2. Configuração da Top Bar (Toolbar)
        // Acessamos a Toolbar que está DENTRO do AppBarLayout
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 3. Configurar o Navigation Drawer e o botão Hamburger
        setupNavigationDrawer()

        // 4. Inicializa o SDK do AdMob
        MobileAds.initialize(this) {}

        // 5. Carrega o Anúncio
        // Acessa a AdView diretamente via binding.adViewBanner
        val adRequest = AdRequest.Builder().build()
        binding.adViewBanner.loadAd(adRequest)

        // 6. Chama a função de API
        // Esta chamada pode ser movida para onResume() ou para um evento de clique,
        // dependendo da sua necessidade, mas a mantemos aqui por enquanto.
        fetchDataFromApi(questionQuantity)

    }



    // Função de lógica da API (mantida como estava)
    private fun fetchDataFromApi(quantityToFetch: Int) {
        val subjectIdToFetch = 10

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getQuestionsBySubject(
                    subjectId = subjectIdToFetch,
                    quantity = quantityToFetch
                )

                withContext(Dispatchers.Main) {
                    Log.d("API_SUCCESS", "Resposta da API recebida (Demora: ${response.count} perguntas)")

                    val primeiraPergunta = response.questions.firstOrNull()
                    if (primeiraPergunta != null) {
                        Log.i("API_DATA", "Primeira pergunta (ID ${primeiraPergunta.id}): ${primeiraPergunta.questionText}")
                    } else {
                        Log.w("API_DATA", "A lista de perguntas retornou vazia.")
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Falha ao carregar dados. Mensagem: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    // TODO: Mostrar erro na UI
                }
            }
        }
    }


    private fun handleQuantitySelection(quantity: Int, menuItem: MenuItem): Boolean {
        // 1. Salva a nova quantidade no SharedPreferences
        sharedPrefs.edit().putInt(PrefsKeys.QUESTION_QUANTITY, quantity).apply()

        // 2. Marca o item de menu como checado
        menuItem.isChecked = true

        // 3. Opcional: Recarrega os dados com a nova quantidade (se for o caso)
        // fetchDataFromApi(quantity)

        return true
    }

    private fun checkSavedQuantityOption() {
        val savedQuantity = sharedPrefs.getInt(
            PrefsKeys.QUESTION_QUANTITY,
            PrefsKeys.DEFAULT_QUESTION_QUANTITY
        )

        val menuId = when (savedQuantity) {
            5 -> R.id.nav_quantity_5
            10 -> R.id.nav_quantity_10
            15 -> R.id.nav_quantity_15
            else -> R.id.nav_quantity_10 // Padrão
        }

        // Procura o item de menu e o marca
        binding.navView.menu.findItem(menuId)?.isChecked = true
    }

    // Função para configurar a Sidebar (Navigation Drawer)
    private fun setupNavigationDrawer() {
        // Usa a Toolbar para gerenciar o botão Hamburger
        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar, // Passa a Toolbar
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Lidar com cliques nos itens da Sidebar (Navigation View)
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            // Fecha o drawer após o clique
            binding.drawerLayout.closeDrawer(GravityCompat.START)

            return@setNavigationItemSelectedListener when (menuItem.itemId) {

                // -------------------------------------------------------------
                // LÓGICA DE CONFIGURAÇÃO DE QUANTIDADE DE PERGUNTAS
                // -------------------------------------------------------------
                R.id.nav_quantity_5 -> handleQuantitySelection(5, menuItem)
                R.id.nav_quantity_10 -> handleQuantitySelection(10, menuItem)
                R.id.nav_quantity_15 -> handleQuantitySelection(15, menuItem)

                // -------------------------------------------------------------
                // LÓGICA DAS TELAS PRINCIPAIS (mantida)
                // -------------------------------------------------------------
                R.id.nav_home -> {
                    // Lógica para ir para a tela Home
                    true // Indica que o item foi manipulado
                }
                R.id.nav_subjects -> {
                    // Lógica para ir para a tela de Matérias
                    true
                }
                R.id.nav_settings -> {
                    // Lógica para Configurações
                    true
                }
                else -> false
            }
        }
        checkSavedQuantityOption()
    }

    private fun loadThemesAndSubjectsAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Timeout de 40 segundos para as duas chamadas combinadas
                withTimeout(40_000L) {
                    // Chamada para Temas
                    val themes = RetrofitClient.instance.getThemes()

                    // Chamada para Matérias
                    val subjects = RetrofitClient.instance.getSubjects()

                    // Armazena no cache da Activity na thread principal
                    withContext(Dispatchers.Main) {
                        allThemes = themes
                        allSubjects = subjects

                        // 🚨 NOVO LOG DE VALIDAÇÃO
                        Log.d("API_LOAD", "Temas carregados: ${themes.size}")
                        Log.d("API_LOAD", "Matérias carregadas: ${subjects}")

                        if (themes.isNotEmpty()) {
                            showThemesFragment(themes)
                        } else {
                            // Tratar erro: Lista vazia
                        }
                    }
                }

            } catch (e: TimeoutCancellationException) {
                // Tratar Timeout
            } catch (e: Exception) {
                // Tratar outros Erros
            }
        }
    }

    fun onThemeSelected(theme: Theme) {
        // 1. Loga a seleção
        Log.d("THEME_NAV", "Tema selecionado: ID=${theme.id}, Nome=${theme.name}")
        Log.d("FILTER_DEBUG", "Total de Matérias carregadas (allSubjects): ${allSubjects?.size ?: 0}")

        // 2. Fecha o drawer (boa prática após uma seleção de conteúdo)
        binding.drawerLayout.closeDrawer(GravityCompat.START)

        // 3. 🚨 Próximo Passo: Filtrar as matérias e mostrar o SubjectsFragment

        // Filtra as matérias que pertencem a este tema
        val filteredSubjects = allSubjects?.filter { subject ->
            subject.themeId == theme.id
        } ?: emptyList()

        Log.d("FILTER_DEBUG", "Matérias filtradas para Tema ${theme.id}: ${filteredSubjects.size}")

        if (filteredSubjects.isNotEmpty()) {
            // TODO: Chamar o Fragmento de Matérias (SubjectsFragment) com a lista filtrada
            showSubjectsFragment(filteredSubjects)
        } else {
            // Tratar caso de não haver matérias para este tema
            Toast.makeText(this, "Nenhuma matéria encontrada para ${theme.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSubjectsFragment(subjects: List<Subject>) {
        // 🚨 A transição usa REPLACE, que apaga o ThemesFragment e o substitui.
        supportFragmentManager.commit {
            replace(binding.mainFragmentContainer.id, SubjectsFragment.newInstance(subjects))
            setReorderingAllowed(true)
            // Adiciona a transação à Back Stack, permitindo que o usuário use o botão "Voltar"
            // para retornar à lista de Temas.
            addToBackStack(null)
        }
    }

    private fun showThemesFragment(themes: List<Theme>) {
        // Agora, vamos criar este Fragmento!
        if (allSubjects == null) {
            // Se as matérias não carregaram (teoricamente não deve acontecer aqui), trate o erro
            Log.e("API_LOAD", "Matérias não carregadas junto com temas.")
            return
        }

        // 🚨 Substituir o Fragmento de Loading pelo Fragmento de Temas
        supportFragmentManager.commit {
            replace(binding.mainFragmentContainer.id, ThemesFragment.newInstance(themes))
            setReorderingAllowed(true)
        }
    }

    fun onSubjectSelected(subject: Subject) {
        Log.d("SUBJECT_NAV", "Matéria selecionada: ID=${subject.id}, Nome=${subject.name}")

        // TODO: AQUI É ONDE VOCÊ INICIA A TELA DE PERGUNTAS!
        Toast.makeText(this, "Matéria ${subject.name} clicada. Pronto para começar o Quiz!", Toast.LENGTH_SHORT).show()
    }
}