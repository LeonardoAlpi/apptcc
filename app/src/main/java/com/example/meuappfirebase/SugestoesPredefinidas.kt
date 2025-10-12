package com.example.meuappfirebase

// Este objeto funcionará como nosso banco de dados local e estático de sugestões.
object SugestoesPredefinidas {

    fun getSugestoes(): List<Sugestao> {
        return listOf(
            Sugestao(
                categoria = "LEITURA",
                titulo = "O Poder do Hábito",
                descricao = "Entenda a ciência por trás de como os hábitos se formam e como você pode mudá-los para melhor.",
                passos = listOf("Leia o primeiro capítulo.", "Anote um hábito que você gostaria de mudar.", "Identifique a 'deixa' e a 'recompensa' desse hábito.")
            ),
            Sugestao(
                categoria = "DIETA",
                titulo = "Hidratação Inteligente",
                descricao = "Comece o dia com um copo de água antes de qualquer outra coisa. Isso ajuda a ativar o metabolismo e a hidratar o corpo após o sono.",
                passos = listOf("Deixe um copo de água ao lado da cama à noite.", "Beba-o assim que acordar.", "Tente fazer isso por 3 dias seguidos.")
            ),
            Sugestao(
                categoria = "MEDITACAO",
                titulo = "Meditação de 5 Minutos",
                descricao = "Uma pausa curta para focar na sua respiração pode reduzir drasticamente o estresse e aumentar o foco.",
                passos = listOf("Encontre um lugar silencioso.", "Sente-se confortavelmente e feche os olhos.", "Concentre-se apenas no ar entrando e saindo por 5 minutos.")
            ),
            Sugestao(
                categoria = "RESPIRACAO",
                titulo = "Técnica da Respiração Quadrada",
                descricao = "Uma técnica simples para acalmar o sistema nervoso em momentos de ansiedade ou estresse.",
                passos = listOf("Inspire por 4 segundos.", "Segure o ar por 4 segundos.", "Expire por 4 segundos.", "Fique sem ar por 4 segundos. Repita 5 vezes.")
            ),
            Sugestao(
                categoria = "PODCASTS",
                titulo = "Podcast 'Autoconsciente'",
                descricao = "Ouça um episódio sobre inteligência emocional para entender melhor suas reações e as dos outros.",
                passos = listOf("Procure por 'Autoconsciente' no seu player de podcast.", "Ouça um episódio que chame sua atenção durante uma caminhada ou no trânsito.")
            ),
            Sugestao(
                categoria = "SAUDE_MENTAL_ESTRESSE",
                titulo = "Pausa Ativa de 2 Minutos",
                descricao = "A cada hora de trabalho ou estudo, levante-se e se alongue por dois minutos. Isso alivia a tensão muscular e mental.",
                passos = listOf("Coloque um alarme para tocar a cada hora.", "Levante-se e estique os braços, pescoço e pernas.", "Olhe para um ponto distante pela janela.")
            )
            // Você pode adicionar quantas outras sugestões quiser aqui!
        )
    }
}