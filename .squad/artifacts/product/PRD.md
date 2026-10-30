---
artifact: prd
phase: product
version: 3
author: product-manager
status: in_review
reviewer: chaos-product
verdict: pending
next_action: chaos-product
summary: "Roadmap com 7 histórias para um app Android offline de tarefas com etiquetas, prazos, busca e ordenação, com regras de título, recálculo de prazo e estados vazios fechados."
updated_at: 2026-10-30T00:15:00Z
---

# PRD — App de Lista de Tarefas

> Correção da v2 após o review do Chaos Product (`CHAOS_REVIEW.md` v2):
> - **M1** título/descrição na linha com no máximo 2 linhas + reticências, texto
>   completo no formulário e no TalkBack, e NFR de acessibilidade reescrito;
> - **M2** regra única e testável de recálculo do indicador de prazo (sem timer de
>   meia-noite), alinhada ao caso de borda;
> - **M3** estados vazios da tela de etiquetas e do seletor sem etiquetas;
> - minors 1–10: "ordenação vigente" em vez de seletor futuro, dependências na
>   descrição do `STORIES.json`, limiares do prazo repetidos no JSON, data de
>   conclusão exibida, metas de desempenho mensuráveis, termo de UI em pt-BR
>   ("Etiqueta"), busca preservada ao retornar do formulário, caso de "tarefa
>   legada" removido, cor da etiqueta pré-selecionada e nota de quebra em tarefas.

## 1. Contexto e problema

O usuário precisa de um lugar simples e rápido para registrar o que precisa fazer,
sem depender de conexão, conta ou configuração. As ferramentas atuais de tarefas
exigem login, são pesadas ou não mostram de forma clara **o que está perto do
prazo**. O pedido original (`.squad/artifacts/product/REQUEST.md`) descreve um app
Android de lista de tarefas com:

- título e descrição por tarefa;
- uma label customizada pelo usuário, com cor;
- busca que considera título **e** descrição;
- ordenação por ordem alfabética e por data de criação;
- data limite por tarefa com indicador de proximidade.

A descoberta confirmou: dados **somente no aparelho** (offline), suporte a
**celular Android**, tarefa com estado **concluída** em uma aba "Concluídas",
**uma label por tarefa** com cor de paleta fixa, **uma única lista**, indicador
com os estados **Atrasada/Hoje/Amanhã/Até 7 dias**, **sem notificações** e seis
opções de ordenação.

## 2. Objetivo e não-objetivos

### Objetivo

Entregar um app Android nativo, offline-first, em que o usuário consiga cadastrar
tarefas com prazo e etiqueta, encontrar qualquer tarefa em segundos pela busca e
perceber de imediato quais tarefas estão atrasadas ou vencendo — sem cadastro,
sem login e sem permissões sensíveis.

### Não-objetivos (MVP)

- Sincronização, nuvem, conta, login ou compartilhamento entre aparelhos.
- Mais de uma lista/projeto ou mais de uma etiqueta por tarefa.
- Lembretes, notificações, alarmes ou integração com calendário.
- Suporte a tablet, Wear OS, iOS ou web.
- Tarefas recorrentes, subtarefas, anexos, comentários, checklists.
- Arrastar-e-soltar para reordenar manualmente.
- Colaboração, compartilhamento externo e exportação/importação.
- Idiomas além de pt-BR.
- Anexar **horário** à data limite: o prazo é uma data local, sem hora.
- Seletor de tema no app: sem alternância manual entre claro/escuro e sem temas
  customizados. A definição de seguir claro ou escuro do sistema fica com a fase
  de design; nenhuma história cobre troca de tema pelo usuário.
- Atualização do indicador de prazo por timer em tempo real (ver US-05 AC 5).

## 3. Personas e cenários

### Persona principal — "Ana, organizadora do dia a dia"

Profissional de 25–45 anos, usa o celular Android como caderno principal. Quer
anotar tarefas em segundos (inclusive no transporte, sem sinal) e saber o que
vence primeiro.

Cenários-chave:

1. **Captura rápida:** abre o app, toca em "Nova tarefa", digita título, salva.
2. **Organização por contexto:** cria a etiqueta "Trabalho" com cor e a associa às
   tarefas.
3. **Prazo visível:** define data limite; a lista mostra "Hoje" ou "Atrasada".
4. **Recuperação:** digita parte de uma palavra da descrição e encontra a tarefa.
5. **Fechamento do dia:** conclui tarefas; elas saem da lista ativa e ficam em
   "Concluídas".

### Persona secundária — "Bruno, usuário esporádico"

Usa o app em rajadas, esquece por semanas. Precisa que os dados continuem lá
(offline, sem login) e que o indicador de atraso seja óbvio ao reabrir.

