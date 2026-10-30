# Respostas — Perfil tecnico do app (arquitetura)

- **q1** (text) — Pacote base e nome do app. Default/recomendado: manter pacote com.foli.listadetarefas (namespace e applicationId) e nome de exibicao 'Lista de tarefas'. Responda 'manter' ou informe o pacote/nome desejado.
  - manter
- **q2** (single_choice) — Estrutura de modulos (multi-modulo e o default da squad; preciso saber o grau de granularidade)
  - Modulo unico :app com pacotes por camada (sem boundaries fisicas)
- **q3** (single_choice) — SDKs minimo e de compilacao (o projeto ja usa minSdk 30 e target/compileSdk 37; a PRD pede aparelho de referencia Android 13/API 33)
  - Recomendado - Manter minSdk 30, targetSdk 37, compileSdk 37 (como esta no projeto)
- **q4** (single_choice) — Injecao de dependencia
  - Recomendado - Hilt (KSP) com modulos por camada (default da squad)
- **q5** (single_choice) — Persistencia local (tarefas, etiquetas e preferencias de ordenacao/aba)
  - Recomendado - Room (KSP) para tarefas e etiquetas + DataStore Preferences para ordenacao vigente e aba selecionada
- **q6** (multiple_choice) — Rede, telemetria e privacidade - marque tudo o que vale para o MVP (a PRD nao admite rede nem analytics remoto)
  - Recomendado - Sem :core:network e sem nenhuma dependencia de rede (app 100% offline), Recomendado - Sem SDK de analytics/crash remoto; eventos apenas como object AnalyticsEvents local no-op em snake_case, Recomendado - allowBackup=false e ausencia de dataExtractionRules de nuvem (nenhum dado sai do aparelho), Recomendado - Nenhuma permissao sensivel no AndroidManifest (sem INTERNET, sem notificacoes, sem localizacao), Recomendado - Logs apenas em build debug, sem titulo/descricao/nome de etiqueta (sem PII)
- **q7** (single_choice) — Padrao de apresentacao e navegacao (MVI state/action/effect e o default da squad; o design tem S1 raiz e S2/S3 empilhadas + sheets)
  - Recomendado - MVI (State imutavel + Action + Effect one-shot) + Navigation Compose com rotas type-safe (@Serializable + kotlinx.serialization)
- **q8** (multiple_choice) — Estrategia de testes e qualidade - marque o que deve entrar no PROFILE.md
  - Recomendado - Unitarios de ViewModel/reducer/use cases com kotlinx-coroutines-test + Turbine
