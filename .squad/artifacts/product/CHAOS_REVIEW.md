---
artifact: product_review
phase: product_review
version: 3
author: chaos-product
status: approved
reviewer: none
verdict: approved
next_action: orchestrator
summary: "PRD v3 resolve os 3 majors e os 10 minors do review v2; aprovada com 11 ajustes minor de testabilidade, consistência e acessibilidade para o plano/design."
updated_at: 2026-10-30T00:40:00Z
---

# Chaos Product — Revisão da PRD (v3)

**Escopo revisado:** `PRD.md` v3, `STORIES.json` e `REQUEST.md`, confrontados com
o `CHAOS_REVIEW.md` v2. As respostas humanas foram conferidas pela tabela da §8;
o `human/ANSWERS.md` já não existe (arquivado). Não editei a PRD.

**Resultado:** o veredito está no frontmatter (`verdict: approved`). Não há
blocker nem major. O pedido original continua integralmente coberto: título e
descrição, etiqueta com cor, busca em título+descrição, ordenação alfabética e
por criação, e data limite com indicador de proximidade. As histórias são
incrementais, têm IDs, prioridades, critérios testáveis, dependências explícitas
e o `STORIES.json` corresponde à PRD.

## Blocker e Major

Nenhum. Não há história dependendo de funcionalidade inexistente, requisito
central sem dono, contradição entre ACs ou backlog divergente da PRD.

## Itens do review v2 — verificação

| Item v2 | Evidência na PRD v3 | Situação |
|:--|:--|:--|
| M1 — título longo vs acessibilidade | US-01 AC 10 (PRD 172–175), US-02 AC 6 (218–221) e §6 Acessibilidade (502–505): até 2 linhas com reticências, texto completo no formulário e no TalkBack; NFR reescrito | resolvido |
| M2 — recálculo do prazo contraditório | US-05 AC 5 (354–359), caso de borda (369–375) e não-objetivo (71): regra única, sem timer de meia-noite, atualização na primeira interação/recomposição | resolvido |
| M3 — estados vazios de etiquetas | US-04 AC 8 e 9 (301–306), casos de borda (324–326) e §6 Qualidade (535–537) | resolvido |
| Minor 1 — "ordenação selecionada" antecipada | §4 (113–115) define "ordenação vigente"; US-01 AC 5, US-03 AC 6 e US-06 AC 4/7 usam o termo | resolvido |
| Minor 2 — dependências no schema do JSON | §7 (555) e descrições do `STORIES.json` registram "Depende de:" | resolvido |
| Minor 3 — limiares do indicador no JSON | `STORIES.json` US-05 AC 3 (linha 92) repete as faixas | resolvido |
| Minor 4 — data de conclusão sem uso | US-03 AC 3 (246–247) exibe "Concluída em dd/MM/aaaa"; JSON idem | resolvido |
| Minor 5 — metas de desempenho subjetivas | §6 Desempenho (486–494): 2 s, 16 ms, debounce ≤250 ms e busca ≤300 ms | resolvido |
| Minor 6 — termo de UI em inglês | §5 (127–129) e US-04 usam "Etiqueta"/"Sem etiqueta"; AC 10 exige TalkBack com nome completo | resolvido |
| Minor 7 — US-06 AC 9 pressupunha criação inline | US-06 AC 9 (409–411) fala em abrir/fechar o formulário e preservar o filtro | resolvido |
| Minor 8 — "tarefa legada" sem origem | Caso removido; US-02 (227–228) registra que não há migração no MVP | resolvido |
| Minor 9 — cor obrigatória sem validação | US-04 AC 2 (285–288): primeira cor pré-selecionada, sem estado "sem cor" | resolvido |
| Minor 10 — US-01/US-04 no teto de 6 tarefas | §5 (139–142) e §7 (556) orientam a quebra; segue como risco de plano, não de PRD | endereçado como orientação |

## Problemas remanescentes (minor, não bloqueantes)

Nenhum destes itens reabre o discovery nem impede design/arquitetura. A coluna
"Correção exigida" vale para o próximo toque do PM, para o design ou para o
plano da história correspondente.