## 4. Roadmap macro

| Prioridade | História | Valor de negócio | Depende de |
|:--|:--|:--|:--|
| 1 | US-01 — Cadastrar, editar e excluir tarefas com persistência local | Base do produto: sem cadastro não há app | — |
| 2 | US-02 — Limites e contadores de texto no formulário | Impede conteúdo abusivo e layout quebrado | US-01 |
| 3 | US-03 — Concluir tarefas e aba "Concluídas" | Fecha o ciclo de uso e dá sensação de progresso | US-01 |
| 4 | US-04 — Etiquetas (labels) com nome e cor de paleta fixa | Organização visual por contexto | US-01 |
| 5 | US-05 — Data limite e indicador de proximidade | Diferencial central: percepção de prazo | US-01, US-03 |
| 6 | US-06 — Busca por título e descrição | Recuperação rápida quando a lista cresce | US-01, US-03 |
| 7 | US-07 — Ordenação da lista | Controle de leitura da lista (alfabética, criação, prazo) | US-01, US-05 |

Cada história é entregável de forma incremental **na ordem proposta**; critérios
que dependem de história posterior ficam na história posterior. Busca aparece só
na US-06, checkbox de conclusão só na US-03, indicador de prazo só na US-05,
seletor de ordenação só na US-07 e chip de etiqueta só na US-04.

