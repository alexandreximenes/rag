package com.ia.poc_rag.loader;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.data-loader", name = "loader-file-enabled", havingValue = "true", matchIfMissing = false)
public class RandomDataLoader {

    private static final Logger log = LoggerFactory.getLogger(RandomDataLoader.class);
    private final VectorStore vectorStore;

    public RandomDataLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void loadSentencesIntoVectorStore() {
        log.info("Carregando Random Data Loader no Vector Store...");
        List<String> sentences = List.of(
                "Java é usado para construir aplicações corporativas escaláveis.",
                "Python é comumente usado para tarefas de aprendizado de máquina e automação.",
                "JavaScript é essencial para criar páginas web interativas.",
                "Docker empacota aplicações em contêineres leves.",
                "Kubernetes automatiza a orquestração de contêineres em larga escala.",
                "Redis é um armazenamento de dados em memória usado para cache.",
                "PostgreSQL oferece suporte a consultas complexas e total conformidade com ACID.",
                "Kafka é uma plataforma distribuída de streaming de eventos.",
                "APIs REST permitem comunicação cliente-servidor sem estado.",
                "GraphQL permite que os clientes busquem exatamente os dados de que precisam.",
                "Pontuações de crédito influenciam as taxas de juros de empréstimos.",
                "Fundos mútuos reúnem dinheiro de investidores para comprar títulos.",
                "O Bitcoin opera em uma rede descentralizada ponto a ponto.",
                "O Ethereum oferece suporte à implantação de contratos inteligentes.",
                "O mercado de ações abre às 9h30 (EST) nos dias úteis.",
                "Os juros compostos aumentam os retornos dos investimentos ao longo do tempo.",
                "Diversificar investimentos reduz o risco geral.",
                "Blockchain é um livro-razão distribuído e imutável de transações.",
                "A fotossíntese é o processo pelo qual as plantas convertem a luz solar em energia.",
                "O ciclo da água envolve evaporação, condensação e precipitação.",
                "A camada de ozônio protege a Terra dos raios ultravioleta nocivos.",
                "A Terra gira em torno do Sol em uma órbita elíptica.",
                "Relâmpagos são descargas de eletricidade causadas por nuvens carregadas.",
                "O DNA é a molécula que carrega as instruções genéticas nos organismos vivos.",
                "Vulcões se formam quando o magma sobe através da crosta terrestre.",
                "Terremotos são causados por mudanças tectônicas repentinas.",
                "O Saara é o maior deserto quente do mundo.",
                "O Monte Kilimanjaro é a montanha mais alta da África.",
                "O Japão é conhecido por suas cerejeiras e tecnologia avançada.",
                "A Grande Muralha da China tem mais de 13.000 milhas de extensão.",
                "As Cataratas do Niágara estão localizadas entre o Canadá e os Estados Unidos.",
                "O Rio Amazonas é o segundo rio mais longo do mundo.",
                "A aveia é rica em fibras e ajuda a reduzir o colesterol.",
                "Beber água melhora a digestão e a saúde da pele.",
                "Uma dieta equilibrada inclui proteínas, carboidratos, gorduras e vitaminas.",
                "O brócolis é rico em vitaminas A, C e K.",
                "O chá verde contém antioxidantes benéficos para o metabolismo.",
                "O consumo excessivo de açúcar aumenta o risco de diabetes.",
                "Caminhar 30 minutos por dia melhora a saúde cardiovascular.",
                "A meditação pode reduzir o estresse e melhorar o foco.",
                "Manter um diário de gratidão está associado a níveis mais altos de felicidade.",
                "Exercícios de respiração profunda ajudam a regular a ansiedade.",
                "Ler diariamente melhora o vocabulário e a função cognitiva.",
                "Definir metas diárias aumenta a produtividade.",
                "STEM significa Ciência, Tecnologia, Engenharia e Matemática.",
                "A taxonomia de Bloom categoriza objetivos educacionais.",
                "A aprendizagem baseada em projetos aumenta o engajamento dos alunos.",
                "Cursos online oferecem flexibilidade para estudantes remotos.",
                "Flashcards são eficazes para memorizar vocabulário.",
                "A metodologia Ágil promove o desenvolvimento de software iterativo.",
                "OKRs ajudam a alinhar os objetivos da equipe com a estratégia do negócio.",
                "O trabalho remoto oferece flexibilidade, mas exige comunicação clara.",
                "Sistemas de CRM gerenciam o relacionamento com clientes e o funil de vendas.",
                "A análise SWOT identifica forças, fraquezas, oportunidades e ameaças."
        );

        if(vectorStore.similaritySearch(getSimilaritySearch(sentences)).isEmpty()) {
            log.info("Carregando {} Data Loader no Vector Store.", sentences.size());
            List<Document> documents = sentences.stream()
                    .map(document -> document.replaceAll("\\s+", " ").trim())
                    .map(Document::new).toList();
            vectorStore.add(documents);
        } else {
            log.info("Nenhum carregamento necessário. Random Data Loader já existe no Vector Store.");
        }
    }

    private SearchRequest getSimilaritySearch(List<String> sentences) {
        return SearchRequest.builder().similarityThreshold(0.7).topK(3).query(sentences.getFirst()).build();
    }

    ;
}
