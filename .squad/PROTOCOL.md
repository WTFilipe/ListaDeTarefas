# PROTOCOLO DA SQUAD AUTÔNOMA

Este arquivo é copiado para `.squad/PROTOCOL.md` em cada projeto. Ele é a fonte
de verdade da comunicação entre personas. Nenhum agente recebe contexto de outro
agente pelo prompt: **toda comunicação é feita por arquivos markdown/JSON dentro
de `.squad/`**.

## 1. Princípios

1. **Contexto em disco.** O orquestrador passa ao subagente apenas: a persona, a
   fase e os caminhos de arquivos que ele deve ler/escrever. Nunca o conteúdo.
2. **Um dono por artefato.** Somente o autor escreve o artefato. O revisor
   escreve um arquivo separado `CHAOS_REVIEW.md`.
3. **Handoff explícito.** Todo artefato termina com frontmatter contendo
   `status`, `verdict` e `next_action`. O orquestrador lê esses campos.
4. **Rastreabilidade.** Toda decisão relevante entra em `.squad/state.json`
   (`history`) e no artefato correspondente.
5. **Humano no controle.** Descoberta, decisão de loop e aprovação de MR são
   gates humanos registrados em `.squad/human/`.
6. **Sem segredos.** Nunca grave tokens, PII ou dados sensíveis em `.squad/`,
   logs, commits ou MRs.

## 2. Estrutura de diretórios

```
.squad/
  PROTOCOL.md              # este protocolo
  state.json               # estado da execução (fonte de verdade do dashboard)
  config.json              # configurações do projeto/execução
  references/
    flow.md
    material3-tokens.md
    android-architecture-defaults.md
    human-inbox.md
  artifacts/
    product/
      REQUEST.md           # pedido do usuário (orquestrador)
      PRD.md               # Product Manager
      STORIES.json         # backlog estruturado (Product Manager)
      CHAOS_REVIEW.md      # Chaos Product
    design/
      COLOR_SCHEME.md      # Designer (se o projeto não tiver um)
      DESIGN_SPEC.md       # Designer
      PREVIEW.html         # Designer (mockup visual autocontido)
      CHAOS_REVIEW.md      # Chaos Design
    architecture/
      PROFILE.md           # Architect (decisões técnicas do projeto)
      ARCHITECTURE.md      # Architect
      adr/                 # ADRs (Architect)
        ADR-0001-*.md
      CHAOS_REVIEW.md      # Chaos Architect
    development/
      IMPLEMENTATION.md    # opcional: resumo global; detalhes ficam por tarefa
      CHAOS_REVIEW.md      # opcional: resumo global
    release/
      MR.md                # Angel: resumo de todos os MRs de histórias/tarefas
    traceability/
      TRACEABILITY.md      # Orquestrador (matriz automática)
  stories/
    US-01/
      TASK_PLAN.md         # Developer: quebra da história em tarefas
      TASK_PLAN.json       # versão machine-readable do plano
      PLAN_REVIEW.md       # Chaos Dev: revisão do plano
      MR.md                # Angel: MR de integração da história
      tasks/
        T01/
          IMPLEMENTATION.md # Developer: entrega da tarefa
          CHAOS_REVIEW.md   # Chaos Dev: revisão da tarefa
          MR.md             # Angel: MR da tarefa
        T02/...
  human/
    questions.json         # perguntas de PM/Architect
    QUESTIONS.md
    answers.json           # respostas do dashboard
    ANSWERS.md
    decision_request.json  # pedido de decisão (loop esgotado ou aprovação de MR)
    decision.json          # decisão do humano
    DECISION.md
    READY                  # flag: respostas/decisão prontas para consumo
  logs/
    debug.log              # somente quando debug: true (pode conter PII)
  history/                 # arquivo de Q&A, decisões e snapshots
```

Execuções anteriores substituídas com `init --force` são movidas para
`.squad_history/<timestamp>/` na raiz do projeto.

## 3. Frontmatter obrigatório dos artefatos

Todo artefato markdown começa com:

```yaml
---
artifact: prd
phase: product
version: 1
author: product-manager
status: in_review
reviewer: chaos-product
verdict: pending
next_action: chaos-product
summary: "Uma frase objetiva do que este artefato entrega."
updated_at: 2026-10-29T18:00:00Z
---
```