Até a US-07 existir, o app usa apenas a **ordenação vigente** (padrão "criação
mais recente primeiro"). Menções a "ordenação vigente" nas histórias anteriores
não pressupõem seletor nem escolha do usuário.

A coluna "Depende de" é normativa para o gate de aprovação por história e está
repetida na descrição de cada história em `STORIES.json`.

## 5. Histórias

Convenções usadas nas histórias:

- **Lista ativa** = tarefas não concluídas; **Concluídas** = tarefas concluídas.
- **Ordenação vigente** = ordenação em uso; até a US-07 é sempre a padrão
  ("criação mais recente primeiro").
- **Etiqueta** = label do pedido original. Nos textos de UI e nos critérios de
  aceite o termo exibido é "Etiqueta" / "Sem etiqueta"; "label" permanece apenas
  como termo interno de produto/código.
- **Linha da tarefa** = título (até 2 linhas), descrição (até 2 linhas) e
  metadados; o truncamento com reticências é **apenas visual** — o dado completo
  permanece íntegro e acessível no formulário e ao TalkBack.
- O indicador de prazo é sempre calculado pela **data local do aparelho**, com
  "hoje" definido à 00:00 do dia corrente.
- Eventos de analytics em `snake_case`, **sem PII**: nunca enviar título,
  descrição, nome de etiqueta ou texto da busca. No MVP os eventos são apenas
  **ganchos locais no-op** (sem envio de rede e sem SDK) e **não são critério de
  aceite** de nenhuma história (ver §6 e §8).
- US-01 e US-04 são as histórias maiores: o plano do Developer deve quebrá-las em
  no máximo 6 tarefas (PROTOCOL §17.3). Na US-04, chip e truncamento (AC 10–12)
  são candidatos naturais a uma tarefa própria; não é aceitável entregar a
  história como uma tarefa única.

### US-01 — Cadastrar, editar e excluir tarefas com persistência local

**Descrição.** Como usuário, quero cadastrar tarefas com título e descrição
opcional, editá-las e excluí-las, com os dados guardados no aparelho, para usar o
app sem internet e sem conta.

**Critérios de aceite**

1. A tela principal exibe a lista ativa e uma ação primária "Nova tarefa" que abre
   o formulário de criação.
2. O formulário tem campo **título** (obrigatório) e **descrição** (opcional).
3. Ao salvar com título vazio ou apenas espaços, a tarefa não é criada e aparece
   erro inline no campo título; o formulário permanece aberto com o conteúdo
   digitado.
4. Título e descrição são armazenados com espaços nas extremidades removidos;
   descrição composta apenas por espaços é armazenada como vazia.
5. Ao salvar uma tarefa válida, ela é persistida e entra na lista ativa respeitando
   a **ordenação vigente**: com a ordenação padrão ("criação mais recente
   primeiro") aparece no topo; caso a ordenação padrão não esteja em uso, a lista
   permanece na ordenação vigente.
6. Tocar em uma tarefa abre o formulário em modo edição com os dados atuais;
   salvar atualiza a tarefa sem alterar sua data de criação.
7. Cancelar a edição/criação descarta as mudanças e retorna à lista sem
   alterações.
8. Excluir uma tarefa exige confirmação explícita ("Excluir"/"Cancelar");
   confirmada a exclusão, a tarefa desaparece da lista e do armazenamento.
9. Após fechar e reabrir o app (ou reiniciar o aparelho), todas as tarefas
   continuam disponíveis, sem rede.
10. A linha da tarefa mostra o **título** e a **descrição**, cada um com no máximo
    **2 linhas** e reticências quando exceder; tarefas sem descrição mostram
    apenas o título. O texto completo do título e da descrição fica disponível no
    formulário de edição e é anunciado por inteiro pelo TalkBack na linha.
11. Tarefas com títulos iguais são permitidas (duas tarefas distintas podem ter
    o mesmo título).
12. No primeiro uso, com a lista ativa vazia, aparece um estado vazio com texto
    explicativo e a ação "Nova tarefa", sem elementos quebrados.
13. Em falha de armazenamento ao salvar ou excluir, o app mostra mensagem não
    técnica (sem detalhes de banco) com a ação "Tentar novamente"; na criação/
    edição, o conteúdo digitado é preservado para nova tentativa.

**Casos de borda**

- Título apenas com espaços/quebras de linha → tratado como vazio.
- Título ou descrição com mais de 2 linhas são truncados visualmente com
  reticências, sem alterar a altura da linha; o conteúdo completo permanece
  íntegro no armazenamento, no formulário e no TalkBack.
- Exclusão cancelada mantém a tarefa intacta.
- Criação interrompida (app em segundo plano/fechado) não gera tarefa parcial.
- Falha de armazenamento seguida de "Tentar novamente" não duplica a tarefa já
  salva.

**Dependências:** nenhuma.
**Métrica/eventos:** `task_created_success`, `task_updated`, `task_deleted`
(ganchos locais no-op, sem PII).

---

### US-02 — Limites e contadores de texto no formulário

**Descrição.** Como usuário, quero que o app limite o tamanho do título e da
descrição e mostre a contagem de caracteres, para que textos muito longos não
quebrem a lista nem o formulário.

**Critérios de aceite**

1. O título aceita no máximo **120 caracteres** e exibe contador visível no
   formulário (`atual/120`).
2. A descrição aceita no máximo **2000 caracteres** e exibe contador visível no
   formulário (`atual/2000`).
3. Ao atingir o limite, a digitação excedente é bloqueada (não salva nem exibe
   caracteres além do limite) e o contador permanece correto.
4. Contadores e limites valem tanto na criação quanto na edição de tarefa.
5. Colar um texto maior que o limite não quebra o layout: o campo é limitado e o
   contador reflete o valor efetivamente aceito.
6. Textos no limite continuam legíveis e **não alteram a altura das linhas** da
   lista: título e descrição seguem a US-01 AC 10 (até 2 linhas cada, com
   reticências), e o texto completo permanece no formulário e acessível ao
   TalkBack.

**Casos de borda**

- Texto com emojis/caracteres compostos não causa erro de contagem nem de layout.
- Contador em 0/120 e 0/2000 aparece corretamente ao abrir o formulário vazio.
- Não há migração de dados no MVP: não existem tarefas legadas acima do limite,
  portanto não há valor a truncar na abertura.

**Dependências:** US-01.
**Métrica/eventos:** nenhum evento novo; validação é bloqueante e coberta por
testes.

---

### US-03 — Concluir tarefas e aba "Concluídas"

**Descrição.** Como usuário, quero marcar tarefas como concluídas e vê-las em uma
aba separada, para distinguir o que ainda está pendente do que já foi feito.

**Critérios de aceite**

1. Cada tarefa da lista ativa exibe um checkbox (alvo de toque ≥ 48dp).
2. Marcar o checkbox move a tarefa da lista ativa para a aba "Concluídas",
   registrando a data/hora de conclusão, sem apagar nenhum dado.
3. A aba "Concluídas" lista as tarefas concluídas com o título riscado e a marca
   textual "Concluída em dd/MM/aaaa" (data local da conclusão, sem hora).
4. Desmarcar o checkbox de uma tarefa concluída devolve a tarefa à lista ativa
   preservando todos os dados da tarefa.
5. As abas "Ativas" e "Concluídas" existem na tela principal, com a aba "Ativas"
   selecionada por padrão; a aba selecionada persiste após reabrir o app.
6. Concluir ou reabrir uma tarefa não altera a data de criação; a posição na lista
   é recalculada pela **ordenação vigente** (padrão até a US-07).
7. Com a aba "Concluídas" vazia, aparece um estado vazio textual explicando que
   nenhuma tarefa foi concluída, sem ações quebradas.
8. Excluir uma tarefa concluída segue o mesmo fluxo de confirmação e de falha de
   armazenamento da US-01.

**Casos de borda**

- Tarefa concluída e depois excluída some das duas abas.
- Duplicidade de conclusão (toques rápidos) não cria registros duplicados.
- Conclusão próxima da meia-noite usa a data local do momento da conclusão; a
  marca "Concluída em" não muda depois.
- A contagem de itens (se exibida nas abas) é atualizada imediatamente e nunca
  fica negativa ou divergente.
- Alteração da data do aparelho não altera o estado de conclusão.

**Dependências:** US-01.
**Métrica/eventos:** `task_completed`, `task_reopened` (ganchos locais no-op).

---

### US-04 — Etiquetas (labels) com nome e cor de paleta fixa

**Descrição.** Como usuário, quero criar, editar e excluir etiquetas com nome
livre e uma cor de paleta fixa, associando no máximo uma etiqueta a cada tarefa,
para reconhecer o contexto das tarefas visualmente.

**Critérios de aceite**

1. Existe um fluxo de gerenciamento de etiquetas acessível a partir da tela
   principal (ação "Etiquetas") que lista as etiquetas existentes com nome e
   amostra de cor.
2. Criar etiqueta exige nome não vazio (até 30 caracteres, sem espaços nas
   extremidades); a cor vem de uma paleta fixa do produto com a **primeira cor
   pré-selecionada**, então não existe estado "sem cor". Salvar com nome vazio ou
   apenas espaços é bloqueado com erro inline.
3. Nomes de etiqueta são únicos sem diferenciar maiúsculas/minúsculas e acentos:
   tentar criar duplicata mostra erro e não cria registro.
4. A paleta fixa é um conjunto fechado de cores definido na fase de design; não há
   seletor de cor livre.
5. É possível editar uma etiqueta existente (nome e cor), com as mesmas regras de
   validação e unicidade da criação; o chip nas tarefas reflete a mudança
   imediatamente.
6. Excluir uma etiqueta exige confirmação; as tarefas que usavam essa etiqueta
   passam a ficar **sem etiqueta**, sem perda de título, descrição, prazo ou
   status.
7. O formulário de tarefa permite selecionar **uma** etiqueta existente ou
   "Sem etiqueta", e trocar/remover a etiqueta de uma tarefa já criada.
8. Com nenhuma etiqueta cadastrada, a tela "Etiquetas" exibe estado vazio com texto
   explicativo e a ação "Criar etiqueta".
9. Com nenhuma etiqueta cadastrada, o seletor do formulário de tarefa mostra
   "Sem etiqueta" e um atalho "Criar etiqueta" que abre a criação; ao voltar, o
   conteúdo já digitado no formulário é preservado e a etiqueta recém-criada
   aparece no seletor.
10. A linha da tarefa exibe a etiqueta como chip com a cor e o **nome** visível; a
    cor nunca é o único sinal de identificação. Quando o nome é truncado
    visualmente, o TalkBack anuncia o nome completo.
11. Tarefas sem etiqueta não exibem chip nem espaço reservado vazio.
12. O chip respeita o limite de largura da linha: nome longo é truncado com
    reticências e não empurra os demais elementos da linha.
13. Criar, editar e excluir etiquetas refletem imediatamente nas tarefas e
    persistem após reabrir o app.

**Casos de borda**

- Excluir a etiqueta em uso enquanto a tela de edição da tarefa está aberta:
  salvar a tarefa não recria a etiqueta nem falha.
- Etiqueta criada e nunca usada pode ser excluída normalmente.
- Nome com apenas caracteres invisíveis/espaços → tratado como vazio.
- Renomear uma etiqueta para um nome já existente (ignorando caixa/acentos) é
  bloqueado com erro e não altera o registro.
- Abrir a criação de etiqueta pelo atalho do formulário e cancelar: nenhum
  registro é criado e o conteúdo já digitado no formulário de tarefa permanece.
- Mudança de orientação ou acessibilidade com fonte ampliada não desalinha o chip.

**Dependências:** US-01.
**Métrica/eventos:** `label_created`, `label_updated`, `label_deleted`,
`task_label_changed` (ganchos locais no-op).

---

### US-05 — Data limite e indicador de proximidade

**Descrição.** Como usuário, quero definir uma data limite opcional por tarefa e
ver na lista um indicador de proximidade, para perceber atrasos e vencimentos sem
abrir cada tarefa.

**Critérios de aceite**

1. O formulário de tarefa permite escolher uma data limite (somente data local,
   sem hora), ver a data selecionada e removê-la (voltando a "Sem data limite").
2. A data limite é opcional; tarefa sem data não exibe indicador de prazo.
3. O indicador é calculado pela data local do aparelho e apresenta exatamente um
   dos estados:
   - **Atrasada** — data limite anterior a hoje;
   - **Hoje** — data limite igual a hoje;
   - **Amanhã** — data limite igual a hoje + 1 dia;
   - **Até 7 dias** — data limite entre hoje + 2 e hoje + 7 dias;
   - sem indicador — data limite a mais de 7 dias.
4. O indicador combina texto e ícone (cor não é o único sinal) e é anunciado pelo
   TalkBack junto ao título da tarefa (ex.: "Atrasada", "Vence amanhã").
5. O indicador é recalculado em três momentos objetivos: (a) ao abrir/reabrir o
   app; (b) ao retomar do segundo plano; (c) na primeira recomposição após
   mudança de data, hora ou fuso do aparelho — qualquer interação, rolagem ou
   navegação conta como recomposição. **Não existe timer de meia-noite no MVP**:
   com o app aberto e ocioso, o estado pode permanecer desatualizado até a
   primeira interação após a virada do dia.
6. Tarefa concluída (US-03) exibe a marca "Concluída em dd/MM/aaaa" em vez do
   indicador de prazo.
7. É possível escolher uma data no passado; nesse caso a tarefa é exibida como
   "Atrasada".
8. A data limite é exibida no formulário de edição no formato local pt-BR
   (dd/MM/aaaa) e persiste após reabrir o app.

**Casos de borda**

- Uma tarefa com data limite hoje continua "Hoje" enquanto a data local for hoje;
  após 00:00 do dia seguinte, passa a "Atrasada" na primeira
  interação/recomposição após a virada.
- Virada de dia com o app aberto e ocioso: os estados são atualizados na primeira
  interação/recomposição, não por timer automático (AC 5).
- Fuso horário ou data do aparelho alterados: a classificação segue a nova data
  local a partir da próxima recomposição/retomada.
- Tarefa sem prazo nunca aparece como "Atrasada".
- Tarefa concluída com prazo vencido mostra "Concluída em ...", não "Atrasada".
- Ano bissexto e troca de mês são tratados corretamente no cálculo.

**Dependências:** US-01, US-03 (estado de conclusão e marca "Concluída em" no
AC 6).
**Métrica/eventos:** `deadline_set` (apenas flag `has_deadline`),
`deadline_cleared` (ganchos locais no-op).

---

### US-06 — Busca por título e descrição

**Descrição.** Como usuário, quero buscar por texto na lista, considerando título
e descrição, para encontrar uma tarefa rapidamente quando a lista estiver grande.

**Critérios de aceite**

1. A tela principal tem um campo de busca visível com placeholder e ação de
   limpar.
2. A busca filtra a lista atual por correspondência de **substring** no título
   **ou** na descrição, ignorando maiúsculas/minúsculas e acentos (pt-BR), após
   remover espaços nas extremidades da consulta.
3. Os resultados são atualizados durante a digitação, sem ação de confirmar.
4. Consulta vazia ou apenas espaços restaura a lista completa sem alterar a
   ordenação vigente.
5. A busca atua sobre a aba visível: na aba "Ativas" não mostra tarefas
   concluídas; na aba "Concluídas" não mostra tarefas ativas.
6. Quando não houver resultados, o app exibe estado vazio contendo o texto
   buscado e a ação "Limpar busca".
7. Limpar a busca restaura a lista completa da aba atual, mantendo a ordenação
   vigente.
8. A busca não altera, cria nem exclui dados.
9. O filtro permanece ativo ao abrir e fechar o formulário de criação/edição a
   partir da lista, e ao retornar o resultado já reflete a criação, edição,
   conclusão ou reabertura feita.

**Casos de borda**

- Consulta com acento encontra texto sem acento e vice-versa.
- Consulta com espaços internos é tratada como substring única (ex.:
  "comprar leite" só encontra trechos que contenham essa sequência).
- Caracteres especiais/regex (ex.: `*`, `?`, `%`, `_`) são tratados como texto
  literal.
- Texto muito longo na consulta não quebra o layout nem causa erro.
- Busca em lista vazia mostra o estado vazio de busca, não o estado vazio de
  "nenhuma tarefa" (US-01).

**Dependências:** US-01, US-03 (escopo por aba).
**Métrica/eventos:** `search_performed` (apenas `result_count`),
`search_cleared` (ganchos locais no-op).

---

### US-07 — Ordenação da lista

**Descrição.** Como usuário, quero escolher como a lista é ordenada (alfabética,
data de criação ou data limite), para lê-la da forma que faz sentido no momento.

**Critérios de aceite**

1. Existe um seletor de ordenação acessível na tela principal com exatamente seis
   opções: título A-Z, título Z-A, criação mais recente primeiro, criação mais
   antiga primeiro, data limite mais próxima primeiro, data limite mais distante
   primeiro.
2. A ordenação padrão é "criação mais recente primeiro" na primeira abertura do
   app.
3. A opção escolhida é aplicada imediatamente à lista visível, sem recarregar a
   tela nem perder a busca ativa.
4. A escolha persiste após reabrir o app.
5. Ordenação por título ignora maiúsculas/minúsculas e acentos (collation pt-BR);
   títulos iguais são desempatados por criação mais recente primeiro.
6. Ordenação por criação usa a data/hora de criação, independente de edição ou
   conclusão.
7. Ordenação por data limite coloca tarefas **sem data** sempre ao final, em
   ambas as direções; empates são desempatados por criação mais recente
   primeiro.
8. A ordenação é aplicada de forma consistente nas abas "Ativas" e "Concluídas".
9. A opção selecionada fica visível (nome da ordenação atual) enquanto a lista é
   exibida.

**Casos de borda**

- Lista com 0 ou 1 item: o seletor continua acessível e não gera erro.
- Todos os itens sem data limite na ordenação por prazo: ordem cai no
  desempate por criação, sem falhas.
- Busca ativa + troca de ordenação mantém o filtro e reordena apenas os
  resultados.
- Mudança de orientação/fonte ampliada não fecha o seletor nem perde a escolha.

**Dependências:** US-01, US-05 (dados de prazo).
**Métrica/eventos:** `sort_changed` (apenas o enum da opção; gancho local no-op).

## 6. Requisitos não funcionais

### Plataforma e armazenamento

- App Android nativo para **celular**; tablet/Wear fora do MVP. Versão mínima de
  SDK definida na fase de arquitetura.
- 100% offline: todos os dados em armazenamento local do aparelho, persistindo
  entre execuções, atualizações do app e reinício do aparelho.
- Nenhum login, conta, rede obrigatória ou permissão sensível (contatos,
  localização, câmera, notificações) no MVP.
- Interface em **pt-BR**; todos os textos em recursos de string para permitir
  localização futura. Termos exibidos: "Etiquetas" / "Sem etiqueta".
- Tema: sem seletor no app e sem customização pelo usuário; a fase de design
  define se a UI segue o claro ou o escuro do sistema.

### Desempenho

- Abertura a frio até a lista utilizável em ≤ 2 s, medidos em **build release**
  num aparelho de referência de faixa intermediária (Android 13/API 33, 4 GB de
  RAM); o Architect confirma o aparelho de referência no `PROFILE.md`.
- Lista com 500 tarefas: rolagem sem frames acima de **16 ms** (frame budget),
  medidos em build release; não há margem para operação de UI bloqueante na
  thread principal.
- Busca com 500 tarefas: debounce de **≤ 250 ms** e resultados atualizados em
  **≤ 300 ms** após a última digitação.
- Criação/edição/exclusão refletidas na UI em ≤ 150 ms.

### Acessibilidade

- Alvos de toque ≥ 48dp; foco de teclado/TalkBack navegável em todos os
  controles.
- Informação nunca transmitida apenas por cor: etiqueta sempre com nome; prazo
  sempre com texto + ícone.
- **Texto truncado nunca perde informação**: título ou descrição podem ser
  exibidos com até 2 linhas e reticências na lista, mas o conteúdo completo é
  visível no formulário e anunciado por inteiro pelo TalkBack. Nome de etiqueta
  truncado no chip também é anunciado por inteiro.
- Ações (editar, excluir, concluir, limpar busca, trocar ordenação) e o estado de
  prazo nunca são truncados, inclusive com fonte a 200%.
- Contraste conforme Material 3 para textos, chips e indicadores.
- Suporte a fonte ampliada (até 200%) sem perder informação crítica nem desalinhar
  os elementos da linha.
- Descrições de acessibilidade em pt-BR para checkbox, chip de etiqueta, indicador
  de prazo e ações de excluir/editar.

### Segurança e privacidade

- Nenhum dado de tarefa em logs, analytics, relatórios de erro ou `.squad/`.
- **Nenhuma telemetria é enviada no MVP**: sem rede, sem SDK de analytics, sem
  SDK de anúncio ou rastreamento. Os nomes de eventos do §5 são apenas ganchos
  internos opcionais/no-op, não são critério de aceite e não podem introduzir
  dependência de rede. Telemetria real exigiria nova decisão de produto via
  discovery.
- Eventos, se registrados localmente, apenas com enums/contadores; nunca título,
  descrição, nome de etiqueta ou texto de busca.

### Qualidade

- Datas tratadas por data local do aparelho, sem horário, com testes de fronteira
  (virada de dia, fim de mês, ano bissexto, fuso alterado).
- Altura da linha da tarefa é estável: título com até 2 linhas, descrição com até
  2 linhas, metadados abaixo; truncamento é apenas visual.
- Estados de tela obrigatórios e mapeados a histórias:
  - lista ativa vazia e falha de armazenamento (com "Tentar novamente") → US-01;
  - limites e contadores de texto → US-02;
  - aba "Concluídas" vazia → US-03;
  - tela "Etiquetas" vazia (com "Criar etiqueta") e seletor de etiqueta sem
    nenhuma etiqueta cadastrada (com "Sem etiqueta" + atalho "Criar etiqueta")
    → US-04;
  - busca sem resultado (com o texto buscado e "Limpar busca") → US-06.
- Nenhum comportamento de uma história pode depender de outra de prioridade
  maior sem estar declarado na coluna "Depende de" da §4 e na descrição da
  história em `STORIES.json`.

## 7. Riscos, premissas e questões em aberto

### Riscos

| Risco | Impacto | Mitigação |
|:--|:--|:--|
| Cálculo de prazo depender da data do aparelho | Classificação incorreta se o usuário mudar data/fuso | Recalcular ao abrir, ao retomar e na primeira recomposição (US-05 AC 5); testar fronteiras |
| Indicador desatualizado com o app ocioso na virada do dia | Usuário vê "Hoje" para tarefa que já venceu | Regra explícita no AC 5 e no caso de borda; sem timer no MVP, atualiza na primeira interação |
| Busca sem índice em listas grandes | Lentidão com muitos itens | Debounce ≤ 250 ms e meta de ≤ 300 ms; validar com 500 tarefas |
| Exclusão de etiqueta usada | Perda de organização | Confirmação + tarefas ficam sem etiqueta, sem perda de dados |
| Escopo crescer para notificações/nuvem | Atraso no MVP | Não-objetivos explícitos; qualquer pedido novo volta ao discovery |
| Paleta fixa com contraste insuficiente | Barreira de acessibilidade | Design define cores com contraste; nome da etiqueta sempre visível |
| Histórias voltarem a referenciar funcionalidades futuras | Gate de aprovação por história quebra | Dependências na §4 e na descrição da história no `STORIES.json`; "ordenação vigente" em vez de seletor |
| US-01 e US-04 no teto de 6 tarefas | Plano de tarefas grosso ou não testável | Regra explícita na §5; chip/truncamento como tarefa própria na US-04 |

### Premissas

- Uso individual, um único usuário por aparelho, sem compartilhamento.
- Volume típico de até 500 tarefas e até 30 etiquetas.
- O usuário mantém a data do aparelho correta (o app não corrige fuso).
- A fase de design define a paleta fixa e os tokens visuais do indicador.
- Nenhuma telemetria real no MVP (decisão registrada na §8).
- O plano de cada história cabe em 2 a 6 tarefas (PROTOCOL §17.3).

### Questões em aberto

Nenhuma. As lacunas de contexto foram fechadas na descoberta e as decisões de
produto tomadas nas correções estão registradas na §8.

## 8. Decisões de descoberta e de produto

Respostas humanas incorporadas (arquivadas em
`.squad/history/20261029T233710Z/ANSWERS.md`; a cópia temporária em
`human/ANSWERS.md` foi arquivada após o consumo):

| ID | Pergunta | Resposta |
|:--|:--|:--|
| q1 | Armazenamento | Somente no aparelho, funcionando offline |
| q2 | Dispositivos | Celular Android |
| q3 | Conclusão | Checkbox move a tarefa para seção/aba "Concluídas" |
| q4 | Labels | Uma label por tarefa, cor de paleta fixa |
| q5 | Listas | Uma única lista de tarefas |
| q6 | Estados do indicador | Atrasada, Hoje, Amanhã, Próximos 7 dias |
| q7 | Lembretes | Sem notificações no MVP; apenas indicador visual |
| q8 | Ordenação | Título A-Z/Z-A, criação recente/antiga, prazo próximo/distante |

Decisões de produto tomadas para tornar os critérios testáveis (sem contrariar
as respostas):

| Tema | Decisão | Justificativa |
|:--|:--|:--|
| Limites de texto | Título ≤ 120, descrição ≤ 2000, nome de etiqueta ≤ 30 caracteres | Evita layout quebrado e conteúdo abusivo |
| Organização dos limites | Limites e contadores em US-02 própria | Mantém US-01 dentro de um plano de 2 a 6 tarefas |
| Título/descrição na linha | Até 2 linhas cada, com reticências; texto completo no formulário e integral no TalkBack | Resolve o conflito entre 120 caracteres, altura estável da linha e acessibilidade |
| Recálculo do prazo | Recalculado ao abrir, ao retomar do segundo plano e na primeira recomposição após mudança de data/fuso; sem timer de meia-noite | Regra única, observável e testável; o app não mantém serviço de timer |
| Data de conclusão | Registrada com data/hora e exibida na aba "Concluídas" como "Concluída em dd/MM/aaaa" | Dá uso ao dado e reduz ambiguidade do AC |
| Cor da etiqueta | Primeira cor da paleta pré-selecionada na criação | Elimina o estado "sem cor" sem validação extra |
| Unicidade de etiqueta | Nome único ignorando caixa/acentos | Evita etiquetas duplicadas confusas |
| Edição de etiqueta | Incluída no MVP (AC 5 da US-04) | Renomear/corrigir cor sem excluir e perder vínculos |
| Estados vazios de etiquetas | Tela vazia com "Criar etiqueta"; seletor sem etiquetas com "Sem etiqueta" + atalho "Criar etiqueta" | Cobre o primeiro uso do app, quando não há etiqueta alguma |
| Termo de UI | "Etiqueta"/"Sem etiqueta" na interface; "label" é termo interno | Coerência com pt-BR e strings localizáveis |
| Prazo | Somente data local (sem hora) | Sem notificações no MVP; "Hoje" é inequívoco |
| Estado de prazo | "Próximos 7 dias" passou a "Até 7 dias" (faixa hoje+2 a hoje+7) | O rótulo original contradizia a faixa de 6 dias |
| Datas passadas | Permitidas, exibidas como "Atrasada" | Permite registrar tarefas já vencidas |
| Busca | Substring única, sem acento e sem caixa, escopada à aba atual; filtro preservado ao voltar do formulário | Simples, previsível e suficiente para o MVP |
| Ordenação padrão | Criação mais recente primeiro | Captura rápida aparece no topo |
| "Ordenação vigente" | Até a US-07 é a ordenação padrão; nenhuma história anterior pressupõe seletor | Remove referência antecipada e mantém cada história testável |
| Nova tarefa na lista | Entra no topo apenas com a ordenação padrão; com outra ordem, respeita a vigente | Evita contradição entre US-01 e US-07 |
| Persistência de preferência | Ordenação e aba selecionada são lembradas | Reduz atrito na reabertura |
| Sem data na ordenação por prazo | Vão sempre ao final | Mantém tarefas com prazo em destaque |
| Concluídas | Título riscado + "Concluída em dd/MM/aaaa"; sem indicador de prazo | Foco no que ainda importa; evita alarme falso |
| Dependências entre histórias | Nenhuma história exige comportamento de história posterior; dependências na §4 e na descrição do `STORIES.json` | Preserva o gate de aprovação por história sem inventar schema |
| Desempenho | Frame ≤ 16 ms, debounce ≤ 250 ms, busca ≤ 300 ms | Torna os NFRs verificáveis |
| Analytics | Apenas ganchos locais no-op, sem rede/SDK; não é critério de aceite | Privacidade e coerência com o app 100% offline |
| Tema | Sem seletor/customização no MVP; design define claro ou escuro do sistema | Evita requisito sem história dona |

## 9. Fora de escopo

- Login, conta, sincronização, backup em nuvem ou exportação/importação.
- Notificações, lembretes, alarmes, widget, atalhos e deep links.
- Múltiplas listas, múltiplas etiquetas por tarefa, subtarefas, recorrência,
  anexos, comentários e checklists.
- Reordenação manual (drag-and-drop) e fixação (pin) de tarefas.
- Compartilhamento, colaboração e múltiplos usuários.
- Tablet, Wear OS, iOS, web e versões desktop.
- Idiomas além de pt-BR.
- Seletor de cor livre, seletor de tema no app e temas visuais customizados.
- Telemetria/analytics remoto e qualquer SDK de rastreamento.
- Timer/serviço de atualização do indicador de prazo em tempo real.
- Relatórios, gráficos, gamificação e estatísticas históricas.

## Traceability

| Story | Contribuição |
|:--|:--|
| US-01 | Cadastrar, editar e excluir tarefas com persistência local, linha com título/descrição em 2 linhas e acessível (ACs 1–13) |
| US-02 | Limites e contadores de texto no formulário, sem alterar a altura da linha (ACs 1–6) |
| US-03 | Concluir tarefas e aba "Concluídas" com "Concluída em dd/MM/aaaa" (ACs 1–8) |
| US-04 | Etiquetas com nome e cor de paleta fixa, estados vazios, seletor e chip truncado acessível (ACs 1–13) |
| US-05 | Data limite e indicador de proximidade com recálculo definido e sem timer (ACs 1–8) |
| US-06 | Busca por título e descrição, escopada à aba e preservada ao voltar do formulário (ACs 1–9) |
| US-07 | Ordenação da lista com seis opções (ACs 1–9) |
