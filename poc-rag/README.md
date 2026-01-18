# PoC RAG (Proof of Concept)

Visão geral
- Projeto Java Maven que demonstra um fluxo RAG (Retrieval-Augmented Generation) integrado com PDFs e memória em H2.
- Estrutura principal na pasta `poc-rag`.

Estrutura do projeto (resumo)
- `poc-rag/` - módulo principal da aplicação.
- `poc-rag/src/main/java` - código-fonte Java (controllers, services, loaders, config).
- `poc-rag/src/main/resources` - recursos: `application.yaml`, `pdf/`, `promptTemplates/`, `db/`.
- `poc-rag/data/` - banco H2 persistido para memória de chat (`h2-chat-memory*.db`).
- `poc-rag/docker-compose.yml` - compose para dependências (quando aplicável).
- `poc-rag/mvnw.cmd` - wrapper Maven para Windows.

Pré-requisitos
- Java 17+ (ou versão compatível configurada no projeto).
- Maven (opcional se usar `mvnw.cmd`).
- Windows (instruções abaixo consideram Windows).
- Docker \& Docker Compose (opcional, se usar `docker-compose.yml`).

Como executar (Windows)
- Build e testes:
  - `.\mvnw.cmd clean package`
  - `.\mvnw.cmd test`
- Executar aplicação local:
  - `.\mvnw.cmd spring-boot:run`
- Ou executar jar gerado:
  - `java -jar target\poc-rag-<versao>.jar`

Configurações importantes
- Arquivo de configuração: `poc-rag/src/main/resources/application.yaml`.
- Propriedades relacionadas à AI: `poc-rag/src/main/java/com/ia/poc_rag/config/AIProperties.java`.
- Esquema do H2: `poc-rag/src/main/resources/db/chat-memory-schema.sql`.
- PDFs de exemplo: `poc-rag/src/main/resources/pdf/` (contém arquivos usados para ingestão).

Principais endpoints (exemplos)
- Controllers principais:
  - `RagController` — ponto para consultas RAG.
  - `RhController` — endpoints relacionados a RH (uso dos PDFs de políticas).
  - `SpringAIQuestionController` — perguntas gerais via Spring AI.
- Exemplos de rota (ajustar conforme `@RequestMapping` real):
  - `POST /api/rag`
  - `POST /api/rh`
  - `POST /api/spring-ai/question`

Como testar rapidamente
- Use `curl` ou Postman contra os endpoints acima após iniciar a aplicação.
- Verifique os templates de prompt em `poc-rag/src/main/resources/promptTemplates/`.

Notas
- Dados persistidos de sessão/ memória ficam em `poc-rag/data/` (H2).
- Ajuste chaves/credenciais de AI em `application.yaml` ou via variáveis de ambiente conforme `AIProperties`.
- Código e classes relevantes estão em `poc-rag/src/main/java/com/ia/poc_rag/`.

Licença e contribuição
- Arquivo de licença não incluído no repositório; adicione conforme necessário.