Campos:

| Campo | Valores |
|:--|:--|
| `artifact` | chave/artefato (`request`, `prd`, `product_review`, `color_scheme`, `design_spec`, `design_preview`, `design_review`, `profile`, `architecture`, `architecture_review`, `implementation`, `dev_review`, `mr`, `traceability`, `task_plan`, `task_plan_review`, `task_implementation`, `task_review`, `task_mr`, `story_mr`) |
| `phase` | `discovery`, `product`, `product_review`, `design`, `design_review`, `architecture`, `architecture_review`, `development`, `dev_review`, `release` |
| `version` | inteiro; o autor incrementa ao corrigir |
| `author` | persona dona do artefato |
| `status` | `draft`, `in_review`, `changes_requested`, `approved`, `blocked` |
| `reviewer` | persona revisora (Chaos) |
| `verdict` | `pending`, `approved`, `changes_requested` |
| `next_action` | persona, `human`, `orchestrator` ou `done` |
| `summary` | resumo de 1 frase exibido no card do dashboard |
| `updated_at` | ISO-8601 UTC |

Regras:

- O autor define `status: in_review`, `verdict: pending` e
  `next_action: chaos-<fase>` quando termina.
- O revisor escreve `CHAOS_REVIEW.md` com `status: approved|changes_requested`,
  `verdict` e `next_action: <autor>|orchestrator`.
- Ao corrigir, o autor incrementa `version`, relê o review e volta para
  `in_review`.
- O revisor **nunca** edita o artefato do autor.
- O orquestrador **nunca** edita artefatos de fase; ele apenas lê e atualiza
  `.squad/state.json`.

## 4. Fases, donos e revisores

| Fase | Dono | Artefato | Revisor | Review |
|:--|:--|:--|:--|:--|
| discovery | product-manager | pergunta em `human/questions.json` | — | — |
| product | product-manager | `product/PRD.md` | chaos-product | `product/CHAOS_REVIEW.md` |
| design | designer | `design/COLOR_SCHEME.md`, `design/DESIGN_SPEC.md`, `design/PREVIEW.html` | chaos-design | `design/CHAOS_REVIEW.md` |
| architecture | architect | `architecture/PROFILE.md`, `architecture/ARCHITECTURE.md`, `architecture/adr/*.md` | chaos-architect | `architecture/CHAOS_REVIEW.md` |
| development | developer | `stories/<ID>/TASK_PLAN.{md,json}` + `stories/<ID>/tasks/<T>/IMPLEMENTATION.md` + código | chaos-dev | `stories/<ID>/PLAN_REVIEW.md`, `stories/<ID>/tasks/<T>/CHAOS_REVIEW.md` |
| release | angel | `stories/<ID>/tasks/<T>/MR.md`, `stories/<ID>/MR.md`, `release/MR.md` | humano | aprovação da história em `human/` |

## 5. Fluxo

```
INTAKE
  -> product-manager
       -> [dúvidas] human (discovery) -> product-manager ...
       -> PRD
  -> chaos-product
       -> changes_requested -> product-manager (loop, max 3) -> human (decisão)
       -> approved -> designer
  -> designer
       -> COLOR_SCHEME.md (se necessário) + DESIGN_SPEC.md
  -> chaos-design
       -> changes_requested -> designer (loop, max 3) -> human (decisão)
       -> approved -> architect
  -> architect
       -> [PROFILE ausente] human (architecture) -> architect ...
       -> PROFILE.md + ARCHITECTURE.md
  -> chaos-architect
       -> changes_requested -> architect (loop, max 3) -> human (decisão)
       -> approved -> desenvolvimento por história
  -> para cada história US-XX (prioridade):
       -> developer: quebra em tarefas (TASK_PLAN.md/json, 2-6 tarefas)
       -> chaos-dev: revisa o plano (loop, max 3)
       -> para cada tarefa TXX:
            -> developer: implementa SÓ a tarefa
            -> chaos-dev: revisa SÓ a tarefa (loop, max 3)
       -> todas as tarefas aprovadas
       -> human: aprova a história UMA vez (story_approval)
       -> angel: abre 1 MR por tarefa + 1 MR de integração da história
       -> história done
  -> done
```

