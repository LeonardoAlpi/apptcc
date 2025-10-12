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
                categoria = "LEITURA",
                titulo = "Hábitos Atômicos - James Clear",
                descricao = "Um guia prático sobre como criar bons hábitos e quebrar os maus. Aprenda a fazer pequenas mudanças que geram resultados gigantescos.",
                passos = listOf("Leia sobre o conceito de 'melhorar 1% a cada dia'.", "Escolha um hábito pequeno que você queira implementar.", "Use a técnica de 'empilhamento de hábitos': conecte seu novo hábito a um já existente.")
            ),

            Sugestao(
                categoria = "LEITURA",
                titulo = "A Coragem de Ser Imperfeito - Brené Brown",
                descricao = "Entenda como abraçar a vulnerabilidade e a imperfeição pode te levar a uma vida mais plena e corajosa. Um livro transformador sobre pertencimento e autoestima.",
                passos = listOf("Leia a introdução para entender o que a autora define como vulnerabilidade.", "Pense em uma área da sua vida onde você tem medo de falhar.", "Pratique um pequeno ato de coragem hoje, como expressar uma opinião ou pedir ajuda.")
            ),

            Sugestao(
                categoria = "LEITURA",
                titulo = "Essencialismo - Greg McKeown",
                descricao = "Aprenda a disciplinada busca por menos. Este livro ensina como identificar o que é vitalmente importante e eliminar todo o resto para focar sua energia.",
                passos = listOf("Leia o primeiro capítulo sobre a diferença entre ser essencialista e não-essencialista.", "Liste suas tarefas para o dia de amanhã.", "Escolha UMA tarefa que não é essencial e elimine-a da sua lista.")
            ),

            Sugestao(
                categoria = "LEITURA",
                titulo = "Trabalho Focado (Deep Work) - Cal Newport",
                descricao = "Descubra como cultivar a habilidade de se concentrar sem distrações em um mundo cheio delas. Uma leitura essencial para quem busca produtividade e qualidade.",
                passos = listOf("Entenda a diferença entre 'Trabalho Focado' e 'Trabalho Superficial'.", "Agende um bloco de 45 minutos no seu dia para se dedicar a uma única tarefa.", "Durante esse bloco, coloque o celular em modo avião e feche abas desnecessárias.")
            ),

            Sugestao(
                categoria = "LEITURA",
                titulo = "Mindset - Carol S. Dweck",
                descricao = "A psicóloga Carol Dweck revela o poder da nossa mentalidade. Entenda a diferença entre o 'mindset fixo' e o 'mindset de crescimento' e como isso afeta todas as áreas da vida.",
                passos = listOf("Leia sobre as características do mindset de crescimento.", "Identifique um desafio que você está enfrentando atualmente.", "Encare esse desafio com a mentalidade de que você pode aprender e melhorar, em vez de focar apenas no resultado.")
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
            ),
            Sugestao(
                categoria = "DIETA",
                titulo = "Planeje a Primeira Refeição",
                descricao = "Decidir o que você vai comer no café da manhã na noite anterior evita escolhas apressadas e pouco saudáveis pela manhã.",
                passos = listOf("Antes de dormir, pense no seu café da manhã.", "Deixe os ingredientes já separados (ex: aveia, frutas).", "Siga o plano ao acordar.")
            ),
            Sugestao(
                categoria = "DIETA",
                titulo = "Mastigue Mais Devagar",
                descricao = "Comer devagar melhora a digestão e aumenta a sensação de saciedade, ajudando a controlar a quantidade de comida ingerida.",
                passos = listOf("Descanse os talheres na mesa entre uma garfada e outra.", "Concentre-se em mastigar completamente a comida.", "Tente fazer sua refeição durar pelo menos 20 minutos.")
            ),
            Sugestao(
                categoria = "DIETA",
                titulo = "Inclua uma Fonte de Proteína",
                descricao = "Adicionar uma fonte de proteína no café da manhã (como ovo ou iogurte) ajuda a manter a saciedade por mais tempo ao longo da manhã.",
                passos = listOf("Escolha uma fonte de proteína de sua preferência.", "Adicione ao seu café da manhã habitual.", "Observe como sua fome se comporta até o almoço.")
            ),

// Categoria: MEDITACAO
            Sugestao(
                categoria = "MEDITACAO",
                titulo = "Meditação da Gratidão",
                descricao = "Foque sua atenção em coisas pelas quais você é grato. Essa prática simples pode mudar sua perspectiva e melhorar seu humor.",
                passos = listOf("Encontre uma posição confortável.", "Feche os olhos e respire fundo 3 vezes.", "Mentalmente, liste 3 coisas pelas quais você se sente grato hoje.")
            ),
            Sugestao(
                categoria = "MEDITACAO",
                titulo = "Atenção Plena no Banho",
                descricao = "Transforme um ato automático em um momento de mindfulness. Use o banho para se reconectar com seu corpo e seus sentidos.",
                passos = listOf("Deixe o celular longe do banheiro.", "Sinta a temperatura da água na sua pele.", "Preste atenção no cheiro do sabonete e no som da água.")
            ),
            Sugestao(
                categoria = "MEDITACAO",
                titulo = "Meditação Guiada para Iniciantes",
                descricao = "Se você tem dificuldade em meditar sozinho, use um áudio guiado. Ele te dará as instruções necessárias para relaxar e focar.",
                passos = listOf("Procure por 'meditação guiada para iniciantes' no YouTube ou Spotify.", "Escolha um áudio de 5 a 10 minutos.", "Siga as instruções do guia sem se julgar.")
            ),

