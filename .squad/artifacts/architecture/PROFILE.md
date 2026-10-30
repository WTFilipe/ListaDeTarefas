---
artifact: profile
phase: architecture
version: 2
author: architect
status: in_review
reviewer: chaos-architect
verdict: pending
next_action: chaos-architect
summary: "Perfil técnico Android: módulo único em camadas, MVI com UI models compartilhados, domínio puro, Hilt+Room+DataStore, 100% offline sem telemetria e gate com teste de arquitetura."
updated_at: 2026-10-30T06:40:00Z
---

# PROFILE — Decisões técnicas do projeto

Perfil consolidado a partir de `.squad/human/ANSWERS.md` (q1–q8). Cada decisão
abaixo é normativa para o desenvolvimento; o racional completo está em
`.squad/artifacts/architecture/ARCHITECTURE.md` e nos ADRs.

> **Correção da v2 após o review do Chaos Architect (`CHAOS_REVIEW.md` v1):**
> - **B1** UI models compartilhados passam a viver em `ui/model/` (primitivos +
>   `ui/UiText`), consumidos por features **e** por `ui/designsystem`; nenhuma
>   feature referencia outra (§3, §4, §11).
> - **B2** domínio puro: a chave de cor virou `core/palette/LabelColorKey` (Sem
>   Android/`R.string`); a tradução para `Color`/string acontece em
>   `ui/theme/LabelPalette` + `ui/designsystem`; `UiText` é tipo **de UI**
>   (`ui/UiText.kt`) e o domínio retorna apenas `AppFailure` (§3, §4, §11).
> - **M1** frontmatter corrigido (`in_review` + `chaos-architect`).
> - **M4** gate ganha teste de arquitetura por tarefa e checklist de
>   acessibilidade por história (§9).
> - **M5** lista normativa de eventos centralizada em `ARCHITECTURE.md` §10 e
>   `search_cleared` incluído (§8).
> - **m1** `core/di/` existe na árvore e `core/ui` deixou de existir (§3, §5).
> - **m2** um único `LabelSelectorRow` no design system (§4, §11).

## 1. Identidade do app

| Item | Decisão | Origem |
|:--|:--|:--|
| Nome de exibição | `Lista de tarefas` (pt-BR) | ANSWERS q1 ("manter") |
| Pacote base / `namespace` | `com.foli.listadetarefas` | ANSWERS q1 |
| `applicationId` | `com.foli.listadetarefas` (inalterado) | ANSWERS q1 |
| `rootProject.name` | `ListaDeTarefas` | `settings.gradle.kts` |
| Idioma da UI | pt-BR, todas as strings em `res/values/strings.xml` | PRD §6 |
| Tema | claro/escuro seguindo o sistema, `dynamicColor = false`, sem seletor no app | DESIGN_SPEC §1, COLOR_SCHEME §1 |

## 2. Plataforma e SDKs

| Item | Decisão |
|:--|:--|
| `minSdk` | **30** |
| `targetSdk` | **37** |
| `compileSdk` | **37** (`compileSdk { version = release(37) }`) |
| Linguagem | Kotlin (`kotlin = 2.2.10` no catálogo), JVM target 11 |
| UI | Jetpack Compose + Material 3 (`composeBom = 2026.02.01`, material3 ≥ 1.2 por causa de `surfaceContainer*`) |
| Dispositivo de referência | Celular de faixa intermediária, Android 13 / API 33, 4 GB de RAM (PRD §6) |
| Formato de dispositivo | Celular apenas; tablet/Wear fora do MVP |

Justificativa: `minSdk 30` mantém `java.time` sem desugaring e permite
`androidx.compose` atual; `target/compileSdk 37` é o que o projeto já declara.
Origem: ANSWERS q3 (opção "Recomendado — manter").

## 3. Módulos e estrutura

**Decisão: módulo Gradle único `:app`**, com separação por **pacotes por camada**
(sem *boundaries* físicas). É uma exceção consciente ao default multimódulo da
squad, escolhida pelo humano na ANSWERS q2 e mitigada pelo teste de arquitetura
do §9 (ADR-0001).

```
:app
  com.foli.listadetarefas
    core/        (resultado, tempo, normalização, paleta de chaves, analytics local, DI transversal)
    domain/      (modelos, contratos de repositório, regras puras, use cases)
    data/        (Room, DataStore, mappers, implementações de repositório)
    di/          (módulos Hilt de composição)
    ui/          (UiText, ui/model, theme, designsystem, navigation, feature/*)
```

Regras equivalentes às de multimódulo, aplicadas por pacote:

- `ui` depende de `domain` e de `core`; nunca de `data`.
- `ui/feature/*` depende de `ui/model`, `ui/designsystem`, `ui/theme` e `domain`;
  **nunca** de `data`, `di` ou de outra `ui/feature/*`.
