# FLUXO DE EXECUÇÃO — SQUAD AUTÔNOMA

## Personas e agentes

| Papel | Agente opencode | Tipo |
|:--|:--|:--|
| Orquestrador | `squad-orchestrator` | primary |
| Product Manager | `squad-product-manager` | subagent |
| Chaos Product | `squad-chaos-product` | subagent |
| Designer | `squad-designer` | subagent |
| Chaos Design | `squad-chaos-design` | subagent |
| Architect | `squad-architect` | subagent |
| Chaos Architect | `squad-chaos-architect` | subagent |
| Developer | `squad-developer` | subagent |
| Chaos Dev | `squad-chaos-dev` | subagent |
| Angel | `squad-angel` | subagent |

## Máquina de estados

```
INTAKE
  └─ product-manager (discovery)
       ├─ dúvidas -> human (scope=discovery) -> product-manager
       └─ PRD -> chaos-product (product_review)
            ├─ changes_requested -> product-manager (loop)
            └─ approved -> designer (design)

designer (design)
  ├─ COLOR_SCHEME.md (se o projeto não tiver)
  ├─ DESIGN_SPEC.md
  ├─ PREVIEW.html (mockup autocontido)
  └─ chaos-design (design_review)
       ├─ changes_requested -> designer (loop)
       └─ approved -> architect (architecture)

architect (architecture)
  ├─ PROFILE.md ausente -> human (scope=architecture) -> architect
  ├─ PROFILE.md
  ├─ ARCHITECTURE.md
  ├─ adr/ADR-XXXX-*.md
  └─ chaos-architect (architecture_review)
       ├─ changes_requested -> architect (loop)
       └─ approved -> developer (development)

developer (development, por história)
  ├─ TASK_PLAN.md/json (2-6 tarefas)
  └─ chaos-dev: PLAN_REVIEW.md
       ├─ changes_requested -> developer (loop, max 3)
       └─ approved -> para cada tarefa TXX:
            ├─ developer implementa SÓ TXX
            ├─ tasks/TXX/IMPLEMENTATION.md
            └─ chaos-dev: tasks/TXX/CHAOS_REVIEW.md
                 ├─ changes_requested -> developer (loop, max 3)
                 └─ approved -> próxima tarefa
  └─ todas as tarefas aprovadas -> human (scope=story_approval)

angel (release, por história)
  ├─ story branch squad/<story-slug>
  ├─ 1 branch + 1 MR por tarefa (stacked)
  ├─ MR de integração da história -> main
  ├─ tasks/TXX/MR.md + stories/<ID>/MR.md
  └─ história done -> próxima história
```

## Gates humanos

| Gate | Quando | Como | Retomada |
|:--|:--|:--|:--|
| `discovery` | PM precisa de contexto | PM grava `human/questions.json`; dashboard grava respostas | usuário digita `continuar` |
| `architecture` | Architect precisa de decisões técnicas | Architect grava `human/questions.json`; dashboard grava respostas | usuário digita `continuar` |
| `decision` | loop Chaos atingiu `max_iterations` | orquestrador grava `decision_request.json`; dashboard grava decisão | usuário digita `continuar` |
| `story_approval` | todas as tarefas da história aprovadas pelo Chaos Dev | orquestrador grava `decision_request.json` (scope `story_approval`) | auto-resume ou `continuar` |

Com `auto_resume: true` (default), o orquestrador executa
`scripts/wait_human.py` após o gate e retoma assim que o dashboard criar
`READY`. Com `auto_resume: false`, ele encerra o turno e espera `continuar`.

## Pós-fase (sempre)

Após cada fase relevante, o orquestrador:

1. roda `scripts/traceability.py --project .` para atualizar a matriz;
2. chama `scripts/squad_debug.py log ...` (no-op se `debug: false`);
3. atualiza `state.json` (`set-artifact`, `set-phase`, `history`);
4. no desenvolvimento, usa `scripts/stories.py next` para decidir o próximo
   passo e `stories.py` para atualizar história/tarefa.

## Regras de loop

- Cada fase com Chaos Agent tem no máximo `max_iterations` (padrão 3) rodadas
  de correção.
- O contador só incrementa quando o Chaos Agent devolve
  `verdict: changes_requested`.
- No desenvolvimento, os contadores são por plano de história (`bump-plan`) e
  por tarefa (`bump-task`), não um contador global de dev.
- Ao atingir o máximo, o orquestrador **não** invoca o dono novamente: grava a
  decisão humana e pausa.
- `approve`/`override` libera a fase; `request_changes` zera o contador daquele
  escopo e reinicia o loop; `abort` encerra como `blocked`.
- A aprovação humana é uma vez por história (`approve_story`), não por tarefa.

## Transições de estado (state.json)

