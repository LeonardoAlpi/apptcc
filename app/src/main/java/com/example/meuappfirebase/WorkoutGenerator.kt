package com.example.meuappfirebase

import com.apol.myapplication.data.model.DivisaoTreino
import com.apol.myapplication.data.model.TipoDivisao
import com.apol.myapplication.data.model.TipoTreino
import com.apol.myapplication.data.model.TreinoEntity
import com.apol.myapplication.data.model.TreinoNota

/**
 * Helper class para agrupar o treino, suas divisões e suas notas.
 */
data class GeneratedWorkout(
    val treino: TreinoEntity,
    val divisoes: List<GeneratedDivision>
)

/**
 * Helper class para agrupar uma divisão e suas notas (exercícios).
 */
data class GeneratedDivision(
    val divisao: DivisaoTreino, // Terá treinoId temporário
    val notas: List<TreinoNota>  // Terá divisaoId temporário
)


/**
 * Esta classe é o nosso "Motor de Regras" (Versão 2.1 - Corrigida).
 * Adicionado 'userOwnerId' na criação de DivisaoTreino.
 *
 * IMPORTANTE:
 * Substitua os valores 'R.drawable.ic_...' pelos ícones reais que existem
 * no seu projeto (na pasta res/drawable).
 */
class WorkoutGenerator {


    private val iconeCasa = R.drawable.ic_home
    private val iconeAcademia = R.drawable.ic_academia
    private val iconeParque = R.drawable.ic_corrida
    private val iconeCalistenia = R.drawable.ic_esportes
    // -------------------------------------


    fun gerarTreinos(
        pratica: String,
        tempo: String,
        espacos: List<String>,
        imc: Float,
        userId: String
    ): List<GeneratedWorkout> {

        val treinosGerados = mutableListOf<GeneratedWorkout>()
        val nivel = if (pratica == "Sim") "Avançado" else "Iniciante"
        val foco = if (imc >= 25) "Queima Calórica / Resistência" else "Manutenção / Hipertrofia"

        espacos.forEach { espaco ->
            when (espaco) {
                "Casa" -> treinosGerados.addAll(
                    gerarTreinosCasa(nivel, tempo, foco, userId)
                )
                "Academia" -> treinosGerados.addAll(
                    gerarTreinosAcademia(nivel, tempo, foco, userId)
                )
                "Parque" -> treinosGerados.addAll(
                    gerarTreinosParque(nivel, tempo, foco, userId)
                )
            }
        }
        return treinosGerados
    }

