# HUMAN INBOX — PERGUNTAS, RESPOSTAS E DECISÕES

O dashboard é a interface humana. Ele lê `.squad/state.json`, renderiza o quadro
Kanban e permite responder perguntas e registrar decisões em `.squad/human/`.

## Arquivos

```
.squad/human/
  questions.json          # agente pergunta
  QUESTIONS.md            # versão legível
  answers.json            # dashboard grava
  ANSWERS.md              # versão legível
  decision_request.json   # orquestrador pede decisão
  decision.json           # dashboard grava
  DECISION.md             # versão legível
  READY                   # flag: respostas/decisão prontas
```

## Perguntas (agente -> humano)

O PM (descoberta) e o Architect (perfil técnico) gravam `questions.json`:

```json
{
  "scope": "discovery",
  "persona": "product-manager",
  "title": "Descoberta do produto",
  "intro": "Preciso de contexto para escrever a PRD.",
  "questions": [
    {
      "id": "q1",
      "type": "single_choice",
      "question": "Quem é o público principal?",
      "options": ["Consumidor final", "Uso interno", "B2B"],
      "required": true
    },
    {
      "id": "q2",
      "type": "multiple_choice",
      "question": "Quais recursos são obrigatórios?",
      "options": ["Offline", "Login", "Notificações"],
      "required": false
    },
    {
      "id": "q3",
      "type": "text",
      "question": "Descreva o fluxo principal.",
      "required": true
    }
  ]
}
```

Regras:

- IDs estáveis e únicos (`q1`, `q2`, ...).
- `scope`: `discovery` ou `architecture`.
- Tipos: `single_choice`, `multiple_choice`, `text`.
- Máximo recomendado: 8 perguntas por rodada.
- O agente também grava `QUESTIONS.md` com o mesmo conteúdo em formato legível.
- O agente atualiza o frontmatter do seu artefato para
  `status: awaiting_human`, `next_action: human`.

## Respostas (humano -> agente)

O dashboard grava `answers.json`:

```json
{
  "scope": "discovery",
  "submitted_at": "2026-10-29T18:10:00Z",
  "answers": {
    "q1": "Consumidor final",
    "q2": ["Offline", "Login"],
    "q3": "O usuário cria, conclui e remove tarefas."
  }
}
```

E `ANSWERS.md`:

```markdown
# Respostas — Descoberta do produto

- q1 (single_choice): Consumidor final
- q2 (multiple_choice): Offline, Login
- q3 (text): O usuário cria, conclui e remove tarefas.
```

Depois cria `.squad/human/READY`.

## Decisão (orquestrador -> humano -> orquestrador)

O orquestrador grava `decision_request.json`:

```json
{
  "scope": "product",
  "reason": "Loop máximo atingido sem acordo.",
  "options": ["approve", "request_changes", "override", "abort"],
  "artifacts": [
    ".squad/artifacts/product/PRD.md",
    ".squad/artifacts/product/CHAOS_REVIEW.md"
  ],
  "summary": "Divergência sobre escopo do MVP.",
  "created_at": "2026-10-29T18:20:00Z"
}
```

O dashboard grava `decision.json`:

```json
{
  "scope": "product",
  "action": "request_changes",
  "notes": "Reduza o MVP para 3 histórias e remova login social.",
  "submitted_at": "2026-10-29T18:25:00Z"
}
```

Para aprovação de uma história, `scope: "story_approval"` e ações
`approve_story`, `request_changes`, `abort`. O fluxo por histórias não usa mais
`mr_approval`/`approve_mr`.

## Retomada

1. O dashboard grava `answers.json`/`decision.json` + `READY`.
2. Com `auto_resume: true`, o orquestrador está em `wait_human.py` e retoma
   sozinho ao detectar `READY`. Com `auto_resume: false`, o usuário volta ao
   opencode e digita `continuar`.
3. O orquestrador verifica `READY`, lê os arquivos e passa ao subagente apenas o
   caminho.
4. O subagente consome as respostas e atualiza seu artefato.
5. O orquestrador arquiva o inbox em `.squad/history/<timestamp>/` e remove
   `READY`.

Nunca coloque o conteúdo das respostas no prompt do `Task`. O subagente lê o
arquivo.

## Reset manual

Se o usuário quiser refazer as respostas:

- apague `.squad/human/READY` e `answers.json`/`decision.json` (ou use o botão
  "Editar respostas" do dashboard);
- responda novamente;
- digite `continuar`.

## Segurança

- Não inclua PII, tokens ou segredos nas respostas.
- O dashboard é local; não exponha a porta publicamente.