| ID | Severidade | Local | Por que importa | Correção exigida |
|:--|:--|:--|:--|:--|
| m1 | minor | US-02 AC 1–3 (209–214) e caso de borda (225); `STORIES.json` US-02 AC 1–3 (33–35) | "120/2000 caracteres" não define se a unidade é code unit UTF-16, code point ou grafema. O caso de borda com emoji exige que a contagem "não dê erro", mas não fixa a unidade; dev e QA podem divergir no limite. | Definir a unidade (recomendo grafemas/caracteres percebidos) e usá-la no contador, no bloqueio e no teste; espelhar no JSON e registrar na §8. |
| m2 | minor | US-04 AC 2 (285–288) e AC 5 (293–295); `STORIES.json` US-04 AC 2 (68) | O limite de 30 do nome não diz se o 31º caractere é bloqueado, se colar trunca ou se salvar mostra erro. Diferente da US-02, não há AC de exceção nem contador; o teste fica ambíguo. | Escolher um comportamento (bloqueio com contador ou erro inline) e descrevê-lo nos dois ACs e no JSON. |
| m3 | minor | US-03 AC 2–4 (244–249) e casos de borda (263–264); US-05 AC 6 (360–361); `STORIES.json` US-03 AC 2–3 | "Preservar todos os dados" ao reabrir, "a marca não muda depois" e "registrar data/hora" não definem se reabrir limpa a conclusão, se reconcluir atualiza a data e se mudar o fuso pode alterar o dia exibido. | Definir se "Concluída em" é uma data local congelada, o comportamento ao reabrir/reconcluir e a estabilidade sob mudança de fuso; refletir no JSON/§8. |
| m4 | minor | §4 (106); US-07 AC 8 (453–454); `STORIES.json` US-07 descrição (125) | A coluna "Depende de" é normativa, mas a US-07 usa a aba "Concluídas" (US-03) sem declarar US-03. A prioridade garante a ordem, porém o gate declarado fica incompleto. | Adicionar US-03 às dependências da US-07 na §4 e na descrição do JSON, ou registrar na §4 que a prioridade dispensa a dependência (hoje ela é normativa). |
| m5 | minor | US-04 AC 1 (282–284) e AC 7 (299–300); `STORIES.json` US-04 AC 1/7 | Com até 30 etiquetas (§7), a ordem da lista e do seletor não está definida; "lista as etiquetas" não diz se é alfabética ou por criação. Afeta usabilidade e teste. | Definir a ordenação (recomendo alfabética pt-BR, ignorando caixa/acentos, desempate por criação) e espelhar no JSON. |
| m6 | minor | US-05 AC 5 (354–359); `STORIES.json` US-05 AC 5 (94) | O gatilho usa "recomposição", termo de Compose que um teste manual não observa. A frase seguinte define interação/rolagem/navegação, mas o termo amarra o AC à implementação. | Reescrever em termos observáveis ("primeira interação, rolagem, navegação ou retorno à tela após a mudança"), preservando a semântica. |
| m7 | minor | US-05 AC 3 (345–351) vs AC 4 (352–353); `STORIES.json` US-05 AC 3–4 | Os estados são "Atrasada/Hoje/Amanhã/Até 7 dias", mas o exemplo de TalkBack é "Vence amanhã" e não há string definida para os demais. O anúncio exato não é testável e o design precisa fixá-lo. | Definir a string de acessibilidade por estado (linguagem natural é aceitável, desde que explícita) e garantir que o anúncio "junto ao título" seja verificável. |
| m8 | minor | §6 Desempenho (486–494) vs ACs de US-01/US-06/US-07; §6 Qualidade (531–541) | 2 s de abertura, 500 tarefas, 16 ms, debounce ≤250 ms e busca ≤300 ms não são AC de nenhuma história. O gate por história pode aprovar todas as tarefas sem medir os NFRs; também não há forma definida de obter a massa de 500 tarefas. | Declarar os NFRs da §6 como critério global de aceite (verificado no fechamento de US-07 ou em todas as histórias) e indicar o seed de 500 tarefas; ou referenciar a §6 nos ACs correspondentes. |
| m9 | minor | US-06 AC 5 (402–403) e AC 7 (406–407); `STORIES.json` US-06 AC 5/7 | "A busca atua sobre a aba visível" e "limpar restaura a aba atual" sugerem que a consulta persiste ao trocar de aba, mas isso não é dito. A UI pode limpar ou manter a consulta sem critério de teste. | Definir se a consulta permanece ao trocar de aba (reaplicando à nova) ou é limpa; refletir no JSON. |
| m10 | minor | US-01 AC 10 (172–175); §6 Acessibilidade (502–505) | A regra resolve o truncamento, mas anunciar a descrição completa (até 2000 caracteres) ao focar a linha torna a navegação por TalkBack penosa em listas grandes, contra o objetivo de "encontrar em segundos". Não é contradição, é risco de usabilidade. | Confirmar a intenção; alternativa é anunciar título + metadados e expor a descrição completa por ação ("Ler descrição") ou na edição. Se mantiver o anúncio integral, registrar a decisão na §8 e testar com 500 itens. |
| m11 | minor | PRD US-01 AC 13 (180–182) e US-03 AC 8 (256–257) vs `STORIES.json` US-01 AC 13 (22) | A PRD exige "Tentar novamente" também em falha de exclusão; o JSON só fala em falha de armazenamento e preservação do formulário. O plano nascido do JSON pode não cobrir exclusão. | Atualizar o AC do JSON para "ao salvar ou excluir" e manter a preservação de conteúdo no caso de criação/edição. |

