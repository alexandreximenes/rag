# PoC RAG (Proof of Concept)

Visão geral
- Projeto Java Maven que demonstra um fluxo RAG \(Retrieval-Augmented Generation\) integrado com PDFs e memória em H2.
- Módulo principal: `poc-rag`.

Releases
- v0.1.0 \- PoC inicial: estrutura Maven, ingestão básica de PDFs.
- v0.2.0 \- Persistência H2 para memória de chat; scripts de schema adicionados.
- v0.3.0 \- Adicionados controllers `RagController`, `RhController`, `SpringAIQuestionController`.
- v0.4.0 \- Configuração de `AIProperties` refatorada; templates de prompt reorganizados.
- 24/01/2025 
  - Adicionando Pre-Retrieve com WebSearch utiliznado Tavily
  - Adicionando Post-Retrieve com LLM utilizando Mascara nos dados sensiveis