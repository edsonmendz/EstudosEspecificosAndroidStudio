package com.edsonmendesfullstack.EstudosEspecificos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class MainActivity : AppCompatActivity() {

    // Variável para a AdView
    private lateinit var adView: AdView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Remove enableEdgeToEdge(), que é incompatível com a abordagem manual de Views.
        // Se você usou enableEdgeToEdge() no seu código original, ele deve ser removido.
        // enableEdgeToEdge()

        // Define o layout XML principal
        setContentView(R.layout.activity_main)

        // 1. Inicializa o SDK do AdMob (deve ser a primeira coisa a ser feita)
        // Isso deve vir ANTES de tentar carregar o anúncio.
        MobileAds.initialize(this) {}

        // 2. Configuração ÚNICA e Correta do Insetting
        // Encontra o layout principal UMA ÚNICA VEZ
        val rootView = findViewById<android.view.View>(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            // Obtém as dimensões das barras do sistema
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Aplica o padding no topo e na parte inferior da sua View
            view.setPadding(
                systemBars.left,
                systemBars.top,      // Padding no topo para a Status Bar
                systemBars.right,
                systemBars.bottom    // Padding na parte inferior para a Navigation Bar
            )
            // Retorna o resultado
            insets
        }

        // 3. Carrega o Anúncio
        // Note que o ID 'ad_view_banner' deve estar definido no seu XML de banner.
        adView = findViewById(R.id.ad_view_banner)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}