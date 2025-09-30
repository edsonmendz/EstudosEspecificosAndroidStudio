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

// Adicionado: É uma boa prática usar o Binding para acesso a Views,
// inclusive para a AdView, se ela estiver no layout principal.



class MainActivity : AppCompatActivity() {

    // Utiliza o View Binding para acessar todas as Views do layout
    private lateinit var binding: ActivityMainBinding

    // Variável para a AdView (mantida, mas acessada via binding)
    // private lateinit var adView: AdView // Não é mais necessário se usar binding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inflar o layout e configurar o View Binding
        // Note que o R.id.main do seu código anterior deve ser o ID do DrawerLayout ou do ConstraintLayout.
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

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
        fetchDataFromApi()

        // ⚠️ REMOVIDO: A lógica de ViewCompat.setOnApplyWindowInsetsListener
        // Foi removida porque o DrawerLayout e o AppBarLayout geralmente tratam
        // a maioria dos insets do sistema automaticamente quando usados em conjunto.
    }



    // Função de lógica da API (mantida como estava)
    private fun fetchDataFromApi() {
        val subjectIdToFetch = 10
        val quantityToFetch = 2

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
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Lógica para ir para a Home
                }
                R.id.nav_subjects -> {
                    // Lógica para ir para a Activity de Matérias
                }
                R.id.nav_settings -> {
                    // Lógica para ir para a Activity de Configurações
                }
            }
            // Fecha a gaveta após o clique
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }


    // Permite fechar a sidebar com o botão "Voltar"

}