- `ui/designsystem` depende de `ui/model`, `ui/theme` e `ui/UiText`; **nunca** de
  `domain`, `data` ou features.
- `ui/model` contém apenas primitivos, enums de UI e `UiText`; não importa
  `domain`, `data` nem Compose de layout.
- `data` implementa contratos de `domain` e depende de `domain` e de `core`.
- `domain` é Kotlin puro: sem Android, `R.string`, Room, Compose, DataStore ou
  Hilt. Apenas `javax.inject.Inject` no construtor de use cases, documentado nos
  ADRs. Retorna `AppResult`/`AppFailure`, nunca `UiText`.
- `core` não depende de `domain`, `data` ou `ui`.
- Sem ciclos de dependência (verificado pelo `ArchitectureRulesTest` em cada
  tarefa).

ADR: `ADR-0001-modulo-unico-por-camadas.md`,
`ADR-0012-ui-models-compartilhados.md`.

## 4. Apresentação: MVI

| Item | Decisão |
|:--|:--|
| Padrão | MVI com UDF: `State` imutável + `Action` (intenção) + `Effect` (one-shot) |
| ViewModel | Um por tela (`TaskListViewModel`, `TaskEditorViewModel`, `LabelListViewModel`) |
| Exposição | `StateFlow<State>` + `Channel<Effect>` como `Flow<Effect>` |
| Coleta | `collectAsStateWithLifecycle()` (lifecycle-runtime-compose) |
| Reducer | Função pura `reduce(state, action)` / projeção pura a partir de fluxos |
| UI models | `TaskUiModel`, `LabelUiModel`, `DeadlineUiState`, `LabelEditorUiModel` em `ui/model`, preenchidos pelas features a partir do domínio |
| Textos de UI | `UiText` em `ui/UiText.kt` (recurso + args / plano); o domínio nunca o usa |
| Sheets/dialogs | Estado de UI (S4, S5, S6, S7, S8), não destinos de navegação |
| Regra | Nenhum tipo de `data`/Room/Android na UI; nenhuma leitura de relógio na UI |

Origem: ANSWERS q7 ("Recomendado — MVI + Navigation Compose type-safe"). ADRs:
`ADR-0002-mvi-state-action-effect.md`, `ADR-0004-navegacao-type-safe.md`.

## 5. Injeção de dependência

- **Hilt com KSP** (`com.google.dagger.hilt.android` + `ksp`), `@HiltAndroidApp`
  em `ListaDeTarefasApplication`, `@AndroidEntryPoint` em `MainActivity`.
- Módulos por camada: `DatabaseModule`, `DataStoreModule`, `RepositoryModule`,
  `DispatcherModule`, `TimeModule`, `AnalyticsModule` em `di/`; os qualificadores
  `@IoDispatcher` e `@DefaultDispatcher` ficam em `core/di/DispatcherQualifiers.kt`.
- Use cases com `@Inject constructor`; nenhuma dependência resolvida em
  composables (sempre `hiltViewModel()`).
- Hilt é a **única** exceção de framework Android tolerada em `domain` (anotação
  `@Inject` no construtor do use case).

Origem: ANSWERS q4. ADR: `ADR-0003-hilt-com-ksp.md`.

## 6. Rede, telemetria e privacidade

| Item | Decisão |
|:--|:--|
| Rede | **Nenhuma**. Sem `:core:network`, sem Retrofit/OkHttp/Ktor/GraphQL, sem `kotlinx-serialization` para payload remoto |
| Permissões | Nenhuma permissão declarada no `AndroidManifest.xml` (sem `INTERNET`, sem notificações, localização, câmera) |
| Analytics | Sem SDK. Apenas `object AnalyticsEvents` + `NoOpAnalyticsTracker` locais, no-op |
| Crash reporting | Nenhum SDK remoto |
| Backup | `android:allowBackup="false"`, sem `dataExtractionRules`/`fullBackupContent` de nuvem |
| Logs | Apenas em build `debug`, via wrapper próprio, nunca com título, descrição, nome de etiqueta ou consulta de busca |
| Offline | 100%: todo dado em Room/DataStore no aparelho |

Origem: ANSWERS q6 (todas as opções marcadas) e PRD §6. ADR:
`ADR-0006-offline-sem-telemetria.md`.

## 7. Persistência

| Dado | Onde | Detalhe |
|:--|:--|:--|
| Tarefas | **Room** (`task` table) | PK `String` (UUID), título, descrição, `label_id` (FK `SET_NULL`), `due_date` (epochDay), `created_at`, `completed_at` (epoch millis) |
| Etiquetas | **Room** (`label` table) | PK `String` (UUID), nome, `name_key` (normalizado) com índice único, `color_key` = nome de `LabelColorKey` |
| Ordenação vigente e aba selecionada | **DataStore Preferences** | chaves `sort_order`, `selected_tab` (nomes de enum) |
| Consulta de busca | `SavedStateHandle` do `TaskListViewModel` | não é persistida em disco; sobrevive à navegação S1 → S2/S3 |

