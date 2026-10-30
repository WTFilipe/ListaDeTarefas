---
artifact: design_review
phase: design_review
version: 3
author: chaos-design
status: changes_requested
reviewer: none
verdict: changes_requested
next_action: designer
summary: "Os itens do v2 foram fechados, exceto o scroll interno do campo descrição (v2 M1.3); restam 3 majors (maxLines x rolagem, retry no S2, TopAppBar small x título em 2 linhas) e 10 minors de coerência, cobertura e 200%."
updated_at: 2026-10-30T06:40:00Z
---

# CHAOS REVIEW — Design (v3)

Artefatos revisados: `DESIGN_SPEC.md` v3, `COLOR_SCHEME.md` v2, `PREVIEW.html`
(atualizado na v3) e PRD v3. Review anterior: `CHAOS_REVIEW.md` v2 (2 majors,
9 minors).

**Veredito: `changes_requested`** — 0 blockers, **3 majors**, **10 minors**.

A v3 fechou de verdade o que o v2 pediu, com uma exceção: o item de **scroll
interno do campo descrição** (v2 M1.3) voltou como uma regra que não é
implementável como escrita. Os outros dois majors são de **cobertura de AC** e de
**mapeamento para Compose**, não de estética. Nenhuma cor precisa mudar; a
tabela de contraste continua válida e o `COLOR_SCHEME.md` só precisa de um ajuste
de texto.

O que está bom: o mapeamento de shapes foi corrigido de forma tecnicamente
correta, o corpo de S2 e os empty states agora declaram rolagem, o preview tem
card de estresse de S2, e as correções de 200% (chip/badge/ordenação/app bar)
estão refletidas no texto. O problema é que três regras de 200% ainda não têm
contrapartida na API ou na spec (M1, M3 e m9), e o erro de armazenamento do S2
perdeu a ação que a PRD exige (M2).

---

## Situação dos itens do review v2

| Item | Situação | Evidência |
|:--|:--|:--|
| M1 — rolagem a 200% | **Parcial** | §2.3 (corpo de S2 com `verticalScroll` e app bar fixa), §8.2 (empty states de S1/S3/busca roláveis) e o card de estresse de S2 do preview estão feitos. O item 3 (campo descrição) não foi resolvido: reabre como **M1** deste review. |
| M2 — shapes `none`/`full` | **Fechado** | §5 e §13.1 dizem explicitamente que `MaterialTheme.shapes` expõe só `extraSmall…extraLarge`; `none`/`full` são tokens do design system, definidos em um único ponto. §13.2 usa "full do design system" nas três menções. |
| N1 — chip de data exclusivo | **Fechado** | §9.3 ("um único chip por estado"), wireframe de §2.3 com só o chip com data, preview de S2 (claro/escuro) com um chip. O painel de componentes mostra os dois como estados separados e rotulados. |
| N2 — altura mínima de chip/badge | **Fechado** | §9.3 usa altura mínima 32dp/24dp com padding; preview `.chip`/`.badge` com `min-height` e o card `scaled` crescendo. |
| N3 — título da app bar em 2 linhas | **Fechado no texto; reabre no mapeamento** | §4 e §10 permitem 2 linhas e app bar crescendo; preview `.ttl` com `line-clamp:2`. A API mapeada continua sendo `TopAppBar` (small), que não cresce — ver **M3**. |
| N4 — shape da linha "Etiqueta" | **Fechado** | §5 (`extraSmall`), §13.2 e §13.3 (`LabelSelectorRow`). |
| N5 — limpar busca 48dp | **Fechado** | §10 (`Modifier.size(48.dp)` / `minimumInteractiveComponentSize()` com ícone 20dp) e preview `.iconbtn.small` 48×48. |
| N6 — seleção/fechamento de S5 | **Fechado** | §9.5, bloco "Comportamento dos seletores (S5 e S7)". |
| N7 — strings `ok`/`menu_edit` | **Fechado** | §11 (`ok`, `menu_edit`, `delete`); §9.5 e §9.7 usam as chaves. |
| N8 — gatilho de "Limite atingido" | **Fechado** | §9.4: `atual == máximo` nos três campos, contador continua `bodySmall` em `onSurfaceVariant`, erro tem precedência. |
| N9 — troca de aba com busca | **Fechado** | §8.4: consulta preservada e reaplicada na nova aba; "Limpar busca" restaura a lista completa da aba. |