| Evento | `phase` | `active_persona` | `status` |
|:--|:--|:--|:--|
| intake | `discovery` | `product-manager` | `in_progress` |
| PM pergunta | `discovery` | `product-manager` | `awaiting_human` |
| PRD pronta | `product_review` | `chaos-product` | `in_progress` |
| Chaos Product aprovou | `design` | `designer` | `in_progress` |
| Design pronto | `design_review` | `chaos-design` | `in_progress` |
| Chaos Design aprovou | `architecture` | `architect` | `in_progress` |
| Architect pergunta | `architecture` | `architect` | `awaiting_human` |
| Arquitetura pronta | `architecture_review` | `chaos-architect` | `in_progress` |
| Chaos Architect aprovou | `development` | `developer` | `in_progress` |
| Plano da história | `development` | `chaos-dev` | `in_progress` |
| Tarefa em revisão | `development` | `chaos-dev` | `in_progress` |
| Todas as tarefas aprovadas | `development` | developer/angel | `awaiting_human` (story_approval) |
| História aprovada | `development` | `angel` | `in_progress` |
| MRs abertos | `development` (ou `done` na última) | `angel` | `done` |
| abort/erro | `blocked` | — | `blocked` |

Artefatos extras:

- `design/PREVIEW.html` gerado pelo Designer e revisado pelo Chaos Design.
- `architecture/adr/*.md` gerados pelo Architect e revisados pelo Chaos Architect.
- `traceability/TRACEABILITY.md` gerado pelo orquestrador e exibido no dashboard.
- `logs/debug.log` gerado somente com `debug: true`.

## Ordem de leitura por persona

- `product-manager`: `PROTOCOL.md`, `references/flow.md`, `REQUEST.md`,
  `ANSWERS.md` (se existir), `CHAOS_REVIEW.md` (se existir).
- `chaos-product`: `PROTOCOL.md`, `references/flow.md`, `REQUEST.md`, `PRD.md`.
- `designer`: `PROTOCOL.md`, `references/flow.md`, `references/material3-tokens.md`,
  `PRD.md`, tema/color scheme existente no projeto.
- `chaos-design`: `PROTOCOL.md`, `references/flow.md`, `references/material3-tokens.md`,
  `PRD.md`, `COLOR_SCHEME.md`, `DESIGN_SPEC.md`, `PREVIEW.html`.
- `architect`: `PROTOCOL.md`, `references/flow.md`,
  `references/android-architecture-defaults.md`, `PRD.md`, `DESIGN_SPEC.md`,
  `ANSWERS.md` (se existir), arquivos Gradle do projeto.
- `chaos-architect`: `PROTOCOL.md`, `references/flow.md`,
  `references/android-architecture-defaults.md`, `PRD.md`, `DESIGN_SPEC.md`,
  `PROFILE.md`, `ARCHITECTURE.md`.
- `developer` (planejamento): `PROTOCOL.md`, `references/flow.md`, `PRD.md`,
  `STORIES.json`, `DESIGN_SPEC.md`, `PREVIEW.html`, `PROFILE.md`,
  `ARCHITECTURE.md`, `adr/`, `PLAN_REVIEW.md` (se existir).
- `developer` (tarefa): os mesmos + `TASK_PLAN.md/json`, a tarefa atual,
  `tasks/<T>/CHAOS_REVIEW.md` (se existir).
- `chaos-dev` (plano): `PROTOCOL.md`, `references/flow.md`, `PRD.md`,
  `STORIES.json`, `TASK_PLAN.md/json`, `ARCHITECTURE.md`,
  `android-architecture-defaults.md`.
- `chaos-dev` (tarefa): os mesmos + `tasks/<T>/IMPLEMENTATION.md`, código/diff,
  `IMPLEMENTATION.md` global (se existir).
- `angel`: `PROTOCOL.md`, `references/flow.md`, `PRD.md`, `STORIES.json`,
  `stories/<ID>/TASK_PLAN.md`, `tasks/*/IMPLEMENTATION.md`,
  `tasks/*/CHAOS_REVIEW.md`, `traceability/TRACEABILITY.md`,
  `human/decision.json`, `human/READY`.

## Extensibilidade

Para adicionar uma persona/fase:

1. crie um agente em `~/.config/opencode/agents/squad-<persona>.md`;
2. adicione a persona em `PERSONAS` e o artefato em `ARTIFACTS` no
   `scripts/squad_state.py`;
3. adicione a transição neste `flow.md` e no `SKILL.md`;
4. adicione a ordem de leitura e o gate humano, se houver;
5. reinicie o opencode.

Nenhuma mudança no dashboard é necessária se a nova fase usar um `phase` e um
artefato mapeados; o board agrupa por fase.