- Room com KSP, `exportSchema = true`, schema em `app/schemas/`, versão **1**
  (projeto sem dados legados).
- Consultas expostas como `Flow`, coletadas em `Dispatchers.IO`.
- Exclusão de etiqueta em `@Transaction` (tarefas ficam `label_id = NULL` antes do
  delete), sem depender só da FK.
- Escritas idempotentes: `upsert` com PK gerada uma única vez por formulário —
  "Tentar novamente" não duplica tarefa (PRD US-01, caso de borda).

Origem: ANSWERS q5. ADR: `ADR-0005-room-e-datastore.md`.

## 8. Eventos (analytics local no-op)

- **A lista normativa de eventos e parâmetros é `ARCHITECTURE.md` §10**; esta
  seção é um resumo e não pode divergir dela.
- Nomes em `snake_case`, centralizados em `object AnalyticsEvents`; parâmetros em
  `snake_case`, nunca PII.
- Eventos: `task_created_success`, `task_updated`, `task_deleted`,
  `task_completed`, `task_reopened`, `label_created`, `label_updated`,
  `label_deleted`, `task_label_changed`, `deadline_set` (só `has_deadline`),
  `deadline_cleared`, `search_performed` (só `result_count`), `search_cleared`,
  `sort_changed` (só o enum da opção).
- `AnalyticsTracker` é uma interface com implementação `NoOpAnalyticsTracker`
  ligada por Hilt; nenhum evento é critério de aceite e nenhum sai do aparelho.

## 9. Testes e qualidade

| Camada | Estratégia |
|:--|:--|
| Arquitetura (pacotes) | `ArchitectureRulesTest` (JUnit, lê imports/fontes) no source set de teste — **obrigatório no gate por tarefa** |
| Domínio puro (prazo, busca, ordenação, validação) | JUnit 4 + `kotlinx-coroutines-test`, casos de fronteira de data e pt-BR |
| ViewModel | `kotlinx-coroutines-test` + **Turbine** (estado e effects) |
| Repositórios/DAO | Testes JVM com Room em memória (Robolectric) — recomendados; não bloqueiam tarefa |
| UI | Testes instrumentados de semântica obrigatórios nas tarefas de linha (US-01) e chip/grade (US-04); checklist manual de acessibilidade por história |
| Cobertura | Sem meta percentual cega; **fluxos críticos das 7 histórias cobertos** |

- Gate obrigatório por tarefa: `./gradlew assembleDebug testDebugUnitTest` verde
  (inclui o teste de arquitetura), sem regressão nos testes já existentes.
- `ArchitectureRulesTest` cobre: `ui.feature.*` não importa `data`/`di`/outra
  feature; `ui.designsystem` não importa `domain`/`data`; `domain` não importa
  `android.*`, `androidx.*` (exceto `javax.inject`), `retrofit`, `room` ou
  `compose`; `core` não importa `domain`/`data`/`ui`.
- Acessibilidade: as histórias US-01 e US-04 incluem uma tarefa com testes de
  semântica (texto integral da linha, chip truncado, `contentDescription` de
  ações) e um checklist manual (TalkBack, fonte 200%, alvos ≥ 48dp) registrado no
  `IMPLEMENTATION.md`. Instrumentados rodam em emulador
  (`connectedDebugAndroidTest`) e não bloqueiam o gate unitário por tarefa.
- Observação: a ANSWERS q8 marcou apenas testes unitários como obrigatórios; por
  isso o gate por tarefa é unitário e a verificação de semântica é exigida em
  tarefa própria com checklist manual.

ADRs: `ADR-0009-testes-unitarios-e-qualidade.md`.

## 10. Segurança e compliance

- Sem segredos no repositório: nenhum token, chave ou `.env`; sem keystore
  versionada.
- Sem PII em `.squad/`, logs, commits ou MRs.
- Sem tráfego de rede ⇒ nenhum requisito de HTTPS/TLS a implementar (proibido
  adicionar permissão `INTERNET`).
- `allowBackup=false` e nenhum `dataExtractionRules` de nuvem: dados não saem do
  aparelho, inclusive em backup do Google.
- Permissões mínimas: nenhuma permissão declarada.
- Mensagens de erro de armazenamento sempre não técnicas (nunca citar banco/SQL).
- `android:exported="true"` apenas na `MainActivity` com launcher intent.

## 11. Nomenclatura e estrutura (resumo)