**Conclusão da tabela:** 9 dos 11 itens do v2 fechados; M1 fica parcial e N3
reabre apenas por causa do mapeamento de Compose. O restante deste review trata
do que sobrou e de lacunas novas.

---

## Major

### M1 — Campo descrição: `maxLines = 6` e "sem rolagem interna" são mutuamente exclusivos

- **Local:** `DESIGN_SPEC.md` §2.3 (linhas 195–196), §9.4 (linhas 614–616), §10
  (linha 708), §14 (linha 942); `PREVIEW.html` card "Estresse — fonte a 200% (S2)"
  (linhas 1317–1351), `.scaled .field.area` (linha 405) e `.phone.stress .form`
  (linha 400).
- **Problema:** a spec diz que a descrição "cresce até `maxLines = 6` **sem
  rolagem interna**; acima disso quem rola é a página". Em Compose, um
  `OutlinedTextField`/`BasicTextField` com `maxLines = 6` **capa** o campo em 6
  linhas e rola internamente o excedente. Para "acima disso quem rola é a
  página", o campo precisa continuar crescendo (sem `maxLines` ou com
  `Int.MAX_VALUE`). As duas frases não podem valer juntas. É exatamente o
  "scroll aninhado" que o v2 M1.3 mandou resolver; a v3 trocou a pergunta por
  uma afirmação contraditória.
- **Impacto:** a descrição aceita **2000 caracteres** (US-02). O Developer vai
  escolher um dos dois comportamentos: com `maxLines = 6`, o campo tem rolagem
  interna e a página não é quem rola; sem `maxLines`, o campo cresce sem limite e
  a frase "maxLines = 6" é falsa. O card de estresse do preview usa um texto de
  ~110 caracteres a 32px, que fica **na fronteira** das 6 linhas; ele não
  evidencia de forma inequívoca o comportamento acima do limite. Em qualquer
  caso, a regra de 200% fica parcialmente não implementada.
- **Correção exigida:** escolher e declarar **um** comportamento, no §2.3, §9.4,
  §10, §14 e no preview:
  1. **Recomendado:** `minLines = 4`, **sem** `maxLines` (ou
     `Int.MAX_VALUE`); o campo cresce com o texto e a **página** é o único
     scroller. Remover "maxLines = 6" e "cresce até".
  2. Alternativa: manter `maxLines = 6` e declarar que o **campo** rola
     internamente e que a página não rola enquanto o campo tiver foco. Nesse
     caso, o texto de §10 ("o campo descrição não tem rolagem interna") precisa
     mudar.
  Em ambos, o card de estresse de S2 deve ter descrição longa o suficiente para
  passar de 6 linhas a 200% e demonstrar o comportamento escolhido.

### M2 — Falha de armazenamento no S2 não tem a ação "Tentar novamente" exigida pela PRD

