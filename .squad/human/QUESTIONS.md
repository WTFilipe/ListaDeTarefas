---
artifact: questions
phase: architecture
version: 1
author: architect
status: awaiting_human
verdict: pending
next_action: human
summary: "8 perguntas técnicas (pacote, módulos, SDKs, DI, persistência, rede/privacidade, MVI/navegação, testes) para fechar o PROFILE.md antes da arquitetura."
updated_at: 2026-10-30T05:10:00Z
---

# Perguntas de arquitetura — perfil técnico

Responda no dashboard (`.squad/human/questions.json`). Cada pergunta tem uma
opção **recomendada**; o `PROFILE.md` só será escrito depois das respostas.

## Contexto que motivou as perguntas

- A PRD (v3) e as 7 histórias (`STORIES.json`) estão fechadas; o design (v3) e o
  color scheme (v2) definem telas S1–S8, tokens MD3 e a paleta fixa de 8 cores —
  inclusive `dynamicColor = false` e tokens `none`/`full` próprios do design
  system.
- O app é **100% offline**, pt-BR, celular, uma lista única, uma etiqueta por
  tarefa, até 500 tarefas / 30 etiquetas, e **sem telemetria remota**.
- O projeto hoje é o template do Android Studio: um único módulo `:app`, pacote
  `com.foli.listadetarefas`, `minSdk 30`, `targetSdk`/`compileSdk 37`, Kotlin
  `2.2.10`, AGP `9.5.0`, Compose BOM `2026.02.01`, sem DI, Room, navegação,
  serialização, `material-icons-extended`, testes além de `junit`/espresso ou
  `build-logic`. `Theme.kt` usa dynamic color e precisa ser substituído.

## Requisitos técnicos já fixados por PRD/design (não são perguntas)

| Requisito | Origem |
|:--|:--|
| Offline total, sem login, sem permissões sensíveis | PRD §6 |
| Preferências persistidas: ordenação vigente e aba selecionada | US-03 AC 5, US-07 AC 4 |
| Cálculo de prazo no domínio, com data local, recalculado ao abrir/retomar/recompor | US-05 AC 5 |
| Busca por substring sem caixa/acento (collation pt-BR), escopada à aba, debounce ≤ 250 ms | US-06, PRD §6 |
| Tema claro/escuro pelo sistema, `dynamicColor = false`, paleta de etiquetas fora do `ColorScheme` | COLOR_SCHEME §1/§10 |
| Strings pt-BR em recursos, datas `dd/MM/aaaa`, truncamento apenas visual com TalkBack integral | DESIGN_SPEC §4/§10/§11 |

## Perguntas

### q1 — Pacote base e nome do app (texto)

Default recomendado: manter `com.foli.listadetarefas` (namespace e
`applicationId`) e o nome de exibição "Lista de tarefas". Responda `manter` ou
informe outro pacote/nome.

### q2 — Estrutura de módulos (`single_choice`)

- **Recomendado** — Enxuto: `:app`, `:build-logic`, `:core:common`,
  `:core:designsystem`, `:core:domain`, `:core:data`, `:feature:tasks`.
- Completo: `:core:common`, `:core:designsystem`, `:core:database`,
  `:core:datastore`, `:core:domain`, `:core:testing`, `:data:tasks`,
  `:feature:tasks`, `:feature:labels`, `:app`, `:build-logic`.
- Módulo único `:app` com pacotes por camada.

### q3 — SDKs (`single_choice`)

- **Recomendado** — `minSdk 30`, `targetSdk 37`, `compileSdk 37` (como está).
- `minSdk 26` (default documentado), `targetSdk 37`, `compileSdk 37`.
- `minSdk 24`, `targetSdk 36`, `compileSdk 36`.
- Outro (informar).

### q4 — Injeção de dependência (`single_choice`)

- **Recomendado** — Hilt (KSP), módulos por camada.
- Koin.
- Sem framework (manual + `CompositionLocal`).

### q5 — Persistência (`single_choice`)

- **Recomendado** — Room (KSP) para tarefas e etiquetas + DataStore Preferences
  para ordenação vigente e aba selecionada.
- Room para tudo (tabela de preferências, sem DataStore).
- DataStore (Proto/JSON) para tudo, sem Room.
- SQLDelight.

### q6 — Rede, telemetria e privacidade (`multiple_choice`)

Todos os itens abaixo são recomendados:

- Sem `:core:network` e sem dependência de rede.
- Sem SDK de analytics/crash remoto; eventos apenas `object AnalyticsEvents`
  local no-op em `snake_case`.
- `allowBackup=false` (nenhum dado sai do aparelho).
- Nenhuma permissão sensível no manifest (sem `INTERNET`, notificações,
  localização).
- Logs apenas em build debug, sem PII (título, descrição, nome de etiqueta).

> Consequência registrada: "backup/exportação" permanece fora de escopo (PRD §9);
> se você escolher manter backup do sistema, isso será documentado no perfil como
> decisão explícita.

### q7 — Apresentação e navegação (`single_choice`)

- **Recomendado** — MVI (State imutável + Action + Effect one-shot) +
  Navigation Compose com rotas type-safe (`@Serializable`).
- MVI + Navigation Compose com rotas em string.
- MVI sem Navigation Compose (S1 raiz, S2/S3 em estado local + sheets).
- MVVM sem `Effect` + Navigation Compose.

### q8 — Testes e qualidade (`multiple_choice`)

Recomendados:

- Unitários de ViewModel/reducer/use cases com `kotlinx-coroutines-test` +
  Turbine.
- Fronteiras de data no domínio (virada de dia, fim de mês, bissexto, fuso,
  passado/futuro).
- Room com banco em memória (instrumentado ou Robolectric): DAOs e consultas de
  busca/ordenação.
- Compose UI test dos fluxos críticos (criar, editar, excluir com confirmação,
  concluir, buscar, ordenar).
- Sem meta de cobertura percentual; foco nos fluxos críticos.
- Opcional: ktlint/detekt no build.

## Próximo passo

1. Responder no dashboard e digitar `continuar` (ou aguardar auto-resume).
2. O Architect consolida `.squad/artifacts/architecture/PROFILE.md`, escreve
   `ARCHITECTURE.md` e os ADRs (`ADR-0001`…`ADR-0008`), e envia para
   `chaos-architect`.
