package com.edsonmendesfullstack.EstudosEspecificos

import retrofit2.http.GET
import retrofit2.http.Path // Necessário para o endpoint com ID
import retrofit2.http.Query

interface ApiService {

    /**
     * Endpoint para buscar todos os Temas.
     * Espera receber uma lista de objetos Theme.
     */
    @GET("temas")
    suspend fun getThemes(): List<Theme>

    /**
     * Endpoint para buscar todas as Matérias.
     * Espera receber uma lista de objetos Subject.
     */
    @GET("materias")
    suspend fun getSubjects(): List<Subject>

    /**
     * Endpoint para buscar perguntas com base no ID da Matéria e
     * opcionalmente manipulando a quantidade (qtd).
     * Ex: /perguntas/10?qtd=2
     */
    @GET("perguntas/{subjectId}") // 🚨 CORRIGIDO: O nome no Path AGORA É 'subjectId'
    suspend fun getQuestionsBySubject(
        // 🚨 CORRIGIDO: O nome da anotação AGORA É 'subjectId' para casar com o Path
        @Path("subjectId") subjectId: Int, // O '10' na URL
        @Query("qtd") quantity: Int? = null // O '2' na URL
    ): QuestionResponse
}