O MR de tarefa não é aberto antes da aprovação da história. A aprovação humana é
por história; os MRs de tarefa são pequenos e empilhados (stacked) sob a branch
de integração da história.

O diagrama detalhado está em `.squad/references/flow.md`.

## 6. Comunicação e handoff

O orquestrador invoca um subagente com um prompt mínimo, por exemplo:

```
Leia .squad/PROTOCOL.md e .squad/references/flow.md.
Execute a persona product-manager na fase product.
Leia .squad/artifacts/product/REQUEST.md e, se existirem,
.squad/human/ANSWERS.md e .squad/artifacts/product/CHAOS_REVIEW.md.
Escreva apenas .squad/artifacts/product/PRD.md.
Não receba contexto além dos caminhos acima.
```

Proibido:

- Colar a PRD, o design, a arquitetura ou o código no prompt do `Task`.
- Resumir o trabalho de outro agente no prompt.
- Passar respostas humanas diretamente no prompt. O subagente lê
  `.squad/human/ANSWERS.md`.

## 7. Human inbox

### 7.1 Perguntas (`questions.json`)

Quem pergunta (PM ou Architect) grava:

```json
{
  "scope": "discovery",
  "persona": "product-manager",
  "title": "Descoberta do produto",
  "intro": "Preciso entender o contexto antes de escrever a PRD.",
  "questions": [
    {
      "id": "q1",
      "type": "single_choice",
      "question": "Qual é o público principal?",
      "options": ["Consumidor final", "Uso interno", "B2B"],
      "required": true
    },
    {
      "id": "q2",
      "type": "multiple_choice",
      "question": "Quais plataformas precisam de suporte?",
      "options": ["Android", "Tablet", "Wear OS"],
      "required": false
    },
    {
      "id": "q3",
      "type": "text",
      "question": "Descreva o fluxo principal do usuário.",
      "required": true
    }
  ]
}
```

Tipos aceitos: `single_choice`, `multiple_choice`, `text`.

O dashboard renderiza o formulário, grava `answers.json`, `ANSWERS.md` e cria a
flag `READY`. O usuário volta ao opencode e digita `continuar`.

Formato de `answers.json`:

```json
{
  "scope": "discovery",
  "submitted_at": "2026-10-29T18:10:00Z",
  "answers": {
    "q1": "Consumidor final",
    "q2": ["Android", "Tablet"],
    "q3": "O usuário adiciona, conclui e remove tarefas."
  }
}
```

### 7.2 Decisão humana (`decision_request.json` / `decision.json`)

O orquestrador grava o pedido quando:

- o loop atinge `max_iterations` sem acordo; ou
- o código está aprovado e o MR precisa de aprovação humana.

Formato do pedido:

```json
{
  "scope": "design",
  "reason": "Loop máximo atingido sem acordo entre designer e chaos-design.",
  "options": ["approve", "request_changes", "override", "abort"],
  "artifacts": [
    ".squad/artifacts/design/DESIGN_SPEC.md",
    ".squad/artifacts/design/CHAOS_REVIEW.md"
  ],
  "summary": "Última divergência: uso de shape XL vs LG.",
  "created_at": "2026-10-29T18:20:00Z"
}
```

Para aprovação de uma história (antes de abrir os MRs das tarefas), use
`scope: "story_approval"` e opções
`["approve_story", "request_changes", "abort"]`. O formato antigo de MR único
(`mr_approval`/`approve_mr`) não é mais usado no fluxo por histórias.

O dashboard grava `decision.json`, `DECISION.md` e `READY`:

```json
{
  "scope": "design",
  "action": "override",
  "notes": "Aceito LG; registre como decisão humana.",
  "submitted_at": "2026-10-29T18:25:00Z"
}
```

Ações:

| Ação | Efeito |
|:--|:--|
| `approve` | marca o artefato/revisão como aprovado e avança |
| `request_changes` | zera o contador do escopo e devolve ao dono com as notas |
| `override` | aprova com override registrado no histórico |
| `abort` | marca a fase como `blocked` e encerra |
| `approve_story` | autoriza o Angel a abrir os MRs das tarefas + MR da história |
| `request_changes` (história) | devolve a história ao Developer/Chaos Dev com as notas |