- Pacotes `lowercase`: `com.foli.listadetarefas.<camada>.<área>`; features em
  `ui.feature.<feature>` (`tasks`, `editor`, `labels`).
- UI models compartilhados em `ui/model/` (`TaskUiModel`, `LabelUiModel`,
  `DeadlineUiState`, `LabelEditorUiModel`); nenhum deles depende de `domain`.
- Textos de UI: `ui/UiText.kt`; o domínio retorna apenas `AppFailure`.
- Chaves de cor: `core/palette/LabelColorKey` (8 valores, Kotlin puro). Os
  modelos de domínio (`TaskLabel`) e de UI (`LabelUiModel`, `LabelEditorUiModel`)
  guardam apenas a chave; `R.string`/`Color` só em `ui/theme/LabelPalette` e
  `ui/designsystem`. Não existe enum `LabelColor` em `domain`.
- Arquivos: um por declaração pública principal; nome = nome da declaração.
- Classes `PascalCase`; funções/variáveis `camelCase`; constantes
  `UPPER_SNAKE_CASE`; composables `PascalCase`; booleanos `isX`/`hasX`/`canX`.
- Sufixos obrigatórios: `XxxState`, `XxxAction`, `XxxEffect`, `XxxViewModel`,
  `XxxScreen`, `XxxRoute`, `XxxUiModel`, `XxxUseCase`, `XxxRepository`,
  `XxxRepositoryImpl`, `XxxEntity`, `XxxDao`, `XxxMapper`, `XxxModule`,
  `XxxDataSource`.
- Recursos: `strings.xml` com chaves em `snake_case` (as chaves do
  `DESIGN_SPEC.md` §11 são normativas); cores nomeadas por **papel**
  (`PrimaryLight`, `SurfaceContainerHighDark`), nunca por matiz.
- A árvore concreta de arquivos está no `ARCHITECTURE.md` §3.

## 12. Build

- Gradle Kotlin DSL, `gradle/libs.versions.toml` como única fonte de versões.
- Sem `build-logic` (módulo único); plugins no `build.gradle.kts` raiz com
  `apply false` e aplicados em `:app`.
- Plugins: `com.android.application`, `org.jetbrains.kotlin.plugin.compose`,
  `org.jetbrains.kotlin.plugin.serialization`, `com.google.devtools.ksp`,
  `com.google.dagger.hilt.android`.
- `buildFeatures { compose = true; buildConfig = true }`.
- Versões das novas bibliotecas (Room, Hilt, KSP, DataStore, Navigation,
  Coroutines, Turbine) são fixadas pelo Developer na tarefa, respeitando
  `kotlin = 2.2.10` e `composeBom = 2026.02.01`; o Architect não inventa números.
- `testOptions { unitTests.isReturnDefaultValues = false }`; o
  `ArchitectureRulesTest` lê as fontes a partir do diretório do projeto e roda no
  `testDebugUnitTest` sem dependência nova.
- Release mantém o `optimization { enable = true; ... }` já configurado e usa
  shrinking para remover código morto.

ADR: `ADR-0010-build-e-catalogo.md`.

## 13. Restrições de desempenho (herdadas da PRD)

- Abertura a frio até lista utilizável ≤ 2 s em build release (API 33, 4 GB).
- Lista com 500 tarefas: frame ≤ 16 ms em rolagem (build release).
- Busca: debounce **200 ms** (limite da PRD ≤ 250 ms) e resultado ≤ 300 ms.
- Criar/editar/excluir refletidos na UI ≤ 150 ms.
- Nenhum trabalho de I/O na main thread; filtro/ordenação em memória são puros e
  O(n log n) sobre ≤ 500 itens.

## 14. Rastreabilidade das decisões

| ID | Pergunta | Resposta | Onde virou decisão |
|:--|:--|:--|:--|
| q1 | Pacote/nome | `com.foli.listadetarefas` / "Lista de tarefas" | §1 |
| q2 | Módulos | Módulo único `:app` por camadas | §3, ADR-0001 |
| q3 | SDKs | minSdk 30 / target 37 / compile 37 | §2 |
| q4 | DI | Hilt (KSP) | §5, ADR-0003 |
| q5 | Persistência | Room + DataStore Preferences | §7, ADR-0005 |
| q6 | Rede/telemetria | Sem rede, sem SDK, sem backup, sem permissões, logs só debug | §6, §10, ADR-0006 |
| q7 | Apresentação | MVI + Navigation Compose type-safe | §4, ADR-0002, ADR-0004 |
| q8 | Testes | Unitários de ViewModel/reducer/use case com Turbine | §9, ADR-0009 |
| review v1 | Correções B1/B2/M1–M5/m1–m4 | UI models em `ui/model`, domínio puro, gate de arquitetura, eventos alinhados | §3, §4, §8, §9, ADR-0012 |