    /**
     * Gera treinos para CASA, agora com opções detalhadas para todos os tempos e níveis.
     */
    private fun gerarTreinosCasa(nivel: String, tempo: String, foco: String, userId: String): List<GeneratedWorkout> {
        val workouts = mutableListOf<GeneratedWorkout>()

        // Reestruturamos para um 'when' que cobre todos os tempos
        when (tempo) {
            // --- CENÁRIOS: MENOS DE 30 MINUTOS ---
            "Menos de 30 minutos" -> {
                if (nivel == "Iniciante") {
                    // Nível: Iniciante, Tempo: < 30 min (Manter o original A/B)
                    val treino = TreinoEntity(
                        userOwnerId = userId, nome = "Casa: Rápido (Iniciante)",
                        iconeResId = iconeCasa, tipoDeTreino = TipoTreino.ACADEMIA,
                        detalhes = "Foco: Criar Hábito. 20 min.",
                        tipoDivisao = TipoDivisao.LETRAS
                    )
                    val notasA = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento (Peso Corporal)", conteudo = "3 séries de 10-12 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Flexão de Joelhos", conteudo = "3 séries de 8-10 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Prancha", conteudo = "3 séries de 30 segundos")
                    )
                    val divisaoA = GeneratedDivision(
                        DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "Treino A (Corpo Inteiro)", ordem = 1),
                        notasA
                    )
                    val notasB = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Avanço (Peso Corporal)", conteudo = "3 séries de 10 reps (cada perna)"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Remada com Toalha", conteudo = "3 séries de 12 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Ponte de Glúteos", conteudo = "3 séries de 15 repetições")
                    )
                    val divisaoB = GeneratedDivision(
                        DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "Treino B (Corpo Inteiro)", ordem = 2),
                        notasB
                    )
                    workouts.add(GeneratedWorkout(treino, listOf(divisaoA, divisaoB)))

                } else { // Nível: Avançado
                    // Nível: Avançado, Tempo: < 30 min (Manter o HIIT original)
                    val treino = TreinoEntity(
                        userOwnerId = userId, nome = "Casa: HIIT (Avançado)",
                        iconeResId = iconeCasa, tipoDeTreino = TipoTreino.ACADEMIA,
                        detalhes = "Foco: Intensidade. 20 min.",
                        tipoDivisao = TipoDivisao.LETRAS
                    )
                    val notasHiit = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Aquecimento", conteudo = "5 minutos de Polichinelos leves"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Burpees", conteudo = "4 séries de 30s ON / 30s OFF"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento com Salto", conteudo = "4 séries de 30s ON / 30s OFF"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Corrida Estacionária (Joelho Alto)", conteudo = "4 séries de 30s ON / 30s OFF")
                    )
                    val divisaoHiit = GeneratedDivision(
                        DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "HIIT - Treino Único", ordem = 1),
                        notasHiit
                    )
                    workouts.add(GeneratedWorkout(treino, listOf(divisaoHiit)))
                }
            }

            // --- (NOVO) CENÁRIOS: 30 A 60 MINUTOS ---
            "Entre 30 e 60 minutos" -> {
                if (nivel == "Iniciante") {
                    // Nível: Iniciante, Tempo: 30-60 min
                    val treino = TreinoEntity(
                        userOwnerId = userId, nome = "Casa: Fundamentos (Iniciante)",
                        iconeResId = iconeCasa, tipoDeTreino = TipoTreino.ACADEMIA,
                        detalhes = "Foco: Aprender Movimentos. 45 min.",
                        tipoDivisao = TipoDivisao.LETRAS
                    )
                    val notasA = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento (Peso Corporal)", conteudo = "3 séries de 15 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Flexão de Joelhos", conteudo = "3 séries de 10 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Ponte de Glúteos", conteudo = "3 séries de 15 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Prancha", conteudo = "3 séries de 45 segundos")
                    )
                    val divisaoA = GeneratedDivision(
                        DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "Treino A (Inferior/Core)", ordem = 1),
                        notasA
                    )
                    val notasB = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Avanço Alternado", conteudo = "3 séries de 12 reps (cada perna)"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Remada com Toalha/Elástico", conteudo = "3 séries de 15 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Superman (Lombar)", conteudo = "3 séries de 15 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Abdominal 'Bicicleta'", conteudo = "3 séries de 20 (total)")
                    )
                    val divisaoB = GeneratedDivision(
                        DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "Treino B (Superior/Core)", ordem = 2),
                        notasB
                    )
                    workouts.add(GeneratedWorkout(treino, listOf(divisaoA, divisaoB)))

                } else { // Nível: Avançado
                    // Nível: Avançado, Tempo: 30-60 min
                    val treino = TreinoEntity(
                        userOwnerId = userId, nome = "Casa: Calistenia (Avançado)",
                        iconeResId = iconeCalistenia, tipoDeTreino = TipoTreino.ACADEMIA,
                        detalhes = "Foco: Força Pura. 45 min.",
                        tipoDivisao = TipoDivisao.LETRAS
                    )
                    val notasA = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Flexão (Padrão ou Variação)", conteudo = "4 séries até a falha"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Flexão Declinada (Pés elevados)", conteudo = "3 séries de 10-12 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Mergulho no Banco (Tríceps)", conteudo = "4 séries de 15 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Prancha 'Toca-Ombro'", conteudo = "3 séries de 45 segundos")
                    )
                    val divisaoA = GeneratedDivision(
                        DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "Treino A (Empurrar)", ordem = 1),
                        notasA
                    )
                    val notasB = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Barra Fixa (se tiver) ou Remada c/ Mochila", conteudo = "4 séries até a falha"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento Búlgaro", conteudo = "4 séries de 10 reps (cada perna)"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Elevação Pélvica (Um pé)", conteudo = "3 séries de 12 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Elevação de Pernas (Core)", conteudo = "3 séries de 15 repetições")
                    )
                    val divisaoB = GeneratedDivision(
                        DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "Treino B (Puxar/Pernas)", ordem = 2),
                        notasB
                    )
                    workouts.add(GeneratedWorkout(treino, listOf(divisaoA, divisaoB)))
                }
            }

            // --- (NOVO) CENÁRIOS: MAIS DE 1 HORA ---
            "Mais de 1 hora" -> {
                if (nivel == "Iniciante") {
                    // Nível: Iniciante, Tempo: > 60 min
                    val treino = TreinoEntity(
                        userOwnerId = userId, nome = "Casa: Completo (Iniciante)",
                        iconeResId = iconeCasa, tipoDeTreino = TipoTreino.ACADEMIA,
                        detalhes = "Foco: Resistência. 60 min.",
                        tipoDivisao = TipoDivisao.LETRAS
                    )
                    val notas = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Aquecimento", conteudo = "10 min (Pular corda leve / Polichinelos)"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento (Peso Corporal)", conteudo = "3 séries de 20 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Flexão de Joelhos", conteudo = "3 séries de 12 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Remada com Toalha", conteudo = "3 séries de 15 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Ponte de Glúteos", conteudo = "3 séries de 20 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Prancha", conteudo = "3 séries de 60 segundos"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cardio Leve", conteudo = "20 min (Dança, Subir escada, etc)"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Alongamento", conteudo = "5 min (Corpo inteiro)")
                    )
                    val divisao = GeneratedDivision(
                        // Treino único para ser repetido 2-3x na semana
                        DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "Treino Corpo Inteiro + Cardio", ordem = 1),
                        notas
                    )
                    workouts.add(GeneratedWorkout(treino, listOf(divisao)))

                } else { // Nível: Avançado
                    // Nível: Avançado, Tempo: > 60 min
                    val treino = TreinoEntity(
                        userOwnerId = userId, nome = "Casa: Força + HIIT (Avançado)",
                        iconeResId = iconeCalistenia, tipoDeTreino = TipoTreino.ACADEMIA,
                        detalhes = "Foco: $foco. 60+ min.",
                        tipoDivisao = TipoDivisao.LETRAS
                    )
                    val notas = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Aquecimento", conteudo = "5 min (Movimentos articulares)"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento Búlgaro", conteudo = "4 séries de 12 reps (cada perna)"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Flexão (Variação Difícil)", conteudo = "4 séries até a falha"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Barra Fixa ou Remada c/ Peso", conteudo = "4 séries até a falha"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Elevação de Perna (Core)", conteudo = "3 séries de 15-20 repetições"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "--- HIIT (15 min) ---", conteudo = "Circuito: 40s ON / 20s OFF"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "HIIT 1: Burpees", conteudo = "3-4 rodadas"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "HIIT 2: Agachamento com Salto", conteudo = "3-4 rodadas"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "HIIT 3: Escalador", conteudo = "3-4 rodadas"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Alongamento", conteudo = "5 min (Foco nos músculos usados)")
                    )
                    val divisao = GeneratedDivision(
                        // Treino único de alta intensidade
                        DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "Treino Força + Condicionamento", ordem = 1),
                        notas
                    )
                    workouts.add(GeneratedWorkout(treino, listOf(divisao)))
                }
            }
        } // Fim do 'when (tempo)'

        return workouts
    }

    /**
     * Gera treinos para ACADEMIA, agora com opções detalhadas para todos os tempos e níveis.
     */
    /**
     * Gera treinos para ACADEMIA, (Versão 3.0)
     * Lógica totalmente reestruturada com base nas suas novas regras:
     * - < 30 min e 30-60 min: Treino A/B/C (Superior / Inferior / Complementar)
     * - > 1 hora: Treino A/B/C/D (Split: Peito / Costas / Braços / Pernas)
     */
    private fun gerarTreinosAcademia(nivel: String, tempo: String, foco: String, userId: String): List<GeneratedWorkout> {
        val workouts = mutableListOf<GeneratedWorkout>()

        // --- DEFINIÇÕES DE SÉRIES/REPS ---
        // (Ajusta o volume baseado no nível, mas o tempo define a estrutura)
        val repsIniciante = "3 séries de 10-12 repetições"
        val repsAvancado = "4 séries de 8-10 repetições"

        // --- NOTAS DE CARDIO (REUTILIZÁVEIS) ---
        val cardioCurto = TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cardio", conteudo = "10 min (Esteira ou Elíptico)")
        val cardioLongo = TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cardio Pós-Treino", conteudo = "20-30 min (Esteira ou Bike)")


        when (tempo) {
            // --- CENÁRIO: MENOS DE 30 MINUTOS (A/B/C Rápido) ---
            "Menos de 30 minutos" -> {
                val treinoNome = if (nivel == "Iniciante") "Academia: A/B/C Rápido (Ini.)" else "Academia: A/B/C Rápido (Av.)"
                val detalhes = "Treino Rápido (Superior/Inferior/Compl.)"
                val treino = TreinoEntity(
                    userOwnerId = userId, nome = treinoNome,
                    iconeResId = iconeAcademia, tipoDeTreino = TipoTreino.ACADEMIA,
                    detalhes = detalhes, tipoDivisao = TipoDivisao.LETRAS
                )

                val (notasA, notasB, notasC) = if (nivel == "Iniciante") {
                    // --- Treino A (Superior - Ini.) ---
                    val a = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Supino (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Puxada Frontal (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Desenvolvimento (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Rosca Bíceps (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Tríceps Pulley (Corda)", conteudo = repsIniciante)
                    )
                    // --- Treino B (Inferior - Ini.) ---
                    val b = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Leg Press 45º", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cadeira Extensora", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cadeira Flexora", conteudo = repsIniciante)
                    )
                    // --- Treino C (Complementar - Ini.) ---
                    val c = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Abdominal (Máquina)", conteudo = "3 séries de 15 reps"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Extensão Lombar (Banco)", conteudo = "3 séries de 15 reps"),
                        cardioCurto.copy() // Cardiozinho
                    )
                    Triple(a, b, c) // Retorna os 3
                } else { // Avançado
                    // --- Treino A (Superior - Av.) ---
                    val a = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Supino Reto (Halteres)", conteudo = repsAvancado),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Remada Curvada (Halteres)", conteudo = repsAvancado),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Desenvolvimento (Halteres)", conteudo = repsAvancado),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Rosca Direta (Barra W)", conteudo = repsAvancado),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Tríceps Testa (Halteres)", conteudo = repsAvancado)
                    )
                    // --- Treino B (Inferior - Av.) ---
                    val b = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento (Livre ou Smith)", conteudo = repsAvancado),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cadeira Extensora", conteudo = "3 séries de 12 reps"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cadeira Flexora", conteudo = "3 séries de 12 reps")
                    )
                    // --- Treino C (Complementar - Av.) ---
                    val c = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Levantamento Terra (Stiff)", conteudo = "3 séries de 10 reps"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Abdominal (Cabo)", conteudo = "3 séries de 15 reps"),
                        cardioCurto.copy()
                    )
                    Triple(a, b, c) // Retorna os 3
                }

                val divisaoA = GeneratedDivision(DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "A (Superior)", ordem = 1), notasA)
                val divisaoB = GeneratedDivision(DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "B (Inferior)", ordem = 2), notasB)
                val divisaoC = GeneratedDivision(DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "C (Complementar)", ordem = 3), notasC)
                workouts.add(GeneratedWorkout(treino, listOf(divisaoA, divisaoB, divisaoC)))
            }

            // --- CENÁRIO: 30 A 60 MINUTOS (A/B/C com mais Volume) ---
            "Entre 30 e 60 minutos" -> {
                val treinoNome = if (nivel == "Iniciante") "Academia: A/B/C Volume (Ini.)" else "Academia: A/B/C Volume (Av.)"
                val detalhes = "Treino de Volume (Superior/Inferior/Compl.)"
                val treino = TreinoEntity(
                    userOwnerId = userId, nome = treinoNome,
                    iconeResId = iconeAcademia, tipoDeTreino = TipoTreino.ACADEMIA,
                    detalhes = detalhes, tipoDivisao = TipoDivisao.LETRAS
                )

                val (notasA, notasB, notasC) = if (nivel == "Iniciante") {
                    // --- Treino A (Superior - Ini. Volume) ---
                    val a = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Supino (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Peck Deck (Voador)", conteudo = repsIniciante), // +1 Peito
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Puxada Frontal (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Remada Sentada (Máquina)", conteudo = repsIniciante), // +1 Costas
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Desenvolvimento (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Elevação Lateral (Máquina)", conteudo = repsIniciante), // +1 Ombro
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Rosca Bíceps (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Tríceps Pulley (Corda)", conteudo = repsIniciante)
                    )
                    // --- Treino B (Inferior - Ini. Volume) ---
                    val b = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Leg Press 45º", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento Hack (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cadeira Extensora", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cadeira Flexora", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Panturrilha (Máquina)", conteudo = "3 séries de 20 reps")
                    )
                    // --- Treino C (Complementar - Ini. Volume) ---
                    val c = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Abdominal (Máquina)", conteudo = "3 séries de 15 reps"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Extensão Lombar (Banco)", conteudo = "3 séries de 15 reps"),
                        cardioCurto.copy(conteudo = "15 min (Esteira ou Elíptico)") // Um pouco mais de cardio
                    )
                    Triple(a, b, c)
                } else { // Avançado
                    // --- Treino A (Superior - Av. Volume) ---
                    val a = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Supino Reto (Halteres)", conteudo = repsAvancado),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Supino Inclinado (Halteres)", conteudo = repsIniciante), // +1 Peito
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Barra Fixa (ou Puxada)", conteudo = repsAvancado),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Remada Curvada (Barra)", conteudo = repsAvancado), // +1 Costas
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Desenvolvimento (Halteres)", conteudo = repsAvancado),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Elevação Lateral (Halteres)", conteudo = repsIniciante), // +1 Ombro
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Rosca Direta (Barra W)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Tríceps Testa (Halteres)", conteudo = repsIniciante)
                    )
                    // --- Treino B (Inferior - Av. Volume) ---
                    val b = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento Livre", conteudo = repsAvancado),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Leg Press 45º", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cadeira Flexora", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Stiff (Barra)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Panturrilha (Livre/Máquina)", conteudo = "3 séries de 20 reps")
                    )
                    // --- Treino C (Complementar - Av. Volume) ---
                    val c = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Levantamento Terra", conteudo = "3 séries de 8 reps"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Abdominal (Cabo)", conteudo = "3 séries de 15 reps"),
                        cardioCurto.copy(conteudo = "15 min (Esteira ou Elíptico)")
                    )
                    Triple(a, b, c)
                }

                val divisaoA = GeneratedDivision(DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "A (Superior)", ordem = 1), notasA)
                val divisaoB = GeneratedDivision(DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "B (Inferior)", ordem = 2), notasB)
                val divisaoC = GeneratedDivision(DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "C (Complementar)", ordem = 3), notasC)
                workouts.add(GeneratedWorkout(treino, listOf(divisaoA, divisaoB, divisaoC)))
            }

            // --- CENÁRIO: MAIS DE 1 HORA (Split A/B/C/D) ---
            "Mais de 1 hora" -> {
                val treinoNome = if (nivel == "Iniciante") "Academia: Split A/B/C/D (Ini.)" else "Academia: Split A/B/C/D (Av.)"
                val detalhes = "Split 4 dias. Sugestão: 2 treina, 1 descansa, 2 treina."
                val treino = TreinoEntity(
                    userOwnerId = userId, nome = treinoNome,
                    iconeResId = iconeAcademia, tipoDeTreino = TipoTreino.ACADEMIA,
                    detalhes = detalhes, tipoDivisao = TipoDivisao.LETRAS
                )

                val (notasA, notasB, notasC, notasD) = if (nivel == "Iniciante") {
                    // --- Treino A (Peito - Ini.) ---
                    val a = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Supino (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Supino Inclinado (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Peck Deck (Voador)", conteudo = repsIniciante),
                        cardioLongo.copy()
                    )
                    // --- Treino B (Costas - Ini.) ---
                    val b = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Puxada Frontal (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Remada Sentada (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Remada Cavalinho (Máquina)", conteudo = repsIniciante),
                        cardioLongo.copy()
                    )
                    // --- Treino C (Braços/Ombros - Ini.) ---
                    val c = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Desenvolvimento (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Elevação Lateral (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Rosca Bíceps (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Tríceps Pulley (Corda)", conteudo = repsIniciante),
                        cardioLongo.copy()
                    )
                    // --- Treino D (Pernas - Ini.) ---
                    val d = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Leg Press 45º", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento Hack (Máquina)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cadeira Extensora", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cadeira Flexora", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Panturrilha (Máquina)", conteudo = "3 séries de 20 reps"),
                        cardioLongo.copy()
                    )
                    Quadruple(a, b, c, d)
                } else { // Avançado
                    // --- Treino A (Peito - Av.) ---
                    val a = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Supino Reto (Barra ou Halter)", conteudo = repsAvancado),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Supino Inclinado (Halteres)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Crucifixo (Halteres ou Polia)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Paralelas (ou Dips Máquina)", conteudo = "3 séries até a falha"),
                        cardioLongo.copy()
                    )
                    // --- Treino B (Costas - Av.) ---
                    val b = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Barra Fixa (ou Graviton)", conteudo = "4 séries até a falha"),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Remada Curvada (Barra)", conteudo = repsAvancado),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Serrote (Remada Unilateral)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Encolhimento (Halteres)", conteudo = "3 séries de 15 reps"),
                        cardioLongo.copy()
                    )
                    // --- Treino C (Braços/Ombros - Av.) ---
                    val c = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Desenvolvimento (Halteres)", conteudo = repsAvancado),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Elevação Lateral (Halteres)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Rosca Direta (Barra W)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Rosca Martelo (Halteres)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Tríceps Testa (Barra W)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Tríceps Corda (Polia)", conteudo = repsIniciante),
                        cardioLongo.copy()
                    )
                    // --- Treino D (Pernas - Av.) ---
                    val d = listOf(
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento Livre", conteudo = repsAvancado),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Stiff (Barra)", conteudo = repsAvancado),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Leg Press 45º", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Afundo (Halteres)", conteudo = repsIniciante),
                        TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Panturrilha (Livre ou Smith)", conteudo = "4 séries de 20 reps"),
                        cardioLongo.copy()
                    )
                    Quadruple(a, b, c, d)
                }

                val divisaoA = GeneratedDivision(DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "A (Peito)", ordem = 1), notasA)
                val divisaoB = GeneratedDivision(DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "B (Costas)", ordem = 2), notasB)
                val divisaoC = GeneratedDivision(DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "C (Braços/Ombros)", ordem = 3), notasC)
                val divisaoD = GeneratedDivision(DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = "D (Pernas)", ordem = 4), notasD)
                workouts.add(GeneratedWorkout(treino, listOf(divisaoA, divisaoB, divisaoC, divisaoD)))
            }
        } // Fim do 'when (tempo)'
        return workouts
    }

    // (Helper class para o retorno de 4 valores, se ainda não existir no seu arquivo)
    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    /**
     * Gera treinos para PARQUE/AR LIVRE, agora com opções detalhadas para todos os tempos e níveis.
     * Foco principal é Corrida (TipoTreino.CORRIDA) e Calistenia (TipoTreino.ACADEMIA).
     */
    /**
     * Gera treinos para PARQUE/AR LIVRE, (Versão 3.0)
     * Lógica reestruturada com base no IMC (Foco) como driver principal.
     * - Foco "Queima Calórica": Prioriza Cardio (corrida/tiros) com calistenia complementar.
     * - Foco "Manutenção": Prioriza Calistenia (força) com cardio complementar.
     */
    /**
     * Gera treinos para PARQUE/AR LIVRE, (Versão 3.1 - Correção de 'val')
     * Lógica reestruturada com base no IMC (Foco) como driver principal.
     */
    private fun gerarTreinosParque(nivel: String, tempo: String, foco: String, userId: String): List<GeneratedWorkout> {
        val workouts = mutableListOf<GeneratedWorkout>()
        val repsIniciante = "3 séries de 10-15 repetições"
        val repsAvancado = "4 séries até a falha (ou 10-15 reps)"

        // --- CORREÇÃO AQUI ---
        // 1. Definimos as variáveis (nome, ícone, divisão) ANTES, com base no foco.
        val nomeDoTreino: String
        val iconeDoTreino: Int
        val nomeDivisao: String

        if (foco == "Queima Calórica / Resistência") {
            // FOCO: CARDIO (IMC ALTO)
            nomeDoTreino = "Parque: Foco Cardio ($nivel)"
            iconeDoTreino = iconeParque // Ícone de Parque/Corrida
            nomeDivisao = "Treino Aeróbico + Circuito"
        } else {
            // FOCO: CALISTENIA (IMC NORMAL)
            nomeDoTreino = "Parque: Foco Força ($nivel)"
            iconeDoTreino = iconeCalistenia // Ícone de Calistenia
            nomeDivisao = "Treino Calistenia + Cardio"
        }

        // 2. Agora criamos o TreinoEntity (como 'val') com os valores corretos.
        val treino = TreinoEntity(
            userOwnerId = userId,
            nome = nomeDoTreino, // Usa a variável definida acima
            iconeResId = iconeDoTreino, // Usa a variável definida acima
            tipoDeTreino = TipoTreino.GENERICO,
            detalhes = "Foco: $foco, Nível: $nivel",
            tipoDivisao = TipoDivisao.LETRAS
        )

        val notasDoTreino = mutableListOf<TreinoNota>()

        // 3. O restante da lógica (os 'when(tempo)') permanece idêntico

        // --- DRIVER PRINCIPAL: FOCO (IMC) ---
        if (foco == "Queima Calórica / Resistência") {
            when (tempo) {
                // --- < 30 min (Foco Cardio) ---
                "Menos de 30 minutos" -> {
                    if (nivel == "Iniciante") {
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Aquecimento", conteudo = "5 min - Caminhada leve"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Treino Principal", conteudo = "20 min - Caminhada Rápida (contínua)"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Desaquecimento", conteudo = "5 min - Alongamento leve"))
                    } else { // Avançado
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Aquecimento", conteudo = "5 min - Trote leve"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "HIIT (Tiros) 8x", conteudo = "1 min CORRIDA FORTE / 1 min CAMINHADA"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Desaquecimento", conteudo = "5 min - Trote leve"))
                    }
                }
                // --- 30-60 min (Foco Cardio) ---
                "Entre 30 e 60 minutos" -> {
                    val cardioPrincipal = if (nivel == "Iniciante") "20 min - Caminhada Rápida/Trote" else "20 min - Corrida Contínua"
                    notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cardio Inicial", conteudo = cardioPrincipal))
                    notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "--- Circuito (3 Rodadas) ---", conteudo = "Descansar 1 min entre rodadas"))
                    if (nivel == "Iniciante") {
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Flexão Inclinada (em banco)", conteudo = "10 repetições"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento (Peso Corporal)", conteudo = "15 repetições"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Abdominal (curto)", conteudo = "20 repetições"))
                    } else { // Avançado
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Flexão (Chão)", conteudo = "10-15 repetições"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento com Salto", conteudo = "15 repetições"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Elevação de Joelhos (em barra)", conteudo = "15 repetições"))
                    }
                }
                // --- > 1 hora (Foco Cardio) ---
                "Mais de 1 hora" -> {
                    val cardioPrincipal = if (nivel == "Iniciante") "30-40 min - Caminhada Rápida/Trote" else "30-40 min - Corrida Contínua"
                    notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cardio Inicial", conteudo = cardioPrincipal))
                    notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "--- Circuito (3-4 Rodadas) ---", conteudo = "Descansar 1 min entre rodadas"))
                    if (nivel == "Iniciante") {
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Polichinelos", conteudo = "30 segundos"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Flexão Inclinada (banco)", conteudo = "12 repetições"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento (Peso Corporal)", conteudo = "20 repetições"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Prancha", conteudo = "30-45 segundos"))
                    } else { // Avançado
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Burpees", conteudo = "10 repetições"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Flexão (Chão)", conteudo = "15-20 repetições"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Avanço (com salto)", conteudo = "12 repetições (cada perna)"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Barra Fixa (ou Australiana)", conteudo = "Até a falha"))
                    }
                }
            }
        } else {
            // FOCO: CALISTENIA (IMC NORMAL)
            when (tempo) {
                // --- < 30 min (Foco Força) ---
                "Menos de 30 minutos" -> {
                    notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cardio (Aquecimento)", conteudo = "5 min - Trote leve ou Polichinelos"))
                    notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "--- Circuito Rápido (3-4 Rodadas) ---", conteudo = "Descanso mínimo entre exercícios"))
                    if (nivel == "Iniciante") {
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Remada Australiana (barra baixa)", conteudo = "10 reps"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Flexão Inclinada (banco)", conteudo = "10 reps"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento (Peso Corporal)", conteudo = "15 reps"))
                    } else { // Avançado
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Barra Fixa (Pull-up)", conteudo = "5-8 reps"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Mergulho (Dips em paralelas/banco)", conteudo = "10 reps"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento Búlgaro", conteudo = "10 reps (cada perna)"))
                    }
                }
                // --- 30-60 min (Foco Força) ---
                "Entre 30 e 60 minutos" -> {
                    notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cardio (Aquecimento)", conteudo = "5-10 min - Trote ou Pular Corda"))
                    notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "--- Treino de Força ---", conteudo = "Descansar 60-90s entre séries"))
                    if (nivel == "Iniciante") {
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Remada Australiana (barra baixa)", conteudo = repsIniciante))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Flexão Inclinada", conteudo = repsIniciante))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento", conteudo = repsIniciante))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Prancha", conteudo = "3 séries de 45-60 segundos"))
                    } else { // Avançado
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Barra Fixa (Pull-up)", conteudo = repsAvancado))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Flexão (Padrão ou Variação)", conteudo = repsAvancado))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Mergulho (Dips)", conteudo = repsAvancado))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento (Pistola assistido ou Búlgaro)", conteudo = repsAvancado))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Elevação de Pernas (na barra)", conteudo = "3 séries de 15 reps"))
                    }
                    notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cardio (Final)", conteudo = "10 min - Trote leve"))
                }
                // --- > 1 hora (Foco Força) ---
                "Mais de 1 hora" -> {
                    notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cardio (Aquecimento)", conteudo = "10 min - Trote ou Pular Corda"))
                    notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "--- Treino de Força (Alto Volume) ---", conteudo = "Descansar 60-90s entre séries"))
                    if (nivel == "Iniciante") {
                        val repsIniVolume = "4 séries de 10-15 repetições"
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Remada Australiana", conteudo = repsIniVolume))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Flexão Inclinada", conteudo = repsIniVolume))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento", conteudo = repsIniVolume))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Avanço (Passada)", conteudo = "3 séries de 12 reps (cada)"))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Prancha", conteudo = "4 séries de 60 segundos"))
                    } else { // Avançado
                        val repsAvVolume = "5 séries até a falha (ou 12-15 reps)"
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Barra Fixa (Pull-up)", conteudo = repsAvVolume))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Flexão (Padrão ou Variação)", conteudo = repsAvVolume))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Mergulho (Dips)", conteudo = repsAvVolume))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Agachamento (Pistola assistido ou Búlgaro)", conteudo = repsAvVolume))
                        notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Elevação de Pernas (na barra)", conteudo = "4 séries de 15 reps"))
                    }
                    notasDoTreino.add(TreinoNota(userOwnerId = userId, divisaoId = 0, titulo = "Cardio (Final)", conteudo = "15-20 min - Corrida contínua"))
                }
            }
        }

        // Adiciona o treino à lista de retorno
        val divisao = GeneratedDivision(
            DivisaoTreino(userOwnerId = userId, treinoId = 0, nome = nomeDivisao, ordem = 1),
            notasDoTreino
        )
        workouts.add(GeneratedWorkout(treino, listOf(divisao)))

        return workouts
    }
}