### 7.3 Consumo e arquivamento

1. O usuário responde no dashboard.
2. Se `.squad/config.json` tiver `auto_resume: true`, o orquestrador executa
   `scripts/wait_human.py` e retoma assim que `READY` aparecer. Caso contrário,
   o usuário digita `continuar` no opencode.
3. O orquestrador confirma `READY`, lê `answers.json`/`decision.json` e passa
   ao subagente apenas o caminho do arquivo.
4. Depois que o subagente consumiu, o orquestrador arquiva os arquivos em
   `.squad/history/<timestamp>/` e limpa `READY`.

## 8. Iterações e escalonamento

- Contadores de produto/design/arquitetura: `product`, `design`, `architecture`.
- No desenvolvimento por histórias, use `bump-plan`/`bump-task` do
  `scripts/stories.py` para os loops do plano e de cada tarefa.
- `max_iterations` padrão: 3 (em `.squad/config.json`).
- Cada `changes_requested` incrementa o contador correspondente.
- Se o contador chegar ao máximo e o revisor ainda reprovar, o orquestrador:
  1. grava `decision_request.json`;
  2. define `human_action_required` no `state.json`;
  3. pausa e pede ao usuário para responder no dashboard.
- A decisão humana pode continuar de onde parou: `approve`/`override` avança;
  `request_changes` zera o contador e reinicia o loop; `abort` encerra.

## 9. Retomada

Ao receber `/squad` ou `continuar`, o orquestrador:

1. lê `.squad/state.json`;
2. se `.squad/human/READY` existir, consome a resposta/decisão;
3. reconcilia o estado com o frontmatter dos artefatos;
4. continua do `phase`/`next_action` atual, sem repetir fases aprovadas.

Se não existir `.squad/`, inicializa. Se a execução anterior estiver `done`, uma
nova tarefa deve ser iniciada com `init --force` (o `/squad new <tarefa>` faz
isso).

## 10. Definição de pronto por fase

- **Product/PRD**: objetivo, não-objetivos, personas, histórias, critérios de
  aceite, roadmap macro, riscos, dependências, métricas. Sem dúvidas abertas.
- **Design**: color scheme definido (ou reutilizado), spec com tokens MD3,
  estados, tipografia, shape, elevação, motion, acessibilidade e
  `PREVIEW.html` autocontido. Nenhuma cor literal na spec; apenas tokens.
- **Architecture**: `PROFILE.md` preenchido, arquitetura multi-módulo, MVI
  (state/action/effect), estrutura de pastas, DI, rede, persistência, eventos,
  testes, segurança, build e ADRs das decisões relevantes.
- **Development (por história)**: `TASK_PLAN.md/json` aprovado pelo Chaos Dev;
  cada tarefa implementada individualmente, revisada e aprovada; código
  Kotlin/Compose compilando; sem segredos/PII; cada `IMPLEMENTATION.md` com
  `## Traceability`.
- **Release (por história)**: aprovação humana única da história registrada;
  um `MR.md` por tarefa e um `MR.md` de integração da história, com URLs,
  branches, bases e commits; matriz de rastreabilidade sem histórias
  `MISSING`.

## 11. Nomenclatura e idioma

- Documentação em português; identificadores de código em inglês.
- Artefatos markdown em `UPPER_SNAKE_CASE.md`.
- Diretórios em `lowercase`.
- Kotlin: pacotes `lowercase`, classes `PascalCase`, funções/variáveis
  `camelCase`, constantes `UPPER_SNAKE_CASE`, composables `PascalCase`.
- Eventos Firebase/analytics em `snake_case`: `screen_action_result`
  (ex.: `login_submit_clicked`, `task_created_success`), sem PII.

## 12. Segurança

- Não grave tokens, chaves, e-mails, IDs de usuário ou conteúdo sensível em
  `.squad/`, logs, commits ou MRs.
- O Angel nunca commita `.env`, `local.properties`, keystores ou arquivos não
  relacionados à tarefa.
- O Chaos Dev verifica vazamento de segredos, logs com PII, strings hardcoded e
  permissões excessivas.
- Com `debug: true`, `.squad/logs/debug.log` pode conter inputs e decisões do
  usuário. Trate esse arquivo como confidencial e não o commite.