- **Local:** `DESIGN_SPEC.md` §8.2, linha 526 ("Se ocorrer em S2, **banner
  inline** `errorContainer` acima do conteúdo + conteúdo digitado preservado"),
  §11 (`storage_error_inline`, `retry`) e `PREVIEW.html` S2 dark (linhas
  1084–1087) e painel de componentes (linhas 1210–1213).
- **Problema:** a PRD US-01 AC 13 exige, para falha ao salvar/excluir,
  "mensagem não técnica ... **com a ação 'Tentar novamente'**". Em S1/S3 a spec
  define o snackbar com essa ação; em S2 o banner mostra apenas "Não foi possível
  salvar. Seu texto foi mantido." e deixa o "Salvar" da app bar como retry
  implícito. Não há ação rotulada "Tentar novamente", nem string que componha
  mensagem + ação, nem regra dizendo que "Salvar" volta a ficar habilitado e
  reexecuta a mesma operação. O preview confirma o banner sem ação.
- **Impacto:** o AC 13 falha na criação/edição — o caminho onde o conteúdo
  digitado precisa ser preservado. Também há um risco de o banner ficar **dentro
  do corpo rolável**: após uma tentativa de salvar com o formulário rolado até a
  data, o banner pode estar fora da viewport, e o usuário não vê o erro.
- **Correção exigida:** declarar no §8.2 que o banner do S2 fica **ancorado
  abaixo da app bar, fora do corpo rolável** (ou que a página rola até ele), e
  incluir uma ação de texto **"Tentar novamente"** (`retry`) no próprio banner,
  reexecutando o save com o mesmo conteúdo, sem duplicar a tarefa, mantendo os
  campos preenchidos e o "Salvar" habilitado após a falha. Atualizar o preview do
  S2 (claro/escuro) e a tabela de strings; se for usada uma mensagem nova, ela
  precisa entrar em §11. Vale a mesma explicitação para a falha de **exclusão**
  em S3 (snackbar já tem a ação, mas o reenvio deve ser declarado).

### M3 — `TopAppBar` (small) não cresce, mas a spec exige título em 2 linhas a 200%

- **Local:** `DESIGN_SPEC.md` §4 (linha 365: "até 2 linhas; a app bar cresce em
  altura"; linhas 393–395), §10 (linhas 709–710), §13.2 (linhas 883–884:
  `TopAppBar` (small) e `TopAppBar` com `navigationIcon`/`actions`), §14 (não há
  linha de risco para o título a 200%); `PREVIEW.html` `.scaled .appbar` (linhas
  384–385) e card de estresse de S2 (linha 1321).
- **Problema:** o `TopAppBar` small do Material 3 tem altura fixa de 64dp. A spec
  pede que o título ocupe **até 2 linhas** e que a barra **cresça**; com
  `titleLarge` a 200% (44sp/56sp de line height), duas linhas passam de 112dp.
  O mapeamento de §13.2 manda justamente o `TopAppBar` (small), que não tem
  parâmetro de altura expansível; um Developer que siga a tabela obtém título
  cortado ou conteúdo estourando a barra. O preview demonstra o crescimento
  (`.appbar` com `min-height:auto` e título de 32px), mas a API mapeada não
  sustenta isso. É o mesmo tipo de defeito que o v2 M2 apontou em shapes, agora
  na app bar.
- **Impacto:** em "Nova tarefa"/"Editar tarefa" com fonte a 200%, o título é o
  único identificador da tela; cortá-lo viola o NFR de acessibilidade da PRD §6
  e a própria regra de §10 ("nada de altura fixa para texto"). O Developer pode
  inventar um `Surface`/`Column` local, gerando divergência entre S1, S2 e S3.
- **Correção exigida:** trocar o mapeamento de S2/S3 por uma **top app bar
  customizada** (altura mínima 64dp, cresce com o título em até 2 linhas, `←` e
  "Salvar" sempre visíveis) ou qualificar explicitamente `TopAppBar` +
  `MediumTopAppBar`/altura customizada; remover "(small)" da tabela ou dizer que
  ela vale apenas para S1. Registrar o risco em §14 e garantir que o card de
  estresse de S2 do preview seja a evidência do comportamento escolhido (ele já
  mostra a barra crescendo; falta a spec autorizar a API). Se a decisão for
  manter 1 linha, §4/§10 precisam deixar de prometer 2 linhas — mas aí o
  truncamento de "Editar tarefa" a 200% volta a ser informação perdida.

---

## Minor (novos)

### m1 — `COLOR_SCHEME.md` atribui "abas e ordenação" a `secondary`, mas a spec usa `primary`/`onSurfaceVariant`

- **Local:** `COLOR_SCHEME.md` §2 (linha 56: "apoio (abas, ordenação, badge
  'Amanhã')") e §11 (linha 436: "abas, chip de ordenação, badge 'Amanhã'") vs.
  `DESIGN_SPEC.md` §3 (linha 322: aba selecionada `primary`, indicador `primary`;
  linha 323: botão de ordenação `onSurfaceVariant`).
- **Problema:** são duas instruções normativas conflitantes. O Developer que
  seguir o color scheme colore a aba/ordenação com `secondary`; o que seguir a
  spec usa `primary` para a aba e `onSurfaceVariant` para o botão.
- **Correção:** ajustar o texto do `COLOR_SCHEME.md` para "badge 'Amanhã'" e
  "apoios secundários", sem citar abas/ordenação; a spec está coerente com o
  preview e deve prevalecer. Se o texto mudar, incrementar o color scheme para
  v3.

### m2 — Chip "Definir data": variante sem token declarado e copy divergente da PRD

- **Local:** `DESIGN_SPEC.md` §3, linha 329 (chip de data = `surfaceContainerLow`
  + borda `outlineVariant` + `onSurface`), §9.3 (linhas 602–607: "sem data → chip
  'Definir data' (outlined, mínimo 48dp)"), §11 (`deadline_action_set` =
  "Definir data"), `PREVIEW.html` `.dateempty` (linhas 283–287 e 1203–1206) e
  PRD US-05 AC 1 (linha 343: "voltando a 'Sem data limite'").
- **Problema:** a tabela de cores define **um** estilo de chip de data, mas a
  §9.3 descreve **dois** estados (com data e sem data) e o preview pinta o estado
  vazio com fundo transparente, borda `outline` e conteúdo `primary` — nenhum
  desses três tokens está na linha da tabela. Além disso, a PRD cita "Sem data
  limite" como o estado de retorno, e a UI usa "Definir data" (ação), sem declarar
  a diferença.
- **Correção:** acrescentar a variante "sem data" à tabela de §3 (container,
  borda, conteúdo e ícone) e dizer explicitamente que ela é o estado vazio do
  chip; decidir a copy — ou o chip mostra "Sem data limite" e a ação de definir é
  o toque no próprio chip, ou a spec registra que "Definir data" substitui o texto
  da PRD como rótulo de ação. O preview deve refletir a mesma escolha.

### m3 — S7 sem etiquetas pode renderizar "Criar etiqueta" duas vezes; auto-seleção não está na PRD

- **Local:** `DESIGN_SPEC.md` §2.6 (bullets das linhas 291 e 304–306),
  `PREVIEW.html` S7 sem etiquetas (linhas 903–911) e PRD US-04 AC 9.
- **Problema:** o S7 normal tem a ação de texto "+ Criar etiqueta" no rodapé; o
  estado vazio também define um botão de texto "Criar etiqueta". A spec não diz
  que a ação do rodapé desaparece no vazio, então podem existir dois alvos com a
  mesma ação — o mesmo problema de CTA duplicada que a v3 resolveu no FAB. Além
  disso, a spec diz que a etiqueta recém-criada "aparece **selecionada**"; a PRD
  AC 9 só exige que ela "apareça no seletor".
- **Correção:** declarar que, no estado vazio, só existe **uma** ação "Criar
  etiqueta" (o rodapé some ou se funde com o botão do empty) e que a etiqueta
  criada é automaticamente selecionada como decisão de design — ou remover
  "selecionada" e manter apenas "aparece no seletor". Alinhar o preview.

### m4 — Largura máxima do chip de etiqueta desconta duas vezes a área do menu

- **Local:** `DESIGN_SPEC.md` §8.3, linha 544 ("largura máxima = largura da
  coluna de texto − 48dp (área do menu)"), grade da linha em §2.2/§8.3
  (`[checkbox 48dp][texto 1fr][menu 48dp]`) e `PREVIEW.html` `.task` (linha 167)
  /`.chip` (linha 185, `max-width:100%`).
- **Problema:** o menu já é a terceira coluna do grid; a coluna de texto **não
  inclui** os 48dp do menu. Subtrair 48dp de novo estreita o chip sem motivo e
  contradiz o preview, que usa 100% da coluna. Em nomes longos, isso antecipa o
  ellipsis e reduz a informação visível sem necessidade.
- **Correção:** remover a subtração e definir a largura máxima como a largura da
  coluna de texto (ou explicar que os 48dp são reservados dentro da coluna, caso
  o layout real seja diferente do grid do wireframe). Alinhar preview e spec.

### m5 — Preview não sustenta medidas declaradas na spec (linha, gaps, amostras)

- **Local:** `PREVIEW.html` `.task` (linha 167), `.meta` (linha 179), `.palette`
  (linha 335), `.swatch`/`.swatchwrap` (linhas 336–343), `.btn` (linhas 230–234);
  `DESIGN_SPEC.md` §2.1 (linha 117: altura mínima da linha 72dp), §8.3 (linhas
  541–545: metadados com 4dp; chip com largura da coluna), §2.5 (linhas 261–264:
  gutter 8dp, célula 64dp, alvo ≥48dp, círculo 32dp), §4 (linha 386: nome da cor
  em `bodySmall`), §9.1 (linhas 569–575: botões 40dp de altura / 48dp de toque).
- **Problema:** o preview é a evidência visual da spec, mas diverge em pontos
  mensuráveis:
  1. `.task` não tem `min-height`; uma linha só com título mede ~64dp (48dp do
     checkbox + 16dp de padding), abaixo dos **72dp** de §2.1.
  2. `.meta` usa `gap:6px` vs. **4dp** da §8.3; `.palette` usa `gap:12px` vs.
     **8dp** da §2.5.
  3. `.swatchwrap span` usa 11px (`labelSmall`) vs. **`bodySmall` (12sp)** da
     §4; `.swatch` tem 32px sem uma célula de 64dp nem alvo de 48dp visível.
  4. `.btn`/`.txtbtn` têm 40px de altura sem demonstrar o alvo de toque de 48dp
     exigido na §9.1/§10.
- **Impacto:** o Developer/QA que usar o preview como referência reproduz uma
  linha mais baixa, um grid mais solto e alvos de toque menores do que a spec
  normativa. No caso das amostras de cor, o alvo de 48dp é justamente o que a
  §10 exige.
- **Correção:** alinhar o CSS do preview à spec (`min-height:72px` em `.task`,
  `gap:4px` em `.meta`, `gap:8px` em `.palette`, nome 12px/`bodySmall`), envolver
  a amostra em uma célula de 64dp com área de toque de 48dp e incluir o estado de
  hover/focus no preview ou declarar que ele é só visual. Para os botões,
  demonstrar o alvo de 48dp (container) sem mudar a altura visual de 40dp.

### m6 — Conteúdo/documentação do preview: data de S8 incoerente e §15 não lista S7/S8 nem completa motion

- **Local:** `PREVIEW.html` S8 (linhas 926–936: "Outubro 2026" com 12
  selecionado) vs. S2 (linha 612: "12/11/2026", novembro); `DESIGN_SPEC.md` §15
  (linhas 947–967) e tabela de motion do preview (linhas 1354–1365) vs. §7
  (linhas 456–469).
- **Problema:**
  1. O date picker de S8 mostra **outubro de 2026** com o dia 12 selecionado,
     enquanto o exemplo de S2 usa **12/11/2026** (novembro). São cartões
     separados, mas a referência visual fica inconsistente para o mesmo fluxo.
  2. §15 abre com "mostra exatamente" e lista S1–S6 (e componentes), mas o
     preview também tem **S7 (2 estados)** e **S8**; a lista está desatualizada.
  3. §15 diz "galeria completa dos 15 tokens de tipografia, da escala de shape e
     da tabela de motion"; a tabela de motion do preview mostra **6** entradas,
     enquanto §7 tem **11** (faltam foco, snackbar e FAB; o preview também
     combina sheet/dialog e entrada/saída de item).
- **Impacto:** não quebra implementação, mas enfraquece a rastreabilidade
  visual e a confiança na afirmação "mostra exatamente". QA pode concluir que S7/
  S8 ou os estados de motion não foram cobertos.
- **Correção:** trocar o mês do S8 para **novembro de 2026** (ou o chip de S2
  para 12/10/2026); acrescentar S7 (2 estados), S8 e o S4 de duplicata à lista de
  §15; completar a tabela de motion do preview com as 11 entradas de §7 (ou
  desdobrar sheet/dialog e entrada/saída) ou remover a palavra "completa".

### m7 — Estado de falha ao **salvar** etiqueta no S4 não está declarado

- **Local:** `DESIGN_SPEC.md` §2.5, item 4 (linhas 274–278: cobre loading e
  **falha ao carregar**, não falha ao salvar), §8.2 (linha 526: erro de
  armazenamento só S1/S3/S2) e §11 (`labels_load_error`, sem string de falha de
  save de etiqueta).
- **Problema:** criar/editar etiqueta é uma operação de escrita, mas só existe
  estado de erro para **load**. Um Developer que siga a spec não tem UI definida
  para falha de persistência do S4; pode fechar o sheet, perder o nome digitado
  ou não mostrar ação de nova tentativa.
- **Correção:** declarar no S4 e em §8.2 um banner/snackbar de falha de save com
  "Tentar novamente", o sheet aberto e o nome/cor preservados; adicionar a string
  correspondente em §11. Se a decisão for delegar ao host (S1/S3), dizer isso
  explicitamente.

### m8 — `contentDescription` da amostra de cor: nome sozinho vs. string com sufixo

- **Local:** `DESIGN_SPEC.md` §10 (linhas 694–696: "cada amostra tem
  `contentDescription` com o nome da cor ("Vermelho"), `role = RadioButton` e
  `selected`") e §11 (linha 813: `a11y_label_color` = "%1$s, cor da etiqueta").
- **Problema:** há duas descrições normativas diferentes para o mesmo nó. O
  Developer escolhe uma; a outra vira string não usada. A seleção já é coberta por
  `selected`, mas o texto falado muda entre implementações.
- **Correção:** escolher uma (recomendo o nome + sufixo, porque desambigua o
  grupo de 8 amostras) e usar a mesma no contrato `LabelColorGrid` e em §10;
  remover a variante não usada de §11.

### m9 — Sobras de 200%: botões com altura fixa e grade de cores de 4 colunas

- **Local:** `DESIGN_SPEC.md` §9.1 (linhas 569–577: "Altura / touch 40dp /
  48dp"; "Label `labelLarge`; 1 linha; **nunca truncado**"), §2.5 (linhas
  261–264: 4 colunas, célula 64dp, nome da cor em `bodySmall`), §4 (linha 386:
  nome da cor "1 linha, centrado") e §10 (linha 701: "Font scale até 200%: sem
  alturas fixas para texto"; linha 708: "o campo descrição não tem rolagem
  interna"); `PREVIEW.html` `.btn` (linha 232, `height:40px`) e `.palette`
  (linha 335, 4 colunas).
- **Problema:**
  1. Os botões declaram **altura** 40dp; a 200%, `labelLarge` vira 28sp/40sp e
     não cabe em 40dp com padding. O preview fixa `height:40px` e não tem card
     de botão a 200%. Colide com §10 ("ações ... nunca truncadas") e com a
     própria regra N2 que a v3 aplicou a chip/badge.
  2. A grade de cores tem **4 colunas fixas** e célula de 64dp com nome de cor
     em **1 linha**; a 200%, "Verde-água"/"Mostarda" (24sp) não cabem em 64dp
     de largura. O sheet rola na vertical, mas a grade não muda de colunas, então
     os nomes serão cortados ou estourarão a célula. O `contentDescription`
     salva o TalkBack, mas o usuário de baixa visão sem leitor perde o nome —
     justamente o sinal que não é cor.
- **Correção:** trocar "altura 40dp" por **"altura mínima 40dp"** (cresce com a
  fonte; alvo de toque 48dp) em §9.1 e no preview, com um botão a 200% no card
  de estresse. Para o S4, declarar o comportamento a 200%: **2 colunas** (ou
  nomes com quebra/`minHeight` e ellipsis + nome completo no CD) e adicionar um
  card de estresse do S4 — ou registrar explicitamente que a grade mantém 4
  colunas e os nomes podem truncar, o que eu **não** recomendo, por contrariar
  a regra de não depender só de cor.

### m10 — `SearchField`: borda de repouso não declarada; `OutlinedTextField` mostra borda por default

- **Local:** `DESIGN_SPEC.md` §3 (linha 321: container `surfaceContainerHigh`,
  sem borda de repouso), §8.1 (linha 491: repouso = container; foco = borda
  `primary` 2dp) e §13.2 (linha 892: "`OutlinedTextField` com shape `full` do
  design system e container `surfaceContainerHigh`"); `PREVIEW.html` `.search`
  (linhas 135–139: sem borda em repouso).
- **Problema:** `OutlinedTextField` desenha uma borda `outline` em repouso por
  padrão. Como a spec não diz para removê-la, o campo real terá uma borda que
  não existe no preview nem na tabela de §3; se o Developer remover, precisa
  saber que a borda de foco continua `primary` 2dp. É uma decisão aberta de uma
  linha em um componente de alto uso.
- **Correção:** declarar em §13.2 (e no contrato de `SearchField`) que o estado
  de repouso usa `unfocusedBorderColor = Color.Transparent` (ou trocar para a
  variante filled), mantendo `focusedBorderColor = primary` com 2dp e o cursor
  `primary`; assim a implementação bate com §3 e com o preview.

---

## Pontos fortes

- **Correção real, não cosmética:** o mapeamento de shapes agora corresponde à
  API (`MaterialTheme.shapes` com cinco tokens; `none`/`full` no design system em
  um ponto único), o corpo de S2 e os empty states têm regra de rolagem, e o
  preview ganhou um card de estresse de S2 com a app bar fixa. O v2 M1.3 é a
  única pendência herdada.
- **Preview autocontido de verdade:** verifiquei por busca que
  `PREVIEW.html` não tem `<script>`, `http`, `https`, `url(`, `@import` nem
  `src=`; usa CSS inline e sprites SVG locais, sem PII (nomes e datas genéricos).
- **Spec sem cor literal:** busca por hex em `DESIGN_SPEC.md` só encontra a
  própria proibição em "Don'ts" (`Color(0x…)`); o `COLOR_SCHEME.md` continua
  fonte única. A tabela de contraste §8 é coerente com os pares publicados
  (incluindo os piores casos: `onContainer` Laranja escuro 6,97:1 e `dot`
  Mostarda sobre container claro 4,00:1).
- **Modelo único de etiqueta:** S7 com radios, linha somente leitura no S2 e
  `LabelSelectorRow` no contrato de composição impedem a volta dos chips inline.
- **Chrome consistente:** S1 mantém app bar, busca, abas e ordenação em todos os
  estados; FAB oculto apenas onde o empty tem CTA primária; uma única badge de
  prazo por tarefa.
- **Acessibilidade acima da média:** 48dp no chip de data e no limpar busca,
  `stateDescription`/`onClickLabel` no checkbox, texto integral no CD apesar do
  truncamento, `liveRegion = Polite` em erro, foco de teclado visível, RTL por
  `start`/`end`, fonte 200% com rolagem definida (salvo M1/M3/m9).
- **Motion sóbrio:** só `tween`, sem bounce; decisão explícita de não animar cor
  do indicador de prazo, coerente com a ausência de timer na PRD US-05 AC 5.
- **Cobertura de PRD:** S1–S8 cobrem US-01…US-07; os estados obrigatórios de
  §6 da PRD estão mapeados; `## 16. Traceability` cobre todas as histórias.
- **Strings:** praticamente toda a UI está em chaves (§11), com placeholders,
  datas dd/MM/aaaa e termos de produto ("Etiqueta"/"Sem etiqueta"); as lacunas
  são M2, m2 e m8.

## Checklist adversarial

| Item | Situação | Observação |
|:--|:--|:--|
| Cores por token, sem hex na spec | OK | `DESIGN_SPEC.md` sem hex; `COLOR_SCHEME.md` é a fonte |
| Scheme light/dark completo, papéis `on*` | OK | §4/§5 completos, incluindo `surfaceContainer*`, `inverse*`, `scrim`, `*Fixed` |
| Contraste documentado | OK | §8; amostragem coerente com os piores casos |
| Tipografia na escala MD3 | OK | 15 tokens, sem tamanho inventado; ressalva de 200% em m9 |
| Shape/elevação/tonalidade | OK | M2 do v2 fechado; FAB `large`, dialog/sheet `extraLarge`, chip `small` |
| Motion MD3, sem animação gratuita | OK | só `tween`; sem bounce; sem animação de cor do prazo |
| Estados rest/hover/focus/pressed/disabled/loading/empty/error/success | **Parcial** | M2 (retry no S2) e m7 (save de etiqueta) |
| Acessibilidade (48dp, CD, contraste, RTL, 200%) | **Parcial** | M1, M3, m9; m5 (amostras 48dp) e m8 (CD) |
| Consistência entre telas/componentes | **Parcial** | m1, m2, m4, m6 |
| Cobertura da PRD (telas/fluxos/estados) | **Parcial** | M2 (AC 13 no S2); m2 (copy de data) |
| Copy/strings localizáveis | **Parcial** | m2, m8; `storage_error_inline` sem retry (M2) |
| Viabilidade em Compose | **Parcial** | M1 (`maxLines`), M3 (`TopAppBar`), m10 (`OutlinedTextField`) |
| Preview autocontido, sem PII, coerente | Autocontido OK; coerência **parcial** | m5, m6 |
| `## Traceability` cobre todas as US | OK | US-01…US-07 |

## Próximo passo

Designer corrige **M1–M3** e, no mesmo passe, os minors **m1–m10** (todos são
uma frase, uma tabela ou um ajuste de CSS; nenhum exige redesenho). Incrementar
`DESIGN_SPEC.md` para **v4** e `COLOR_SCHEME.md` para **v3** (m1 muda o texto;
m2 pode acrescentar uma linha de variante sem cor nova) e atualizar o
`PREVIEW.html`. Devolver com `status: in_review`, `verdict: pending`,
`next_action: chaos-design`.

**Nenhuma cor precisa mudar** para fechar este review: o alvo é
implementabilidade, cobertura de AC e coerência spec↔preview. A paleta de
etiquetas e a tabela de contraste de §8 permanecem válidas. O orquestrador deve
checar o contador de iterações do escopo design (PROTOCOL §8) antes de reenviar
ao Designer; este é o terceiro `changes_requested` da fase.

