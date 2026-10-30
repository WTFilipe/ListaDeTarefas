# DASHBOARD KANBAN LOCAL

O dashboard é um app Streamlit independente que lê `.squad/` e atualiza sozinho a
cada 2 segundos. Ele não depende do motor Python antigo nem do LangGraph.

## Iniciar

No projeto onde a Squad está rodando:

```powershell
# Windows / PowerShell
python "$env:USERPROFILE\.config\opencode\skills\squad-autonoma\scripts\run_dashboard.py" --project .
```

```bash
# macOS / Linux
python "$HOME/.config/opencode/skills/squad-autonoma/scripts/run_dashboard.py" --project .
```

O launcher:

1. procura um Python com Streamlit (prioriza a venv do projeto);
2. escolhe uma porta livre a partir de 8501;
3. sobe o Streamlit em background;
4. grava o PID em `.squad/dashboard.pid`;
5. imprime a URL.

Se não houver Streamlit no ambiente:

```powershell
python -m pip install -r "$env:USERPROFILE\.config\opencode\skills\squad-autonoma\scripts\requirements.txt"
```

ou use `uv run --with streamlit streamlit run ...`.

## Parar

```powershell
python "$env:USERPROFILE\.config\opencode\skills\squad-autonoma\scripts\run_dashboard.py" --project . --stop
```

## O que o dashboard mostra

- Cabeçalho: tarefa, fase, status, persona ativa, iterações, última atualização.
- Banner de ação humana quando `human_action_required.required = true`.
- Visão “Agora”: o que está acontecendo, progresso por etapa e próxima ação.
- 5 cards de etapa: Produto, Design, Arquitetura, Desenvolvimento e Release.
- Aba **Backlog**:
  - histórias com status, prioridade e progresso de tarefas;
  - plano de tarefas e revisão do plano;
  - cada tarefa com status e documentos (implementação, revisão, MR);
  - MR da história e MRs das tarefas.
- Cards com resumo, status, autor e documentação sob demanda.
- Preview de design: cards `.html` renderizam o mockup em iframe e oferecem link
  para abrir no navegador.
- Rastreabilidade: aba com a matriz
  `artifacts/traceability/TRACEABILITY.md`.
- ADRs: acessíveis nos detalhes da etapa Arquitetura.
- Formulários:
  - perguntas de descoberta/arquitetura (`questions.json`);
  - decisão de loop esgotado (`decision_request.json`);
  - aprovação da história (`decision_request.json`, scope `story_approval`,
    ação `approve_story`).
- Timeline das últimas ações (`state.json.history`).
- Toggle de modo debug + download e prévia de `.squad/logs/debug.log`.
- Toggle de auto-resume (aguardar `READY`) e de atualização automática dos cards.

## Fluxo humano

1. O agente grava `questions.json`/`decision_request.json`.
2. O dashboard mostra o formulário.
3. O usuário responde e clica em enviar.
4. O dashboard grava `answers.json`/`decision.json` + `READY`.
5. Com `auto_resume: true`, o orquestrador está em `wait_human.py` e retoma
   sozinho ao detectar `READY`. Com `auto_resume: false`, o usuário digita
   `continuar`.
6. O orquestrador lê os arquivos, o subagente consome e o fluxo segue.

## Variáveis e configuração

- `SQUAD_PROJECT_DIR`: diretório do projeto (o launcher define).
- Porta: `--port` ou próxima livre.
- Auto-refresh: status/humano/timeline a cada 2 segundos; cards opcionais via
  toggle.
- `.squad/config.json`: `debug`, `auto_resume`, `human_wait_timeout`,
  `max_iterations`, `repo`, `base_branch`, `branch_prefix`.
- O log de debug pode conter dados do input; trate como confidencial.

## Troubleshooting

- "Nenhuma execução encontrada": rode `/squad <tarefa>` primeiro.
- Porta ocupada: o launcher escolhe outra automaticamente.
- Streamlit não encontrado: instale com o requirements acima.
- Formulário desatualizado: clique em "Atualizar agora" na sidebar.
- Dashboard não reflete o estado: verifique se o orquestrador atualizou
  `.squad/state.json` via `squad_state.py`.
- Auto-resume não retomou: verifique `auto_resume` no config; se a tool bash do
  opencode matar o `wait_human.py`, digite `continuar`.
- Preview não aparece: confirme `artifacts/design/PREVIEW.html` e o status do
  artefato `design_preview`.
- Rastreabilidade vazia: cada artefato precisa da seção `## Traceability` com
  IDs `US-XX`.