## 13. Rastreabilidade

- Cada história da PRD deve ter ID `US-01`, `US-02`, etc.
- PRD, `DESIGN_SPEC.md`, `ARCHITECTURE.md`, `IMPLEMENTATION.md` e `MR.md` devem
  conter uma seção:

  ```markdown
  ## Traceability

  | Story | Contribuição |
  |:--|:--|
  | US-01 | <o que este artefato entrega/implementa/cobre> |
  ```

- O orquestrador roda `scripts/traceability.py --project .` após as fases de
  produto, design, arquitetura, desenvolvimento e release.
- A matriz fica em `.squad/artifacts/traceability/TRACEABILITY.md` e é exibida
  no dashboard. Histórias sem implementação aparecem como `MISSING`.

## 14. ADRs

- O Architect registra decisões relevantes em
  `.squad/artifacts/architecture/adr/ADR-XXXX-<slug>.md`.
- Template:

  ```markdown
  # ADR-0001: <decisão>

  - Status: proposto | aceito | substituído
  - Data: <ISO-8601>
  - Contexto: ...
  - Decisão: ...
  - Consequências: ...
  - Alternativas consideradas: ...
  ```

- O Chaos Architect verifica se as decisões estão consistentes com a
  arquitetura e com o `PROFILE.md`.
- O dashboard lista os ADRs automaticamente.

## 15. Preview visual

- O Designer gera `.squad/artifacts/design/PREVIEW.html` autocontido (CSS
  inline, sem rede/external assets), usando os tokens do color scheme.
- O preview cobre as telas/componentes/estados principais e é um apoio visual;
  a fonte normativa continua sendo `DESIGN_SPEC.md`.
- O Chaos Design compara preview, spec e color scheme.
- O dashboard renderiza o HTML em um iframe e oferece link para abrir no
  navegador. Não inclua scripts remotos, dados sensíveis ou PII.

## 16. Debug

- `.squad/config.json`:

  ```json
  {
    "debug": false,
    "auto_resume": true,
    "human_wait_timeout": 1800
  }
  ```

- Com `debug: true`, o orquestrador chama
  `scripts/squad_debug.py log ...` para registrar intake, transições,
  decisões humanas, respostas, artefatos e erros em `.squad/logs/debug.log`.
- Com `debug: false`, o script não grava nada.
- O dashboard tem um toggle de debug e um botão para baixar o log.
- O log é para diagnóstico; pode conter dados do seu input. Não o compartilhe
  publicamente.

## 17. Histórias e tarefas (desenvolvimento incremental)

Para projetos grandes, a implementação não é feita de uma vez. O Product Manager
grava o backlog estruturado em `.squad/artifacts/product/STORIES.json`, e o
desenvolvimento roda história por história.

### 17.1 STORIES.json

```json
{
  "schema_version": 1,
  "stories": [
    {
      "id": "US-01",
      "title": "Login",
      "priority": 1,
      "description": "Entrar com e-mail e senha.",
      "acceptance_criteria": [
        "Login válido abre a home",
        "Credencial inválida mostra erro inline"
      ],
      "status": "pending",
      "tasks": []
    }
  ]
}
```

- O PM é o dono do `STORIES.json` na fase de produto.
- Cada história precisa de ID `US-XX`, prioridade, descrição e critérios de
  aceite.
- O Chaos Product verifica se o backlog bate com a PRD e se as histórias são
  pequenas e testáveis.
- Depois da PRD aprovada, o orquestrador roda `scripts/stories.py init`.

### 17.2 Loop por história

1. O orquestrador chama `scripts/stories.py next` e recebe a próxima ação.
2. `plan`: o Developer grava `stories/<ID>/TASK_PLAN.md` e `TASK_PLAN.json`,
   quebrando a história em 2 a 6 tarefas entregáveis. Ele não implementa ainda.
3. O orquestrador roda `stories.py sync-plan --story <ID>` e o Chaos Dev revisa
   o plano em `PLAN_REVIEW.md`.
4. Se o plano for reprovado, volta ao Developer (`bump-plan`); ao aprovar, o
   orquestrador define a história como `implementing`.
