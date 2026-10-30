---
artifact: traceability
phase: release
version: 1
author: orchestrator
status: in_progress
reviewer: none
verdict: pending
next_action: orchestrator
summary: "7 história(s); 7 sem implementação; 7 sem MR."
updated_at: 2026-10-30T01:28:07Z
---

# Matriz de Rastreabilidade

7 história(s); 7 sem implementação; 7 sem MR.

| Story | PRD | Design | Arquitetura | Implementação | Testes | MR |
|:--|:--|:--|:--|:--|:--|:--|
| US-01 | Cadastrar, editar e excluir tarefas com persistência local, linha com título/descrição em 2 linhas e acessível (ACs 1–13) | S1 (linha com título/descrição em 2 linhas, FAB, empty state), S2 (criação/edição com `←` + "Salvar" na app bar, validação inline, sem ação destrutiva no formulário), S6 (exclusão pelo menu da linha), snackbar de erro de armazenamento com "... | `core.result`, `domain.model`, `domain.usecase.task`, `data.local.db`, `data.repository`, `ui.model`, `ui.feature.tasks`, `ui.feature.editor` | MISSING | — | PENDING |
| US-02 | Limites e contadores de texto no formulário, sem alterar a altura da linha (ACs 1–6) | S2: limites 120/2000, contadores sempre visíveis (0/120, 0/2000), bloqueio no limite (limite atingido não é estado de erro), altura de linha estável (§8.3) | `domain.model.TaskLimits`, `domain.logic.TaskValidator`, `ui.feature.editor.components.TaskFormFields`, `ui.designsystem.components.Counter` | MISSING | — | PENDING |
| US-03 | Concluir tarefas e aba "Concluídas" com "Concluída em dd/MM/aaaa" (ACs 1–8) | Ícone de check; título "Nada concluído ainda"; corpo "As tarefas que você concluir aparecem aqui."; **sem** botão de ação. Chrome completo (busca e ordenação visíveis). FAB de "Nova tarefa" permanece (não há CTA concorrente). | `domain.usecase.task.SetTaskCompletedUseCase`, `data.local.db.TaskDao`, `data.local.preferences`, `ui.feature.tasks` | MISSING | — | PENDING |
| US-04 | Etiquetas com nome e cor de paleta fixa, estados vazios, seletor e chip truncado acessível (ACs 1–13) | S3 + S4 + S7: gestão de etiquetas; S4 com nome `singleLine` limite 30 e contador, erro inline de vazio e duplicata, grade de 8 cores em 4 colunas com alvo 48dp, anel/check e "Vermelho" pré-selecionada, loading/erro; S7 como **único** seleto... | `core.palette.LabelColorKey`, `domain.model.TaskLabel`, `domain.logic.LabelValidator`, `domain.usecase.label`, `ui.model.LabelUiModel/LabelEditorUiModel`, `ui.feature.labels`, `ui.feature.editor`, `ui.designsystem.components.LabelChip/Label... | MISSING | — | PENDING |
| US-05 | Data limite e indicador de proximidade com recálculo definido e sem timer (ACs 1–8) | S2 (chip de data único — data **ou** "Definir data", remover data, date picker pt-BR), S1 (4 estados de badge com texto + ícone, ausência de badge >7 dias, "Concluída em" substitui prazo), motion sem animação de cor | `domain.logic.DeadlineClassifier`, `domain.model.DeadlineState`, `ui.model.DeadlineUiState`, `core.time`, `ui.designsystem.components.DeadlineBadge`, `ui.feature.editor.components.TaskDeadlineChip` | MISSING | — | PENDING |
| US-06 | Busca por título e descrição, escopada à aba e preservada ao voltar do formulário (ACs 1–9) | Ícone de busca; título "Nenhum resultado" (`headlineSmall`); **eco da consulta** "Sua busca: “%1$s”" em `bodyMedium`, `maxLines = 2` + `Ellipsis`; corpo "Tente outra palavra ou limpe a busca."; botão tonal "Limpar busca". Corpo rolável se n... | `domain.logic.TaskFilter`, `core.text.TextNormalizer`, `ui.designsystem.components.SearchField`, `ui.feature.tasks` | MISSING | — | PENDING |
| US-07 | Ordenação da lista com seis opções (ACs 1–9) | S1 + S5: botão com ordenação vigente sempre visível (até 2 linhas a 200%, sem corte), sheet com as 6 opções em radio, seleção aplicada sem perder busca, estado persistido | `domain.model.SortOrder`, `domain.logic.TaskSorter`, `domain.usecase.preferences`, `ui.designsystem.components.SortControl`, S5 sheet | MISSING | — | PENDING |

## Cobertura

- Histórias encontradas: **7**
- Sem implementação: US-01, US-02, US-03, US-04, US-05, US-06, US-07
- Sem MR: US-01, US-02, US-03, US-04, US-05, US-06, US-07

> Gerado automaticamente. Para preencher, cada artefato deve ter uma seção `## Traceability` com IDs `US-XX`.
