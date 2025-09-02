package com.apol.myapplication

import com.apol.myapplication.data.model.DivisaoTreino
import com.apol.myapplication.data.model.TipoDivisao
import com.apol.myapplication.data.model.TipoTreino
import com.apol.myapplication.data.model.TreinoEntity
import com.apol.myapplication.data.model.TreinoNota
import com.example.meuappfirebase.R

// Estruturas para organizar os treinos pré-definidos
data class PredefinedNota(val titulo: String, val conteudo: String)
data class PredefinedDivision(val nome: String, val notas: List<PredefinedNota>)
data class PredefinedWorkout(
    val nome: String,
    val tipoTreino: TipoTreino,
    val tipoDivisao: TipoDivisao,
    val iconeResId: Int,
    val divisions: List<PredefinedDivision>
)

// Nossa "Biblioteca" de treinos agora com mais opções
object WorkoutTemplateRepository {

    // --- TREINOS PARA INICIANTES ---
    val pularCordaIniciante = PredefinedWorkout(
        nome = "Iniciante: Pular Corda",
        tipoTreino = TipoTreino.GENERICO, tipoDivisao = TipoDivisao.LETRAS,
        iconeResId = R.drawable.ic_treinos,
        divisions = listOf(
            PredefinedDivision("Treino A - Foco em Resistência", listOf(
                PredefinedNota("Aquecimento", "5 minutos de caminhada leve e polichinelos."),
                PredefinedNota("Pular Corda", "Pule continuamente por 1 minuto, depois descanse por 1 minuto. Repita 5 vezes."),
                PredefinedNota("Dica de Postura", "Mantenha as costas retas e olhe para frente. Use os pulsos para girar a corda, não os braços."),
                PredefinedNota("Vídeo de Ajuda", "Assista a este vídeo para aprender a técnica correta: https://www.youtube.com/watch?v=1BZM2Vre5oc")
            ))
        )
    )

    val corpoInteiroCasaIniciante = PredefinedWorkout(
        nome = "Corpo Inteiro em Casa (Iniciante)",
        tipoTreino = TipoTreino.GENERICO, tipoDivisao = TipoDivisao.LETRAS,
        iconeResId = R.drawable.ic_treinos,
        divisions = listOf(
            PredefinedDivision("Treino A", listOf(
                PredefinedNota("Aquecimento", "5 minutos de polichinelos e corrida no lugar."),
                PredefinedNota("Agachamento sem peso", "3 séries de 12 repetições. Foque na postura correta."),
                PredefinedNota("Flexão com joelhos no chão", "3 séries de 8 repetições."),
                PredefinedNota("Prancha abdominal", "3 séries segurando a posição por 30 segundos."),
                PredefinedNota("Dica Importante", "Descanse 1 minuto entre cada série. Beba água!")
            ))
        )
    )

    // --- TREINOS PARA QUEM JÁ PRATICA ---
    val calisteniaCasaIntermediario = PredefinedWorkout(
        nome = "Calistenia Intermediária em Casa",
        tipoTreino = TipoTreino.GENERICO, tipoDivisao = TipoDivisao.DIAS_DA_SEMANA,
        iconeResId = R.drawable.ic_treinos,
        divisions = listOf(
            PredefinedDivision("Segunda: Peito e Tríceps", listOf(
                PredefinedNota("Flexões Diamante", "3 séries até a falha."),
                PredefinedNota("Mergulho no Banco/Cadeira", "3 séries de 15 repetições.")
            )),
            PredefinedDivision("Quarta: Costas e Bíceps", listOf(
                PredefinedNota("Barra Fixa (se tiver)", "3 séries até a falha."),
                PredefinedNota("Flexão Isométrica (Superman)", "3 séries segurando por 30 segundos.")
            )),
            PredefinedDivision("Sexta: Pernas e Core", listOf(
                PredefinedNota("Agachamento com Salto", "3 séries de 12 repetições."),
                PredefinedNota("Elevação de Pernas (deitado)", "3 séries de 15 repetições.")
            ))
        )
    )

    // NOVO TREINO 1: HIIT
    val hiitCasa = PredefinedWorkout(
        nome = "HIIT Rápido em Casa",
        tipoTreino = TipoTreino.GENERICO, tipoDivisao = TipoDivisao.LETRAS,
        iconeResId = R.drawable.ic_treinos,
        divisions = listOf(
            PredefinedDivision("Treino de Alta Intensidade", listOf(
                PredefinedNota("Instruções", "Faça cada exercício por 45 segundos com o máximo de esforço, e descanse por 15 segundos entre eles. Repita o circuito completo 3 vezes."),
                PredefinedNota("1. Polichinelos", "Mantenha um ritmo acelerado."),
                PredefinedNota("2. Agachamento com Salto", "Exploda para cima a cada salto."),
                PredefinedNota("3. Escalador (Mountain Climber)", "Mantenha o abdômen contraído."),
                PredefinedNota("4. Burpees", "O exercício mais completo. Dê o seu máximo!")
            ))
        )
    )

    // NOVO TREINO 2: HÍBRIDO
    val hibridoParque = PredefinedWorkout(
        nome = "Treino Híbrido no Parque",
        tipoTreino = TipoTreino.GENERICO, tipoDivisao = TipoDivisao.DIAS_DA_SEMANA,
        iconeResId = R.drawable.ic_treinos,
        divisions = listOf(
            PredefinedDivision("Dia 1: Corrida Leve + Core", listOf(
                PredefinedNota("Cardio", "20 minutos de corrida em um ritmo confortável."),
                PredefinedNota("Core", "3 séries de prancha (45 segundos) e 3 séries de elevação de pernas (15 repetições).")
            )),
            PredefinedDivision("Dia 2: Tiros + Superior", listOf(
                PredefinedNota("Tiros (Sprints)", "Aqueça por 5 min. Corra na velocidade máxima por 30 segundos, caminhe por 1 min. Repita 6 vezes."),
                PredefinedNota("Superior", "Encontre uma barra no parque e faça 3 séries de barra fixa até a falha. Depois, 3 séries de flexões até a falha.")
            )),
            PredefinedDivision("Dia 3: Corrida Longa", listOf(
                PredefinedNota("Resistência", "Corra por 40 minutos ou mais em um ritmo lento e constante. O objetivo é a distância, não a velocidade.")
            ))
        )
    )
}