5. Para cada tarefa:
   - `task_implement`: o Developer implementa **somente** a tarefa e grava
     `tasks/<T>/IMPLEMENTATION.md`;
   - `task_review`: o Chaos Dev revisa **somente** a tarefa e grava
     `tasks/<T>/CHAOS_REVIEW.md`;
   - reprovou: `bump-task` e volta ao Developer; aprovou: marca a tarefa como
     `approved` e segue para a próxima.
6. Quando todas as tarefas estiverem aprovadas, a história vira
   `awaiting_approval` e o orquestrador abre o gate humano
   `story_approval` (uma única aprovação por história).
7. Com `approve_story`, a história vira `releasing` e o Angel:
   - abre um MR por tarefa, em branches empilhadas;
   - abre o MR de integração da história para `main`;
   - grava `tasks/<T>/MR.md` e `stories/<ID>/MR.md`;
   - marca as tarefas como `released` e a história como `done`.
8. O orquestrador volta ao passo 1 para a próxima história.

### 17.3 TASK_PLAN.json

```json
{
  "story_id": "US-01",
  "tasks": [
    {
      "id": "T01",
      "title": "Contratos MVI do login",
      "description": "State, Action, Effect e ViewModel.",
      "files": ["feature/login/.../LoginState.kt"],
      "acceptance_criteria": ["Estado e ações definidos"],
      "depends_on": []
    },
    {
      "id": "T02",
      "title": "Tela de login",
      "description": "UI Compose com loading/erro.",
      "files": ["feature/login/.../LoginScreen.kt"],
      "acceptance_criteria": ["UI cobre loading e erro"],
      "depends_on": ["T01"]
    }
  ]
}
```

Regras de quebra:

- 2 a 6 tarefas por história é o alvo saudável.
- Não crie uma tarefa por arquivo (granular demais).
- Não crie uma única tarefa gigante (grossa demais).
- Cada tarefa deve ser um incremento coerente, revisável e testável.
- O Chaos Dev pode reprovar a quebra se ela estiver granular/grossa demais ou
  não cobrir os critérios de aceite da história.

### 17.4 Frontmatter de plano/tarefa

Plano (`PLAN_REVIEW.md`):

```yaml
---
artifact: task_plan_review
phase: development
story: US-01
version: 1
author: chaos-dev
status: approved
verdict: approved
next_action: orchestrator
summary: "Plano com 3 tarefas cobre os critérios de aceite."
updated_at: 2026-10-29T18:00:00Z
---
```

Implementação de tarefa:

```yaml
---
artifact: task_implementation
phase: development
story: US-01
task: T01
version: 1
author: developer
status: in_review
reviewer: chaos-dev
verdict: pending
next_action: chaos-dev
summary: "Contratos MVI implementados."
updated_at: 2026-10-29T18:00:00Z
---
```

Review de tarefa:

```yaml
---
artifact: task_review
phase: development
story: US-01
task: T01
version: 1
author: chaos-dev
status: approved
verdict: approved
next_action: orchestrator
summary: "Tarefa aderente à arquitetura."
updated_at: 2026-10-29T18:00:00Z
---
```

### 17.5 Estratégia de branches/MRs

- Branch de integração da história: `squad/<story-slug>` a partir de
  `config.base_branch`.
- Branch da primeira tarefa: `squad/<story-slug>/<task-id>-<slug>` a partir da
  branch de integração.
- Branches das próximas tarefas são empilhadas a partir da branch da tarefa
  anterior. Cada MR de tarefa tem como base a branch anterior.
- O MR de integração da história vai da última branch de tarefa (ou da branch de
  integração, se os MRs já foram mesclados) para `main`.
- O humano aprova a história uma vez; o Angel abre os MRs e não faz merge.
- Se houver conflito ou `gh` indisponível, o Angel registra o bloqueio em
  `stories/<ID>/MR.md` e não inventa URLs.

### 17.6 Dashboard

- A aba **Backlog** mostra as histórias, a história atual, a quebra em tarefas,
  o status de cada tarefa e os documentos/MRs de cada uma.
- A aba **Visão geral** resume o progresso da história atual na etapa de
  Desenvolvimento.
- O gate `story_approval` aparece no topo com o título "Aprovação da história".
- A matriz de rastreabilidade agrega os `IMPLEMENTATION.md` e `MR.md` de todas as
  tarefas por história.