## O que está bom (preservar na correção)

- **Rastreabilidade e consistência de backlog:** IDs `US-01..US-07`, títulos,
  prioridades 1–7, `status: pending`, `tasks: []` batem entre PRD, §4,
  §Traceability e `STORIES.json`. A seção `## Traceability` cobre todas as
  histórias e os totais de ACs conferem.
- **Dependências explícitas:** US-05→US-01/US-03, US-06→US-01/US-03,
  US-07→US-01/US-05; nenhuma história depende de prioridade maior. "Ordenação
  vigente" eliminou a referência antecipada ao seletor da US-07.
- **Critérios mensuráveis:** 120/2000/30 caracteres, alvos ≥48dp, 500 tarefas,
  16 ms de frame, debounce ≤250 ms, busca ≤300 ms, abertura ≤2 s com aparelho de
  referência, 2 linhas + reticências e faixas de prazo fechadas.
- **Pedido original coberto:** título e descrição (US-01/US-02), etiqueta com cor
  (US-04), busca em título+descrição (US-06), ordenação alfabética e por criação
  (US-07) e data limite com indicador (US-05).
- **Estados vazios e de erro com dono:** lista ativa vazia e falha de
  armazenamento (US-01), "Concluídas" vazia (US-03), etiquetas vazias e seletor
  sem etiquetas (US-04), busca sem resultado (US-06); §6 (531–538) mapeia todos.
- **Acessibilidade real:** texto + ícone no prazo, nome visível no chip,
  TalkBack com nome completo no truncamento, fonte até 200%, contraste MD3 e
  descrições em pt-BR.
- **Privacidade coerente:** app 100% offline, sem SDK/rede, eventos apenas como
  ganchos locais no-op sem PII e explicitamente fora dos critérios de aceite.
- **Casos de borda úteis:** virada de dia/fuso/ano bissexto, duplicidade de
  conclusão, exclusão de etiqueta em uso durante a edição, busca com acentos e
  regex literal, empates de ordenação, colagem acima do limite e rotação/fonte
  ampliada.
- **Decisões registradas na §8:** a renomeação para "Até 7 dias", a primeira cor
  pré-selecionada, a data de conclusão exibida, a ausência de migração e a
  ordenação padrão estão justificadas, sem requisitos inventados.

## Por que a PRD está pronta

O produto tem um MVP claro, sete histórias pequenas o suficiente para planos de
2 a 6 tarefas e critérios observáveis para cada capacidade pedida. As
dependências são executáveis no gate por história, os estados vazios/erro estão
distribuídos e mapeados, os NFRs são mensuráveis e o `STORIES.json` acompanha a
PRD. Os itens m1–m11 são detalhes de redação, de semântica de borda e de
sincronização do JSON; nenhum muda valor de produto, escopo ou roadmap. As duas
histórias maiores (US-01 e US-04) continuam no teto de 6 tarefas — é um risco de
planejamento que o Chaos Dev deve cobrar no `TASK_PLAN`, não um defeito de PRD.

## Próximo passo

`next_action: orchestrator`. Design e arquitetura podem começar. Recomendo passar
o caminho deste review ao designer e ao architect para que m5, m7 e m10 (paleta,
strings de TalkBack e semântica da linha) não se percam; m1–m4, m6, m8, m9 e m11
podem ser absorvidos pelo plano do Developer ou por um toque futuro do PM. Não é
necessário novo loop de produto.