// Categoria: RESPIRACAO
            Sugestao(
                categoria = "RESPIRACAO",
                titulo = "Respiração 4-7-8",
                descricao = "Uma técnica poderosa para relaxamento e para ajudar a induzir o sono, criada pelo Dr. Andrew Weil.",
                passos = listOf("Inspire pelo nariz por 4 segundos.", "Segure o ar nos pulmões por 7 segundos.", "Expire completamente pela boca, fazendo um som de 'sopro', por 8 segundos. Repita 3 vezes.")
            ),
            Sugestao(
                categoria = "RESPIRACAO",
                titulo = "Respiração Coerente",
                descricao = "Equilibre seu sistema nervoso e acalme a mente sincronizando sua respiração. O objetivo é fazer cerca de 5 respirações por minuto.",
                passos = listOf("Sente-se de forma confortável e reta.", "Inspire lentamente pelo nariz contando até 6.", "Expire lentamente pelo nariz contando até 6. Continue por 3 a 5 minutos.")
            ),

// Categoria: PODCASTS
            Sugestao(
                categoria = "PODCASTS",
                titulo = "Podcast sobre Hábitos",
                descricao = "Ouça um episódio do podcast 'Loop Matinal' ou similar para se inspirar a construir rotinas mais saudáveis e produtivas.",
                passos = listOf("Procure por podcasts com temas de 'hábitos' ou 'desenvolvimento pessoal'.", "Escolha um episódio curto para começar.", "Anote uma dica que você pode aplicar na sua vida.")
            ),
            Sugestao(
                categoria = "PODCASTS",
                titulo = "Aprenda Algo Novo",
                descricao = "Use o formato de podcast para aprender sobre um tópico totalmente novo para você, como história, finanças ou ciência.",
                passos = listOf("Pense em um assunto que desperta sua curiosidade.", "Busque por 'podcast sobre [assunto]'.", "Ouça um episódio durante um trajeto ou enquanto faz tarefas domésticas.")
            ),

// Categoria: SAUDE_MENTAL_ESTRESSE
            Sugestao(
                categoria = "SAUDE_MENTAL_ESTRESSE",
                titulo = "Diário de 'Despejo Mental'",
                descricao = "Antes de dormir, escreva tudo o que está em sua mente. Isso ajuda a organizar os pensamentos e a aliviar a ansiedade.",
                passos = listOf("Pegue um caderno e uma caneta.", "Escreva livremente por 5 minutos, sem se preocupar com a forma.", "Feche o caderno, deixando as preocupações na página.")
            ),
            Sugestao(
                categoria = "SAUDE_MENTAL_ESTRESSE",
                titulo = "Caminhada de 15 Minutos",
                descricao = "Uma pequena caminhada ao ar livre pode melhorar seu humor, reduzir o estresse e aumentar a criatividade.",
                passos = listOf("Coloque um tênis.", "Saia para uma caminhada pelo seu bairro sem um destino certo.", "Preste atenção ao seu redor, em vez de olhar para o celular.")
            ),
            Sugestao(
                categoria = "SAUDE_MENTAL_ESTRESSE",
                titulo = "A Regra dos 2 Minutos",
                descricao = "Se uma tarefa leva menos de dois minutos, faça-a imediatamente. Isso evita o acúmulo de pequenas pendências que geram estresse mental.",
                passos = listOf("Identifique uma pequena tarefa (ex: lavar a xícara do café).", "Pergunte-se: 'Leva menos de 2 minutos?'.", "Se sim, faça agora mesmo.")
            ),
            Sugestao(
                categoria = "SAUDE_MENTAL_ESTRESSE",
                titulo = "Primeira Hora Sem Celular",
                descricao = "Comece o dia com seus próprios pensamentos, não com as notificações dos outros. Isso define um tom mais calmo para o resto do dia.",
                passos = listOf("Deixe o celular longe da cama ao dormir.", "Ao acordar, resista ao impulso de pegá-lo.", "Faça sua rotina matinal antes de checar o celular.")
            ),
            Sugestao(
                categoria = "SAUDE_MENTAL_ESTRESSE",
                titulo = "Escaneamento Corporal",
                descricao = "Uma técnica de atenção plena para se reconectar com seu corpo e identificar pontos de tensão para poder relaxá-los.",
                passos = listOf("Deite-se confortavelmente.", "Preste atenção em cada parte do seu corpo, dos pés à cabeça.", "Note as sensações (tensão, relaxamento) sem julgamento.")
            ),
            Sugestao(
                categoria = "SAUDE_MENTAL_ESTRESSE",
                titulo = "Refeição sem Telas",
                descricao = "Reconecte-se com sua comida. Comer sem a distração do celular ou da TV melhora a digestão e a percepção de saciedade.",
                passos = listOf("Determine que uma refeição do seu dia será sem telas.", "Deixe o celular em outro cômodo.", "Foque nos sabores, cheiros e texturas da sua comida.")
            ),
            Sugestao(
                categoria = "SAUDE_MENTAL_ESTRESSE",
                titulo = "Organize um Pequeno Espaço",
                descricao = "A desordem externa pode contribuir para o estresse interno. Organizar um pequeno espaço, como sua mesa de trabalho ou uma gaveta, traz uma sensação de controle.",
                passos = listOf("Escolha uma área pequena e bagunçada.", "Dedique 10 minutos para organizar apenas aquele local.", "Aprecie a sensação de ordem e clareza.")
            )
            // Você pode adicionar quantas outras sugestões quiser aqui!
        )
    }
}