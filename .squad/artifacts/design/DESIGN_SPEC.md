---
artifact: design_spec
phase: design
version: 3
author: designer
status: in_review
reviewer: chaos-design
verdict: pending
next_action: chaos-design
summary: "Especificação das telas de lista, formulário, etiquetas e estados com tokens MD3, rolagem a 200% definida (S2 e empty states) e mapeamento de shape corrigido."
updated_at: 2026-10-30T04:10:00Z
---

# DESIGN SPEC — App de Lista de Tarefas

> Fonte normativa do design. Não contém cores literais: todos os valores vêm de
> `MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`,
> `MaterialTheme.shapes.*` (mais os tokens `none`/`full` do design system) e da
> paleta de etiquetas definida em `.squad/artifacts/design/COLOR_SCHEME.md`.
> Apoio visual: `.squad/artifacts/design/PREVIEW.html`.

> **Correção da v3 após o review do Chaos Design (`CHAOS_REVIEW.md` v2):**
> - **M1** regra de rolagem a 200% declarada: corpo de S2 rolável com a top app
>   bar fixa (§2.3), empty states de S1/S3 também roláveis (§8.2), campo
>   descrição cresce até `maxLines` **sem** rolagem interna (§9.4) e card de
>   estresse de S2 no `PREVIEW.html` (§15).
> - **M2** mapeamento de shape corrigido: `MaterialTheme.shapes` expõe apenas
>   `extraSmall…extraLarge`; `none` e `full` passam a ser tokens do design system
>   do projeto, definidos em um único ponto (§5, §13.1, §13.2).
> - minors **N1–N9**: chip de data exclusivo (§2.3, §9.3), altura mínima de
>   chip/badge (§9.3), título da app bar em até 2 linhas (§4), shape
>   `extraSmall` na linha de etiqueta (§5, §13.3), alvo de 48dp no limpar busca
>   (§10), seleção/fechamento do S5 (§9.5), strings `ok`/`menu_edit` (§11),
>   gatilho de "Limite atingido" (§9.4) e busca preservada na troca de aba
>   (§8.4).

> **Correção da v2 após o review do Chaos Design (`CHAOS_REVIEW.md` v1):**
> - **M1** S2 passa a ter **apenas** ações na top app bar (`←` + "Salvar"); fim do
>   rodapé `[Cancelar] [Salvar]` e do `✕` alternativo.
> - **M2** seleção de etiqueta consolidada em **um** modelo: sheet S7 com radios;
>   a linha "Etiqueta" do S2 é somente leitura e abre o S7.
> - **M3** todos os estados de S1 mantêm app bar, busca, abas e ordenação.
> - **M4** eco da busca com `maxLines = 2` + ellipsis e `contentDescription`
>   completo; estado vazio rolável.
> - **M5** botão de ordenação pode quebrar em 2 linhas (sem corte) e mantém o nome
>   completo no `contentDescription`.
> - **M6** nova subseção **S4** (nome, contador 30, duplicata, grade de 8 cores,
>   estados e loading).
> - **M7** tabela de contraste do `COLOR_SCHEME.md` recalculada.
> - minors: scrim/sombra/badges/cores de exemplo do preview, CTA duplicada,
>   alvo de 48dp no chip de data, strings faltantes, obrigatório/opcional,
>   contador em um token, US-05 em S1, tons fora das paletas publicados,
>   cobertura real do preview, loading de S3/S7 e preservação de busca, exclusão
>   removida do formulário.

## 1. Escopo, telas e fluxos

App Android de celular, pt-BR, offline, tema claro/escuro **seguindo o sistema**
(sem seletor no app). Uma única lista de tarefas, uma etiqueta por tarefa.

### 1.1 Telas e superfícies

| ID | Superfície | Tipo | Histórias |
|:--|:--|:--|:--|
| S1 | Lista de tarefas (abas "Ativas"/"Concluídas") | tela raiz | US-01, US-03, US-05, US-06, US-07 |
| S2 | Formulário de tarefa (criar/editar) | tela empilhada | US-01, US-02, US-04, US-05 |
| S3 | Etiquetas | tela empilhada | US-04 |
| S4 | Criar/editar etiqueta | modal bottom sheet | US-04 |
| S5 | Ordenação | modal bottom sheet | US-07 |
| S6 | Confirmação de exclusão (tarefa / etiqueta) | alert dialog | US-01, US-03, US-04 |
| S7 | Seletor de etiqueta da tarefa (**modelo único**) | modal bottom sheet | US-04 |
| S8 | Seletor de data limite | date picker dialog | US-05 |

### 1.2 Fluxos cobertos

1. **Captura rápida:** S1 → toque no FAB "Nova tarefa" → S2 (criação) → "Salvar"
   → volta a S1 com a tarefa no topo (ordenação vigente).
2. **Edição:** S1 → toque na linha → S2 (edição, dados preenchidos) → "Salvar".
3. **Exclusão:** S1 → menu da linha (⋮) → "Excluir" → S6 → confirmação → S1 sem
   a tarefa; falha de armazenamento → snackbar com "Tentar novamente". **Não há
   exclusão dentro do formulário S2.**
4. **Conclusão:** S1 (aba Ativas) → checkbox → tarefa sai para "Concluídas"
   com "Concluída em dd/MM/aaaa"; desmarcar devolve à aba Ativas.
5. **Etiquetas:** S1 → ação "Etiquetas" → S3 → FAB "Criar etiqueta" → S4 →
   salvar; editar/excluir pela linha de S3 (exclusão em S6).
6. **Etiqueta na tarefa (modelo único):** S2 → linha "Etiqueta" (somente leitura,
   mostra a etiqueta atual) → **S7** → escolher etiqueta / "Sem etiqueta" /
   atalho "Criar etiqueta" (S4, preservando o conteúdo do formulário). A linha do
   S2 **não** contém chips de seleção.
7. **Prazo:** S2 → linha "Data limite" → S8 → data escolhida → chip com dd/MM/aaaa
   e ação de remover.
8. **Busca:** S1 → campo de busca → filtro na aba visível; sem resultado →
   estado vazio com o texto buscado + "Limpar busca". O filtro permanece ativo ao
   entrar e voltar de S2/S3, e a lista reflete a alteração.
9. **Ordenação:** S1 → botão com a ordenação vigente → S5 → escolha aplicada na
   hora, com busca preservada.

Ao voltar de S2/S3 para S1, **busca, aba e ordenação vigentes são preservadas**
(PRD US-06 AC 9, US-03 AC 5, US-07 AC 4).

### 1.3 Fora de escopo visual

Sem bottom navigation, sem drawer, sem múltiplas listas, sem drag-and-drop, sem
widget, sem notificações, sem tela de onboarding/login. Não há tela de
configurações nem seletor de tema. Não há exclusão a partir do formulário.

## 2. Layout e hierarquia visual

### 2.1 Fundamentos

- Grade base de **4dp**; múltiplos usados: 4, 8, 12, 16, 24, 32.
- Margem lateral de conteúdo: **16dp**; gutter entre colunas: **12dp**.
- Layout edge-to-edge com `Scaffold` e insets do sistema; top app bar consome o
  inset superior, a lista consome o inferior.
- Coluna de conteúdo com largura máxima de **600dp**, centralizada (celular em
  paisagem não estica a linha).
- Altura mínima da linha de tarefa: **72dp** (cresce com font scale, nunca
  encolhe). A altura é estável porque título e descrição têm no máximo 2 linhas.

### 2.2 S1 — Lista (wireframe)

```
┌──────────────────────────────────────────────┐
│ Tarefas                        [Etiquetas]   │  TopAppBar (64dp)
├──────────────────────────────────────────────┤
│ ⌕  Buscar por título ou descrição         ✕  │  SearchField (56dp)
│                                              │
│   Ativas            │      Concluídas        │  TabRow (48dp)
│ ───────────                                  │
│ Ordenação: Criação (mais recente)  ⌄         │  SortRow (≥40dp, até 2 linhas)
├──────────────────────────────────────────────┤
│ ☐  Comprar leite                             │
│    Ver oferta do mercado da esquina e…       │  TaskRow (≥72dp)
│    ▮ Trabalho   ⚑ Atrasada                   │
├──────────────────────────────────────────────┤
│ ☐  Revisar proposta                          │
│    ▮ Estudos   ◷ Hoje                        │
├──────────────────────────────────────────────┤
│ ☐  Ligar para o dentista                     │
│    ⌷ Amanhã                                  │
│                                              │
│                                 ┌──────────┐ │
│                                 │ + Nova   │ │  Extended FAB
│                                 │  tarefa  │ │
└──────────────────────────────────────────────┘
```

Hierarquia: **título da tarefa** (mais forte) → descrição (secundária) →
metadados (etiqueta e prazo) → ações. O FAB é a única ação flutuante.

Regras do chrome de S1 (valem para **todos** os estados):

- App bar, SearchField, abas Ativas/Concluídas e SortRow são **sempre
  renderizados**, inclusive com lista vazia, aba Concluídas vazia, busca sem
  resultado e erro de armazenamento. Só o corpo central troca (§8.2).
- O FAB é ocultado **apenas** quando o estado vazio exibe uma ação primária
  própria ("Nova tarefa" em lista ativa vazia; "Criar etiqueta" em etiquetas
  vazias), evitando dois alvos primários idênticos (§9.2).
- O botão de ordenação ocupa até **2 linhas** e cresce em altura; nunca corta o
  nome da ordenação vigente.

### 2.3 S2 — Formulário (wireframe)

```
┌──────────────────────────────────────────────┐
│ ←  Nova tarefa                     [Salvar]  │  TopAppBar (64dp mín.)
├──────────────────────────────────────────────┤
│ Título                                   0/120│  OutlinedTextField
│ ┌──────────────────────────────────────────┐ │
│ │                                          │ │
│ └──────────────────────────────────────────┘ │
│ Obrigatório                                  │  supportingText
│ Descrição (opcional)                  0/2000 │
│ ┌──────────────────────────────────────────┐ │
│ │                                          │ │
│ │                                          │ │
│ └──────────────────────────────────────────┘ │
│ Etiqueta                                     │
│ ┌──────────────────────────────────────────┐ │
│ │ ▮ Trabalho                          ⌄    │ │  linha somente leitura → S7
│ └──────────────────────────────────────────┘ │
│ Data limite                                  │
│ ( 📅 12/11/2026  ✕ )                         │  um único chip (§9.3)
└──────────────────────────────────────────────┘
```

Ordem fixa: título → descrição → etiqueta → data limite.

Rolagem (M1):

- O corpo do S2 — tudo abaixo da top app bar — é uma `Column` com
  `Modifier.verticalScroll(rememberScrollState())`; a top app bar permanece fixa.
  A ordem título → descrição → etiqueta → data limite é preservada e
  nenhum campo fica inalcançável com fonte a 200% (US-04 AC 7, US-05 AC 1).
- O campo descrição **cresce** até `maxLines = 6` sem rolagem interna; acima
  disso quem rola é a página (§9.4). O foco de teclado/TalkBack rola o campo para
  a área visível automaticamente.
- A seção "Data limite" tem **um único chip**: com data → chip com a data e `✕`;
  sem data → chip "Definir data". **Nunca os dois ao mesmo tempo** (§9.3).
- O botão "Salvar" da app bar fica sempre visível (não rola com o corpo); a
  validação inline continua ancorada ao campo e faz a página rolar até ele.

Regras de ação (M1):

- **Uma única arquitetura:** ações somente na top app bar. À esquerda `←`
  (voltar/cancelar), à direita "Salvar" como `TextButton`.
- **Não existe rodapé de ações** no S2 e **não** existe `✕` na top app bar. `←`
  vale para S2 e S3 (telas empilhadas). O único `✕` do produto é o de **limpar
  busca** dentro do campo e o de remover data no chip.
- Voltar/cancelar descarta as mudanças e retorna a S1 sem alterações
  (PRD US-01 AC 7); não há diálogo de confirmação de descarte no MVP.
- **Não há ação destrutiva no S2.** Excluir só pelo menu da linha (⋮) em S1
  (PRD US-01 AC 8).
- A linha "Etiqueta" é **somente leitura** (chip da etiqueta atual ou
  "Sem etiqueta" + chevron) e abre o S7 (§2.6).

### 2.4 S3 — Etiquetas

```
┌──────────────────────────────────────────────┐
│ ←  Etiquetas                                 │
├──────────────────────────────────────────────┤
│ ● Trabalho                        ⋮          │  lista de etiquetas
│ ● Estudos                         ⋮          │
│ ● Pessoal                         ⋮          │
│                                 ┌──────────┐ │
│                                 │ + Criar  │ │  Extended FAB
└──────────────────────────────────────────────┘
```

Cada linha: amostra circular (`dot`), nome (`titleMedium`), menu (editar,
excluir). Altura mínima 56dp; toque na linha abre edição (S4).

### 2.5 S4 — Criar/editar etiqueta (especificação)

```
┌──────────────────────────────────────────────┐
│                  ▬▬                          │  drag handle
│ Criar etiqueta                               │  titleLarge
│ Nome                                     8/30│  singleLine
│ ┌──────────────────────────────────────────┐ │
│ │ Trabalho                                 │ │
│ └──────────────────────────────────────────┘ │
│ Máximo de 30 caracteres                      │  supportingText
│ Cor                                          │
│ (●) Vermelho (○) Laranja (○) Mostarda (○) Verde│  grade 4 colunas
│ (○) Verde-água (○) Azul  (○) Roxo    (○) Rosa │  alvo ≥48dp
│                                              │
│                        [Cancelar]  [Salvar]  │  ações do sheet
└──────────────────────────────────────────────┘
```

1. **Campo Nome:** `OutlinedTextField`, `singleLine = true`, limite **30
   caracteres**, contador `atual/30` (`bodySmall`) sempre visível à direita do
   `supportingText`, label flutuante "Nome".
2. **Erros inline:** nome vazio/só espaços → "Informe um nome"; nome duplicado
   (comparação sem caixa e sem acentos) → "Já existe uma etiqueta com esse nome".
   Em ambos: borda, label e `supportingText` em `error`, campo com foco, sheet
   **não fecha** e o texto digitado é preservado. O contador não é substituído
   pelo erro.
3. **Grade da paleta fixa:** 8 amostras em **4 colunas** × 2 linhas, gutter 8dp;
   célula de 64dp com **alvo de toque ≥48×48dp**; círculo visual de 32dp e nome
   da cor abaixo em `bodySmall`. Estados da amostra:
   - **rest:** círculo da cor + nome `onSurfaceVariant`;
   - **hover:** state layer 8% sobre a célula;
   - **focus:** anel `primary` 2dp ao redor da célula + layer 10%;
   - **selected:** anel `primary` 2dp com offset de 2dp + check 16dp em
     `onSurface` sobre disco `surface` de 20dp (contraste garantido em qualquer
     cor) + nome em `onSurface`; `role = RadioButton` e `selected = true`;
   - **disabled:** conteúdo 38%, sem interação.

   A **primeira cor ("Vermelho") é pré-selecionada** na criação (US-04 AC 2);
   não existe estado "sem cor".
4. **Estados do sheet:** loading de abertura/edição excedendo **200ms** →
   `CircularProgressIndicator` `primary` (32dp) + "Carregando etiquetas"; falha
   ao carregar → banner `errorContainer` com "Tentar novamente" e sheet aberto;
   ao salvar, se exceder 200ms, "Salvar" fica desabilitado com spinner 18dp +
   "Salvando…" (mesma regra do S2).
5. **Ações:** "Cancelar" (texto) e "Salvar" (`filled`) no rodapé do sheet; shape
   do sheet `extraLarge` no topo; altura máxima 90% da tela com rolagem.

### 2.6 S7 — Seletor de etiqueta (modelo único)

```
┌──────────────────────────────────────────────┐
│                  ▬▬                          │
│ Etiqueta                                     │  titleLarge
│ ◉ Sem etiqueta                               │  1ª opção sempre
│ ○ ● Trabalho                                 │
│ ○ ● Estudos                                  │
│ + Criar etiqueta                             │  ação de texto
└──────────────────────────────────────────────┘
```

- `ModalBottomSheet` (`surfaceContainerLow`, `extraLarge` no topo, drag handle,
  altura máxima 90% com rolagem).
- Linhas com `RadioButton` + `dot` + nome (`bodyLarge`), altura ≥48dp;
  "Sem etiqueta" é **sempre a primeira** opção e começa selecionada quando a
  tarefa não tem etiqueta.
- Selecionar aplica a escolha e fecha o sheet; o conteúdo do formulário é
  preservado (estado elevado no S2).
- "Criar etiqueta" abre o S4 por cima, preservando o formulário; ao voltar, a
  etiqueta recém-criada aparece selecionada (US-04 AC 9).
- Sem etiquetas cadastradas: linha "Sem etiqueta" selecionada + texto "Você
  ainda não tem etiquetas." (`bodyMedium`, `onSurfaceVariant`) + botão de texto
  "Criar etiqueta".
- Loading (abertura > 200ms): spinner `primary` 32dp + "Carregando etiquetas";
  erro ao carregar: banner `errorContainer` + "Tentar novamente".
- **Este é o único seletor de etiqueta da tarefa.** Não existem chips de seleção
  inline no S2.

## 3. Cores (somente tokens)

Referência normativa de valores: `COLOR_SCHEME.md` §4–§7.

| Elemento | Container | Conteúdo |
|:--|:--|:--|
| Fundo do app / telas | `colorScheme.surface` | `onSurface` |
| Top app bar (em repouso) | `surface` | título `onSurface`, ícones `onSurfaceVariant` |
| Top app bar (após rolagem) | `surfaceContainerLow` | idem |
| SearchField | `surfaceContainerHigh` | texto `onSurface`, placeholder/ícone `onSurfaceVariant`, cursor `primary` |
| TabRow | `surface` | selecionada `primary`, não selecionada `onSurfaceVariant`, indicador `primary` |
| SortRow (botão de ordenação) | `surface` | `onSurfaceVariant`; ícone `onSurfaceVariant`; até 2 linhas, sem corte |
| Linha de tarefa | `surface` | título `onSurface`, descrição `onSurfaceVariant` |
| Linha concluída | `surface` | título riscado `onSurfaceVariant`, marca `onSurfaceVariant` |
| Checkbox | não marcado: `outline`; marcado: `primary`/`onPrimary` | — |
| Chip de etiqueta | paleta fixa (`container` da etiqueta) | `onContainer` + `dot` |
| Linha "Etiqueta" do S2 (somente leitura) | `surfaceContainerLow` + borda `outlineVariant` | chip da etiqueta + chevron `onSurfaceVariant` |
| Chip de data (S2) | `surfaceContainerLow` + borda `outlineVariant` | `onSurface`; ícones `onSurfaceVariant` |
| Badge "Atrasada" | `errorContainer` | `onErrorContainer` |
| Badge "Hoje" | `tertiaryContainer` | `onTertiaryContainer` |
| Badge "Amanhã" | `secondaryContainer` | `onSecondaryContainer` |
| Badge "Até 7 dias" | `surfaceContainerHigh` + borda `outlineVariant` | `onSurfaceVariant` |
| Extended FAB | `primaryContainer` | `onPrimaryContainer` |
| Botão preenchido primário | `primary` | `onPrimary` |
| Botão tonal | `secondaryContainer` | `onSecondaryContainer` |
| Botão outlined / texto | transparente | `primary` |
| Ação destrutiva (dialog) | `error` (preenchido) ou transparente com `error` | `onError` / `error` |
| Text field (outlined) | transparente, borda `outline`; foco `primary`; erro `error` | texto `onSurface`, label `onSurfaceVariant` |
| Contador de caracteres | — | `onSurfaceVariant`; em erro de validação, `error` |
| Amostra de cor (S4) | cor da paleta (`dot`) | nome `onSurfaceVariant`; selecionada: anel `primary` + check `onSurface` sobre disco `surface` |
| Dialog / bottom sheet | `surfaceContainerHigh` (sheet: `surfaceContainerLow`) | `onSurface` |
| Scrim de modal | `scrim` (32% de alpha) | — |
| Snackbar | `inverseSurface` | `inverseOnSurface`; ação `inversePrimary` |
| Banner de erro inline | `errorContainer` | `onErrorContainer` |
| Divisor | `outlineVariant` | — |
| Foco visível | anel `primary` | — |

Regras:

- Nunca usar `primary` em corpo de texto longo, nem como fundo de FAB.
- Cor nunca é o único sinal: prazo tem texto + ícone; etiqueta tem nome; a
  amostra de cor do S4 tem nome e check.
- Estados de erro usam `error`/`errorContainer`; sucesso não tem cor própria —
  o conteúdo atualizado é o próprio feedback.
- Superfícies de modal sobem de nível tonal (`surfaceContainerHigh`), não por
  sombra pesada.

## 4. Tipografia (`MaterialTheme.typography.*`)

Família: padrão do projeto (Roboto). **Nenhum tamanho novo**; usar apenas tokens.

| Elemento | Token | Regras |
|:--|:--|:--|
| Título da top app bar | `titleLarge` | até **2 linhas**; a app bar cresce em altura; `Ellipsis` só como último recurso |
| Título da tarefa | `titleMedium` | `maxLines = 2`, `TextOverflow.Ellipsis` |
| Descrição da tarefa | `bodyMedium` | `maxLines = 2`, `Ellipsis`; omitida se vazia |
| Marca "Concluída em dd/MM/aaaa" | `labelMedium` | 1 linha; nunca truncada |
| Chip de etiqueta | `labelMedium` | 1 linha, `Ellipsis` no nome |
| Badge de prazo | `labelMedium` | 1 linha; nunca truncado |
| Label de aba | `titleSmall` | 1 linha |
| Botão de ordenação | `labelLarge` | **até 2 linhas**, altura cresce, sem corte; nome completo em `contentDescription` |
| Texto do campo (input) | `bodyLarge` | — |
| Label/placeholder do campo | `bodyLarge` / `bodyLarge` | label flutuante usa `bodySmall` |
| Texto de apoio e contador | `bodySmall` | contador alinhado ao fim |
| Título de estado vazio | `headlineSmall` | centralizado; cresce com a fonte (sem `maxLines`) |
| Eco da busca (texto buscado) | `bodyMedium` | `maxLines = 2` + `Ellipsis`; consulta completa no campo e no `contentDescription` (**exceção explícita**) |
| Corpo de estado vazio | `bodyMedium` | centralizado, largura máx. 280dp |
| Título de dialog | `headlineSmall` | — |
| Corpo de dialog | `bodyMedium` | — |
| Label de botão | `labelLarge` | — |
| Texto de snackbar | `bodyMedium` | — |
| Título de bottom sheet | `titleLarge` | — |
| Nome de etiqueta na lista (S3) | `titleMedium` | 1 linha, `Ellipsis` |
| Linha de opção do S7 / S5 | `bodyLarge` | até 2 linhas |
| Nome da cor na grade (S4) | `bodySmall` | 1 linha, centrado |

Regras:

- Suportar font scale até 200%: nada de altura fixa para texto; nada de
  `maxLines` em textos de ação, dialog, estado vazio (exceto o eco da busca) ou
  botão.
- O título da top app bar pode ocupar **2 linhas** e a app bar cresce em altura
  (mantendo `←` e "Salvar" visíveis); `Ellipsis` só entra quando nem 2 linhas
  couberem. Isso vale para "Nova tarefa"/"Editar tarefa" a 200%.
- Os 2 `maxLines` da linha são **visuais**; a semântica carrega o texto inteiro
  (§10).
- Não reduzir fonte para caber: truncar com reticências e manter a hierarquia.
- O eco da busca (§8.2) é a **única** exceção à regra de não truncar em estado
  vazio: ele usa `bodyMedium`, `maxLines = 2` e `Ellipsis`, com a consulta
  integral no campo de busca e no `contentDescription`. O estado vazio é
  rolável quando a fonte ampliada empurra o botão.

## 5. Shape (tokens de forma do design system)

`MaterialTheme.shapes` (Compose) expõe **apenas** `extraSmall`, `small`,
`medium`, `large` e `extraLarge`. Os tokens `none` e `full` são da escala visual
MD3, **não** são propriedades de `Shapes`: o projeto os define uma única vez no
design system (`RoundedCornerShape(0.dp)` e `RoundedCornerShape(percent = 50)`;
`CircleShape` na amostra de cor) e os expõe por um só ponto de definição.
Nenhum componente cria raio local.

| Token | Origem | Uso |
|:--|:--|:--|
| `none` | design system (`RoundedCornerShape(0.dp)`) | divisores, fundo de listas |
| `extraSmall` | `MaterialTheme.shapes.extraSmall` | text fields outlined, **linha "Etiqueta" do S2** |
| `small` | `MaterialTheme.shapes.small` | chip de etiqueta |
| `medium` | `MaterialTheme.shapes.medium` | cartões de estado vazio, banner de erro |
| `large` | `MaterialTheme.shapes.large` | Extended FAB, FAB |
| `extraLarge` | `MaterialTheme.shapes.extraLarge` | dialogs e topo de bottom sheets |
| `full` | design system (`RoundedCornerShape(percent = 50)` / `CircleShape`) | SearchField, badge de prazo (pílula), chips de data, botões, alvo circular de ícone, amostra circular de cor |

Regras:

- FAB de 56dp usa `large`; não usar `extraLarge` (viraria pílula).
- Chip de etiqueta usa `small` (8dp) para não competir com o badge de prazo em
  pílula (`full`).
- Text field usa `extraSmall`; não misturar filled e outlined na mesma tela.
- Chips de data usam `full` com altura **mínima** de 48dp (§9.3).
- A linha "Etiqueta" do S2 usa `extraSmall`, coerente com os text fields
  outlined da mesma tela.

## 6. Elevação e tonalidade

| Nível | Superfície | Uso |
|--:|:--|:--|
| 0 | `surface` | fundo de S1/S2/S3, top app bar em repouso |
| 1 | `surfaceContainerLow` | top app bar após rolagem, bottom sheet, linha "Etiqueta" do S2 |
| 2 | `surfaceContainer` | agrupamento de metadados (quando necessário) |
| 3 | `surfaceContainerHigh` | SearchField, dialog, badge "Até 7 dias" |
| 4 | `surfaceContainerHighest` | superfície mais alta (se necessário em sheet sobre dialog) |

- Sem sombras em linhas, campos, tabs, dialogs ou snackbars: separação por
  espaçamento e tom.
- Sombra permitida **apenas** no FAB (elevação 3). Dialogs e sheets usam
  elevação tonal (`surfaceContainerHigh`/`surfaceContainerLow`) + scrim (32%),
  sem sombra, conforme `material3-tokens.md` §5.
- Em dark, subir de nível tonal em vez de clarear com branco.

## 7. Motion

Sem bounce/spring gratuito. `spring` só se justificar movimento físico; neste
MVP, **tudo é `tween`** com `FastOutSlowInEasing` (`standard`) ou
`LinearOutSlowInEasing` (`standardDecelerate`).

| Interação | Duração | Easing |
|:--|--:|:--|
| State layer (hover/press) | `short2` (100ms) | `standard` |
| Foco visível | `short2` (100ms) | `standard` |
| Entrada de item na lista (criação) | `medium1` (250ms) | `standardDecelerate` (fade + altura) |
| Saída de item da lista (exclusão/conclusão) | `short4` (200ms) fade + `medium1` reflow | `standardAccelerate` |
| Troca de aba | `medium1` (250ms) | `standard` |
| Abertura de bottom sheet | `medium2` (300ms) | `emphasizedDecelerate` |
| Entrada/saída de dialog | `medium2` (300ms) | `emphasizedDecelerate` / `emphasizedAccelerate` |
| Snackbar entrar/sair | `medium1` (250ms) / `short4` (200ms) | `standardDecelerate` / `standardAccelerate` |
| Navegação S1 → S2/S3 | `long1` (450ms) | `emphasized` |
| FAB aparecer/ocultar (mudança de estado vazio) | `short4` (200ms) | `standardDecelerate` / `standardAccelerate` |
| Checkbox marcar/desmarcar | `short3` (150ms) | `standard` |

Regras:

- O indicador de prazo **não** anima cor: atualiza na recomposição (PRD US-05
  AC 5). Animação de cor sugeriria passagem de tempo em tempo real.
- Não animar a lista inteira ao mudar ordenação; apenas reposicionar (se houver
  `animateItemPlacement`) em `medium1`.
- Respeitar "remover animações" do sistema (duração 0 quando o usuário desativa
  animações).

## 8. Estados por componente

### 8.1 Tabela de estados

| Componente | Rest | Hover | Focus | Pressed | Disabled | Loading | Empty | Error | Success |
|:--|:--|:--|:--|:--|:--|:--|:--|:--|:--|
| Linha de tarefa | `surface`; texto conforme §3 | state layer 8% `onSurface` | anel `primary` 2dp + layer 10% | layer 12% | — | — | ver 8.2 | — | tarefa aparece na lista |
| Checkbox | borda `outline` 2dp | layer 8% | anel `primary` | layer 12% | conteúdo 38%, container 12% | — | — | — | marcado com check `onPrimary` sobre `primary` |
| Extended FAB | `primaryContainer` | layer 8% `onPrimaryContainer` | anel `primary` | layer 12% | conteúdo 38%, container 12% | — | oculto em empty com CTA (§9.2) | — | — |
| Botão preenchido | `primary` | layer 8% `onPrimary` | anel `primary` | layer 12% | conteúdo 38%, container 12% | spinner 18dp `onPrimary` + label "Salvando…" | — | — | — |
| Botão texto/outlined | transparente | layer 8% `primary` | anel `primary` | layer 12% | conteúdo 38% | — | — | — | — |
| Botão de ordenação | `onSurfaceVariant` | layer 8% | anel `primary` | layer 12% | — | — | — | — | até 2 linhas, sem corte |
| SearchField | `surfaceContainerHigh` | layer 8% | borda `primary` 2dp | — | conteúdo 38% | — | placeholder visível | — | — |
| TextField | borda `outline` | layer 8% | borda `primary` 2dp, label `primary` | — | conteúdo 38% | — | placeholder | borda `error` + texto de apoio `error` | borda `outline`, contador `onSurfaceVariant` |
| Contador | `onSurfaceVariant` | — | — | — | — | — | "0/120", "0/2000" | `error` apenas em erro de validação | valor aceito; limite atingido não é erro |
| Chip de etiqueta | container da etiqueta | layer 8% `onContainer` | anel `primary` | layer 12% | conteúdo 38% | — | — | — | reflete renomear/trocar cor |
| Linha "Etiqueta" (S2, somente leitura) | `surfaceContainerLow`, borda `outlineVariant` | layer 8% | anel `primary` | layer 12% | conteúdo 38% | — | "Sem etiqueta" | — | chip atualizado ao voltar do S7 |
| Chip de data (S2) | `surfaceContainerLow`, borda `outlineVariant`, 48dp mínimo | layer 8% | anel `primary` | layer 12% | — | — | "Definir data" | — | data aplicada |
| Amostra de cor (S4) | círculo + nome `onSurfaceVariant` | layer 8% | anel `primary` 2dp + layer 10% | layer 12% | conteúdo 38% | — | — | — | selecionada: anel + check |
| Badge de prazo | §3 | — | — | — | — | — | ausente | "Atrasada" | — |
| Aba | `onSurfaceVariant` | layer 8% | anel `primary` | layer 12% | — | — | — | — | selecionada `primary` + indicador |
| Menu item | `surfaceContainer` | layer 8% | anel `primary` | layer 12% | conteúdo 38% | — | — | — | — |
| Dialog | `surfaceContainerHigh` | — | — | — | botões disabled 38% | — | — | ação destrutiva `error` | — |
| Bottom sheet | `surfaceContainerLow` | — | — | — | — | spinner 32dp + label | ver 8.2 | banner `errorContainer` + "Tentar novamente" | — |
| Snackbar | `inverseSurface` | — | — | — | — | — | — | ação "Tentar novamente" | — |

Implementação dos state layers: `MutableInteractionSource` +
`collectIsHoveredAsState` / `collectIsFocusedAsState` /
`collectIsPressedAsState`, opacidades do `COLOR_SCHEME.md` §9. Nada de debounce
por relógio.

### 8.2 Estados de tela

**Regra de chrome (M3):** em S1, **todos** os estados mantêm app bar,
SearchField, abas e SortRow; apenas o corpo central muda. Em S3, app bar e FAB
(quando o estado não tiver CTA própria) permanecem.

| Estado | Onde | Regra |
|:--|:--|:--|
| **Loading inicial** | S1 | Banco local responde rápido; só exibir indicador se a leitura exceder **200ms**: `CircularProgressIndicator` em `primary` (48dp) centralizado + label "Carregando tarefas" (`bodyMedium`, `onSurfaceVariant`). Sem skeleton animado. Chrome de S1 visível. |
| **Loading lento** | S3 / S7 | Mesma regra dos 200ms: spinner `primary` 32dp centralizado + "Carregando etiquetas" (`bodyMedium`, `onSurfaceVariant`). |
| **Loading de salvar** | S2 / S4 | Botão "Salvar" vira spinner 18dp + "Salvando…", desabilitado; campos somente leitura durante a operação (não perdem conteúdo). |
| **Empty — lista ativa** | S1 (US-01 AC 12) | Ícone 32dp em círculo 96dp `surfaceContainerHigh`/`onSurfaceVariant`; título "Nenhuma tarefa por aqui"; corpo "Crie sua primeira tarefa para começar."; botão preenchido "Nova tarefa". **FAB oculto** neste estado. |
| **Empty — Concluídas** | S1 (US-03 AC 7) | Ícone de check; título "Nada concluído ainda"; corpo "As tarefas que você concluir aparecem aqui."; **sem** botão de ação. Chrome completo (busca e ordenação visíveis). FAB de "Nova tarefa" permanece (não há CTA concorrente). |
| **Empty — busca** | S1 (US-06 AC 6) | Ícone de busca; título "Nenhum resultado" (`headlineSmall`); **eco da consulta** "Sua busca: “%1$s”" em `bodyMedium`, `maxLines = 2` + `Ellipsis`; corpo "Tente outra palavra ou limpe a busca."; botão tonal "Limpar busca". Corpo rolável se necessário. Prioridade sobre o empty de lista. Consulta completa no campo e no `contentDescription`. |
| **Empty — Etiquetas** | S3 (US-04 AC 8) | Ícone de etiqueta; título "Nenhuma etiqueta"; corpo "Crie etiquetas para organizar suas tarefas por contexto."; botão preenchido "Criar etiqueta". **FAB oculto** neste estado. |
| **Seletor sem etiquetas** | S7 (US-04 AC 9) | Linha "Sem etiqueta" selecionada + "Você ainda não tem etiquetas." + botão texto "Criar etiqueta". |
| **Erro de armazenamento** | S1/S3 (US-01 AC 13) | Snackbar `inverseSurface`: "Não foi possível salvar. Tente novamente." + ação "Tentar novamente". Se ocorrer em S2, **banner inline** `errorContainer` acima do conteúdo + conteúdo digitado preservado. |
| **Erro ao carregar etiquetas** | S3/S7 | Banner `errorContainer` no corpo ("Não foi possível carregar as etiquetas.") + botão texto "Tentar novamente"; chrome da tela/sheet mantido. |
| **Erro de validação** | S2/S4 | Erro inline no campo (`error` + texto de apoio), campo mantém o foco; formulário/sheet não fecha e o conteúdo é preservado. |
| **Sucesso** | S1/S3 | Sem toast/snackbar de sucesso: a própria lista/chip atualizada é o feedback (≤150ms). |

**Rolagem dos estados vazios (M1):** os empty states de S1 (Ativas, Concluídas),
o de busca e o de S3 ficam em `Column` centralizada com `verticalScroll` quando o
conteúdo (halo + título + corpo + ação) exceder a viewport. A ação primária do
empty ("Nova tarefa" / "Criar etiqueta" / "Limpar busca") nunca fica inalcançável
a 200%; se necessário, o halo encolhe antes de qualquer texto ser cortado. O
chrome de S1 (app bar, busca, abas, ordenação) não rola — só o corpo central.

### 8.3 Regras específicas da linha

- Título: 2 linhas + ellipsis. Descrição: 2 linhas + ellipsis; se vazia, não é
  composta (sem espaço reservado).
- Metadados: `FlowRow` com 4dp de espaçamento; se não couber em 1 linha, quebra
  para a linha seguinte sem empurrar o trailing.
- Chip de etiqueta: largura máxima = largura da coluna de texto − 48dp (área do
  menu). Nome com ellipsis; `contentDescription` com nome completo.
- Tarefa concluída: título `onSurfaceVariant` + `TextDecoration.LineThrough`;
  marca "Concluída em dd/MM/aaaa" em `labelMedium`; badge de prazo não aparece.
- Trailing: `IconButton` 48dp "Mais opções" → menu com "Editar" e "Excluir".
  O clique na linha também abre a edição (US-01 AC 6).
- **Exatamente uma** badge de prazo por tarefa (PRD US-05 AC 3).

### 8.4 Preservação de estado

- S1 mantém **busca, aba e ordenação** ao voltar de S2/S3 e após abrir/fechar
  S4/S5/S7 (§1.2).
- **Troca de aba com busca ativa:** a consulta é **preservada** e reaplicada à
  nova aba (o filtro continua escopado à aba visível). Se não houver resultado,
  aparece o empty de busca com o texto; "Limpar busca" restaura a lista completa
  da aba atual (US-06 AC 4/7). Trocar de aba não limpa a consulta.
- S2 mantém o conteúdo digitado ao abrir S4 (atalho de etiqueta) ou S7 e ao
  voltar deles; o conteúdo também é preservado em erro de validação e de
  armazenamento.
- S4 preserva o nome digitado em erro de duplicata e não fecha o sheet.

## 9. Componentes e variantes

### 9.1 Botões

| Variante | Uso | Altura / touch | Shape |
|:--|:--|:--|:--|
| Filled | ação principal em empty state, "Salvar" do S4, confirmações | 40dp / 48dp | `full` |
| Filled destrutivo (`error`) | confirmar exclusão | 40dp / 48dp | `full` |
| Tonal | "Limpar busca", ações secundárias em empty state | 40dp / 48dp | `full` |
| Outlined | ação alternativa em dialog ("Cancelar" quando houver destrutivo preenchido) | 40dp / 48dp | `full` |
| Text | "Salvar" na app bar de S2, "Cancelar" em dialogs/sheets, ações de menu | 40dp / 48dp | `full` |

- Label `labelLarge`; 1 linha; nunca truncado.
- Máximo de **duas** ações por dialog: uma destrutiva/confirmatória e uma de
  cancelamento.
- S2 **não** tem ações de rodapé e **não** tem ação destrutiva (§2.3).

### 9.2 FAB

- **Extended FAB** "Nova tarefa" / "Criar etiqueta": altura 56dp, ícone 24dp +
  `labelLarge`, container `primaryContainer`, conteúdo `onPrimaryContainer`,
  shape `large`, elevação 3.
- Posição: 16dp das bordas direita e inferior, acima do inset de navegação.
- Lista recebe `contentPadding` inferior de 88dp para o FAB não cobrir a última
  linha.
- **Oculto** no empty state de lista ativa e no empty state de etiquetas, que já
  têm botão primário com a mesma ação (evita CTA duplicada). Nos demais estados
  permanece visível.
- Não colapsar em FAB pequeno no scroll (evita estado extra sem requisito).

### 9.3 Chips e badges

- **Chip de etiqueta (linha da tarefa):** `small`, **altura mínima** 32dp com
  padding vertical de 4dp, container da paleta, dot 8dp opcional + nome
  `labelMedium`; sem ação (informativo). Cresce com font scale (nunca recorta).
- **Badge de prazo:** `full`, **altura mínima** 24dp com padding vertical de 3dp,
  texto + ícone 14dp; cresce com font scale.
- **Chip de data (S2):** `full`, **altura mínima** 48dp (cresce com a fonte),
  container `surfaceContainerLow`, borda `outlineVariant`, ícone de calendário
  20dp, texto dd/MM/aaaa `labelLarge` e ação de remover como `IconButton`
  **48×48dp** com ícone de 20dp e `contentDescription` "Remover data limite".
  **Um único chip por estado:** com data → chip com a data e `✕`; sem data →
  chip "Definir data" (outlined, mínimo 48dp). Nunca os dois simultaneamente.
- **Não há chips de seleção de etiqueta** no S2: a seleção acontece no S7 com
  `RadioButton` (§2.6).
- Não usar chip para ordenação (é um botão de texto com ícone + nome atual).

### 9.4 Text fields

- **Outlined**, shape `extraSmall`, altura mínima 56dp; descrição com 4 linhas
  visíveis (`minLines = 4`) que **cresce até `maxLines = 6` sem rolagem
  interna**; acima disso quem rola é a página do S2 (§2.3).
- Label sempre visível (floating), placeholder apenas quando vazio.
- `supportingText` sempre presente em duas partes na mesma linha: texto de apoio
  (ou mensagem de erro) à esquerda e contador `atual/máximo` à direita
  (`bodySmall`). O contador **não** é substituído pelo erro e nunca some.
- **Limite atingido (N8):** quando `atual == máximo`, o texto de apoio passa a
  "Limite atingido" nos **três** campos (título, descrição e nome de etiqueta);
  o contador continua `bodySmall` em `onSurfaceVariant` e **nunca** vai para
  `error`. Ao apagar um caractere, o apoio volta ao texto padrão do campo. Erro
  de validação tem precedência sobre "Limite atingido".
- Título: `singleLine = true`, limite 120, apoio "Obrigatório" em repouso.
- Descrição: limite 2000, label "Descrição (opcional)".
- Nome da etiqueta (S4): `singleLine = true`, limite 30, apoio "Máximo de 30
  caracteres" em repouso.
- Erro: borda + label + texto de apoio em `error`; campo recebe foco.
- Marcação de obrigatório é **texto** no `supportingText`, nunca `*` (não é
  anunciado por leitor de tela).

### 9.5 Dialogs e sheets

- **Dialog de exclusão:** `extraLarge`, largura máx. 312dp, título
  `headlineSmall`, corpo `bodyMedium` ("Esta ação não pode ser desfeita."),
  ações "Cancelar" (texto) e "Excluir" (filled `error`). Toque fora não confirma;
  botão voltar cancela. Sem sombra; apenas scrim 32%.
- **Bottom sheet (S4, S5, S7):** `extraLarge` no topo, drag handle visível,
  altura máxima 90% da tela com rolagem. Título `titleLarge`; ações no rodapé do
  sheet quando aplicável. Sem sombra.
- **Comportamento dos seletores (S5 e S7) — N6:** tocar em uma opção **aplica e
  fecha** o sheet imediatamente (sem botão de confirmar). Drag handle, toque no
  scrim e botão voltar do sistema **fecham sem alterar** a seleção vigente. Ao
  fechar, o foco retorna ao controle que abriu o sheet (S5 → botão de ordenação;
  S7 → linha "Etiqueta"). No S5 a lista por trás reflete a nova ordem e a busca
  ativa é preservada; no S7 o conteúdo do S2 é preservado.
- **Date picker (S8):** `DatePickerDialog` do Material 3, locale pt-BR,
  `showModeToggle` visível (calendário/entrada manual). Sem hora. Botões
  `cancel` ("Cancelar")/`ok` ("OK"). Datas passadas permitidas (US-05 AC 7).

### 9.6 Navegação e abas

- Sem `NavigationBar`. S1 é raiz; S2/S3 são empilhadas com back.
- Top app bar de S2/S3: `←` (voltar, 48dp) à esquerda + título; em S2 há "Salvar"
  como ação de texto. **Não existe `✕` na top app bar** de S2/S3.
- Abas primárias (S1): altura 48dp, labels `titleSmall`, indicador 3dp `primary`
  na largura total da aba; toque mínimo 48dp; seleção persiste.
- Botão voltar do sistema: S4/S5/S7/S8 fecham o modal; S2/S3 voltam para S1.

### 9.7 Menu e snackbar

- Menu de linha: `DropdownMenu` ancorado no `IconButton`, itens 48dp, ícone
  opcional + label, divisor `outlineVariant` antes de "Excluir",
  "Excluir" em `error`. Rótulos vindos de recursos: `menu_edit` ("Editar") e
  `delete` ("Excluir"), com paridade entre `DESIGN_SPEC`, `strings.xml` e
  preview.
- Snackbar: curto (máx. 2 linhas), ação "Tentar novamente" em `inversePrimary`;
  ancorado acima do FAB. Não empilhar; um por vez. Sem sombra.

## 10. Acessibilidade

- **Touch target ≥ 48×48dp** em checkbox, menu, voltar, fechar, **limpar busca**,
  ações de campo, chips interativos, **amostras de cor do S4** e **ação de
  remover data**. O "limpar busca" usa `Modifier.size(48.dp)` (ou
  `minimumInteractiveComponentSize()`) com ícone de 20dp dentro do SearchField de
  56dp; nunca 40dp.
- **Semântica da linha:** checkbox é nó separado (`Role.Checkbox`,
  `stateDescription` = "Concluída"/"Não concluída", `onClickLabel` = "Marcar
  como concluída"/"Reabrir tarefa"). A coluna de texto é clicável
  (`onClickLabel` = "Editar tarefa") com `mergeDescendants = true` e
  `contentDescription` composto pelo texto **completo**:
  título + descrição + "Etiqueta <nome>" + estado de prazo + (se concluída)
  "Concluída em dd/MM/aaaa". Isso garante que o truncamento visual não perde
  informação (PRD §6).
- **Content descriptions obrigatórias:** "Mais opções da tarefa", "Mais opções
  da etiqueta", "Limpar busca", "Remover data limite", "Etiquetas", "Voltar",
  "Alternar para a aba Ativas", "Alternar para a aba Concluídas",
  "Ordenação atual: <nome>". A etiqueta truncada anuncia o nome completo.
- **Eco da busca:** o texto exibido pode ser truncado em 2 linhas, mas o campo
  de busca contém a consulta completa e o estado vazio anuncia a consulta
  integral no `contentDescription`.
- **Seletor de cor (S4):** cada amostra tem `contentDescription` com o nome da
  cor ("Vermelho"), `role = RadioButton` e `selected`; a seleção é indicada por
  anel + check, não só por cor.
- **Contraste:** pares definidos no `COLOR_SCHEME.md` §8; texto 4,5:1, ícones e
  bordas 3:1.
- **RTL:** usar `start`/`end` e `Alignment.Start/End`; não espelhar ícones
  direcionais que não representem direção (lixeira, calendário, etiqueta).
- **Font scale até 200%:** sem alturas fixas para texto; metadados em `FlowRow`;
  ações e estados de prazo nunca truncados; chip de etiqueta e badge de prazo têm
  altura **mínima** (32dp/24dp) e crescem; título/descrição mantêm 2 linhas
  visuais e o restante vai para o TalkBack; **o botão de ordenação quebra em até
  2 linhas** e cresce (nunca `overflow: hidden`).
- **Rolagem a 200% (M1):** corpo de S2 rolável com a top app bar fixa (§2.3) e
  empty states de S1/S3/busca roláveis (§8.2). A ação primária de cada tela fica
  sempre alcançável; o campo descrição não tem rolagem interna.
- **Título da app bar** ocupa até 2 linhas e a barra cresce; `Ellipsis` é o
  último recurso (nunca corta antes de tentar a segunda linha).
- **Foco visível:** anel `primary` 2dp em teclado/switch, com offset de 2dp;
  ordem de foco segue a leitura (app bar → busca → abas → ordenação → lista →
  FAB).
- **Sem dependência de cor:** todo estado tem texto e/ou ícone (prazo, etiqueta,
  erro, seleção).
- **Anúncio de mudanças:** marcar/desmarcar anuncia o novo estado via
  `stateDescription`; exclusão mantém foco no item vizinho; snackbar de erro é
  `liveRegion = Polite`.

## 11. Conteúdo, copy e strings

Todos os textos em recursos (pt-BR). **Nenhuma string hardcoded**; nenhum texto
com dado pessoal em log/analytics. Placeholders e formatos:

| Chave sugerida | Texto pt-BR |
|:--|:--|
| `app_name` | Lista de tarefas |
| `task_list_title` | Tarefas |
| `tab_active` | Ativas |
| `tab_completed` | Concluídas |
| `search_placeholder` | Buscar por título ou descrição |
| `search_clear` | Limpar busca |
| `sort_label` | Ordenação: %1$s |
| `sort_sheet_title` | Ordenar por |
| `sort_title_az` | Título A-Z |
| `sort_title_za` | Título Z-A |
| `sort_created_desc` | Criação (mais recente) |
| `sort_created_asc` | Criação (mais antiga) |
| `sort_deadline_asc` | Prazo (mais próximo) |
| `sort_deadline_desc` | Prazo (mais distante) |
| `a11y_sort_current` | Ordenação atual: %1$s |
| `new_task` | Nova tarefa |
| `edit_task` | Editar tarefa |
| `save` | Salvar |
| `saving` | Salvando… |
| `cancel` | Cancelar |
| `ok` | OK |
| `delete` | Excluir |
| `menu_edit` | Editar |
| `delete_task_title` | Excluir tarefa? |
| `delete_task_body` | Esta ação não pode ser desfeita. |
| `task_title_label` | Título |
| `field_required` | Obrigatório |
| `task_title_required` | Informe um título |
| `task_description_label_optional` | Descrição (opcional) |
| `counter_format` | %1$d/%2$d |
| `support_max_chars` | Máximo de %1$d caracteres |
| `support_limit_reached` | Limite atingido |
| `label_section` | Etiqueta |
| `label_none` | Sem etiqueta |
| `label_create_shortcut` | Criar etiqueta |
| `deadline_section` | Data limite |
| `deadline_action_set` | Definir data |
| `deadline_remove` | Remover data limite |
| `deadline_overdue` | Atrasada |
| `deadline_today` | Hoje |
| `deadline_tomorrow` | Amanhã |
| `deadline_soon` | Até 7 dias |
| `deadline_a11y_today` | Vence hoje |
| `deadline_a11y_tomorrow` | Vence amanhã |
| `deadline_a11y_soon` | Vence em até 7 dias |
| `deadline_a11y_overdue` | Atrasada, venceu em %1$s |
| `completed_on` | Concluída em %1$s |
| `completed_on_a11y` | Concluída em %1$s |
| `empty_active_title` | Nenhuma tarefa por aqui |
| `empty_active_body` | Crie sua primeira tarefa para começar. |
| `empty_completed_title` | Nada concluído ainda |
| `empty_completed_body` | As tarefas que você concluir aparecem aqui. |
| `empty_search_title` | Nenhum resultado |
| `empty_search_echo` | Sua busca: “%1$s” |
| `empty_search_body` | Tente outra palavra ou limpe a busca. |
| `storage_error_message` | Não foi possível salvar. Tente novamente. |
| `storage_error_inline` | Não foi possível salvar. Seu texto foi mantido. |
| `storage_delete_error_message` | Não foi possível excluir. Tente novamente. |
| `labels_load_error` | Não foi possível carregar as etiquetas. |
| `retry` | Tentar novamente |
| `labels_title` | Etiquetas |
| `label_new` | Criar etiqueta |
| `label_edit` | Editar etiqueta |
| `label_name_label` | Nome |
| `label_name_required` | Informe um nome |
| `label_name_duplicate` | Já existe uma etiqueta com esse nome |
| `label_color_section` | Cor |
| `label_delete_title` | Excluir etiqueta? |
| `label_delete_body` | As tarefas que usam esta etiqueta ficarão sem etiqueta. |
| `empty_labels_title` | Nenhuma etiqueta |
| `empty_labels_body` | Crie etiquetas para organizar suas tarefas por contexto. |
| `selector_empty_labels` | Você ainda não tem etiquetas. |
| `a11y_more_options` | Mais opções da tarefa |
| `a11y_more_options_label` | Mais opções da etiqueta |
| `a11y_loading_tasks` | Carregando tarefas |
| `a11y_loading_labels` | Carregando etiquetas |
| `a11y_back` | Voltar |
| `a11y_switch_tab_active` | Alternar para a aba Ativas |
| `a11y_switch_tab_completed` | Alternar para a aba Concluídas |
| `a11y_checkbox_completed` | Concluída |
| `a11y_checkbox_not_completed` | Não concluída |
| `a11y_mark_completed` | Marcar como concluída |
| `a11y_reopen` | Reabrir tarefa |
| `a11y_edit_task` | Editar tarefa |
| `a11y_task` | %1$s. %2$s |
| `a11y_label` | Etiqueta %1$s |
| `a11y_label_color` | %1$s, cor da etiqueta |
| `a11y_selected` | Selecionado |
| `color_name_red` / `_orange` / `_mustard` / `_green` / `_teal` / `_blue` / `_purple` / `_pink` | Vermelho / Laranja / Mostarda / Verde / Verde-água / Azul / Roxo / Rosa |

Regras de tom: instruções curtas, voz ativa, sem jargão técnico (não citar
"banco", "SQLite", "storage"). Datas sempre dd/MM/aaaa. Aspas tipográficas nos
textos que citam a busca.

## 12. Do's and don'ts

**Do's**

- Use `titleMedium` para título de tarefa e `bodyMedium` para descrição, com 2
  linhas e ellipsis.
- Mantenha o espaçamento vertical de 16dp entre campos no formulário.
- Deixe o estado de prazo com **texto + ícone** em todos os casos.
- Nomeie as cores de etiqueta com o nome exibido e pré-selecione "Vermelho".
- Anuncie o texto completo no TalkBack mesmo com truncamento visual.
- Preserve o conteúdo digitado em erro de validação e de armazenamento.
- Use `start`/`end` para margens e alinhamento (RTL).
- Deixe a lista com `contentPadding` inferior para o FAB.
- Deixe o botão de ordenação quebrar em até 2 linhas quando a fonte crescer.
- Use uma única CTA primária por estado (FAB **ou** botão do empty, não os dois).
- Selecione etiqueta **somente** pelo S7 (linhas com radio).
- Deixe o corpo do S2 e os empty states roláveis quando a fonte a 200% exceder a
  viewport.
- Use **altura mínima** em chip de etiqueta, badge de prazo e chip de data.
- Defina `none`/`full` uma única vez no design system e reutilize.

**Don'ts**

- Não usar hex, `Color(0x…)` ou nomes de cor (ex.: "Purple") em componentes; use
  papéis do color scheme.
- Não usar `primary` como fundo de FAB, chip de etiqueta ou corpo de texto.
- Não criar terceira cor de prazo fora dos quatro estados + ausência.
- Não truncar ações, datas, contadores ou estados de prazo.
- Não usar `maxLines` em textos de dialog, estado vazio ou botão — a **única**
  exceção é o eco da consulta no empty de busca (§4).
- Não usar `overflow: hidden` no botão de ordenação nem em qualquer ação.
- Não usar sombra em linhas, campos, tabs, dialogs ou snackbars (só o FAB).
- Não animar mudanças de cor do indicador de prazo.
- Não usar vermelho para "Hoje" (reservado a "Atrasada"/erro).
- Não exibir contagem de itens não requisitada, nem confete/gamificação.
- Não usar imagens externas, fontes customizadas ou assets de rede.
- Não implementar seletor de tema nem dynamic color.
- Não criar ações de rodapé no formulário (S2) nem exclusão dentro dele.
- Não criar chips de seleção de etiqueta inline no formulário (só o S7).
- Não fixar altura (em dp) em chip ou badge de texto: use mínimo + padding.
- Não criar raio local (`RoundedCornerShape(50)`, `CircleShape` avulso) fora do
  ponto único de definição dos tokens `none`/`full`.
- Não usar alvo de 40dp no limpar busca nem em ação de campo.

## 13. Mapeamento para Compose

### 13.1 Tokens

| Necessidade | API |
|:--|:--|
| Todas as cores | `MaterialTheme.colorScheme.<role>` (`primary`, `onPrimary`, `primaryContainer`, `secondaryContainer`, `tertiaryContainer`, `errorContainer`, `surface`, `surfaceContainerHigh`, `outlineVariant`, `inverseSurface`, …) |
| Tipografia | `MaterialTheme.typography.titleLarge/titleMedium/bodyMedium/labelLarge/labelMedium/bodySmall/headlineSmall` |
| Formas (cantos) | `MaterialTheme.shapes.extraSmall/small/medium/large/extraLarge` (a API **não** tem `none` nem `full`) |
| Formas `none` e `full` | Tokens do design system, definidos em um único ponto: `RoundedCornerShape(0.dp)` e `RoundedCornerShape(percent = 50)` (`CircleShape` na amostra de cor) |
| Paleta de etiquetas | `CompositionLocal` do design system (`LocalLabelPalette`), expondo `container`, `onContainer`, `dot` e o nome localizado por índice; fora do `ColorScheme` do Material |
| State layers | `MutableInteractionSource` + `collectIsPressedAsState/Hovered/Focused`; opacidades do `COLOR_SCHEME.md` §9 |

### 13.2 Componentes M3

| Elemento | Composable |
|:--|:--|
| Estrutura de tela | `Scaffold` (`topBar`, `floatingActionButton`, `snackbarHost`, `contentWindowInsets`) |
| App bar S1 | `TopAppBar` (small) com `actions` |
| App bar S2/S3 | `TopAppBar` com `navigationIcon` e `actions` ("Salvar") |
| Abas | `TabRow` + `Tab` (indicador `primary`) |
| Linha | `Row` + `Checkbox` + `Column` + `IconButton` + `DropdownMenu` dentro de `LazyColumn` (`items(..., key = id)`) |
| Chip de etiqueta | `Surface` (`small`, cores da paleta) + `Row` com `dot` e nome |
| Linha "Etiqueta" (S2) | `Surface` clicável shape `extraSmall` (`surfaceContainerLow`, borda `outlineVariant`) que abre o S7 |
| Chip de data | `Surface` shape `full` do design system (`RoundedCornerShape(percent = 50)`), altura mínima 48dp + `IconButton` 48dp para remover |
| Grade de cores (S4) | 4 colunas de células `Box` com `selectable(role = RadioButton)`, `minimumInteractiveComponentSize()` e anel `primary` |
| Badge de prazo | `Surface` shape `full` do design system + `Icon` + `Text`, altura mínima 24dp, cores por estado |
| Busca | `OutlinedTextField` com shape `full` do design system e container `surfaceContainerHigh` (sem `SearchBar` expandido; estado sempre compacto); limpar busca com alvo de 48dp |
| Campos do formulário | `OutlinedTextField` (`supportingText`, `isError`, `singleLine`, `minLines`) |
| Ordenação | `TextButton` (label com até 2 linhas) + `ModalBottomSheet` + `RadioButton` |
| Seletor de etiqueta (S7) | `ModalBottomSheet` + linhas com `RadioButton` + ação "Criar etiqueta" |
| Seletor de data | `DatePickerDialog` + `DatePicker` |
| Exclusão | `AlertDialog` |
| Estados vazios | `Column` centralizada com `Icon`, `Text` e `Button` |
| Snackbar de erro | `SnackbarHost` + `Snackbar` com `actionLabel` |
| FAB | `ExtendedFloatingActionButton` |
| Semântica | `Modifier.semantics(mergeDescendants = true) { contentDescription = … }`, `stateDescription`, `onClickLabel`, `liveRegion` |

### 13.3 Contratos de composição

- `TaskRow(task, label, deadlineState, onToggle, onClick, onDelete)` — recebe o
  estado de prazo **já calculado** (cálculo é do domínio; a UI não lê relógio).
- `DeadlineBadge(state)` — estados `Overdue`, `Today`, `Tomorrow`, `Soon`,
  `None`; `None` não compõe nada.
- `LabelChip(label)` — container/onContainer/dot da paleta; `null` não compõe
  nada (nenhum espaço reservado).
- `EmptyState(icon, title, body, action?)` — ação opcional (Concluídas vazia não
  tem ação).
- `SearchField(query, onQueryChange, onClear)` — clear visível só com consulta
  não vazia.
- `Counter(current, max)` — usa **`bodySmall`**, alinhado ao fim.
- `SortControl(label, onClick)` — até 2 linhas, `contentDescription` com o nome
  completo da ordenação.
- `LabelSelectorRow(current, onClick)` — somente leitura, shape `extraSmall`,
  altura mínima 56dp, abre o S7; nunca renderiza chips de opção.
- `LabelColorGrid(selectedIndex, onSelect)` — 8 amostras, alvo 48dp,
  `role = RadioButton`.

## 14. Riscos e limitações de design

| Risco | Impacto | Mitigação |
|:--|:--|:--|
| Tema atual do projeto é template com dynamic color | Cores imprevisíveis e conflito com a paleta fixa | `COLOR_SCHEME.md` §1 e §10: scheme completo e `dynamicColor = false`; dev ajusta `Theme.kt` |
| Truncamento visual × TalkBack | Perda de informação para leitor de tela | `contentDescription` composto com texto integral + teste instrumentado de semântica |
| Fonte a 200% | Metadados estouram a linha | `FlowRow` nos metadados, sem altura fixa, chip com largura máxima |
| Paleta fixa no escuro | Contraste insuficiente em alguma cor | Pares medidos em `COLOR_SCHEME.md` §8: `onContainer` ≥ 7,0:1 e `dot` ≥ 5,5:1 no escuro |
| Sem timer de meia-noite (PRD) | Badge pode ficar "Hoje" até a primeira interação após a virada | Estado é recalculado na recomposição; a UI não anima cor nem promete tempo real |
| "Até 7 dias" com tom neutro | Pode parecer desabilitado | Texto + ícone + borda `outlineVariant`; nunca só cor |
| Ícones necessários (etiqueta, relógio, calendário) | Dependência de `material-icons-extended` | Architect/Dev decidem o artefato; se não houver, usar apenas ícones do core e texto |
| FAB sobre a última linha | Item inacessível | `contentPadding` inferior de 88dp |
| Ordenação a 200% | Nome da ordenação cortado | Até 2 linhas, altura cresce, `contentDescription` completo |
| Consulta de busca muito longa | Empty state estoura a tela | Eco com `maxLines = 2` + ellipsis, CD completo e corpo rolável |
| Dois alvos primários no empty | Ruído de hierarquia/TalkBack | FAB oculto quando o empty tem CTA própria |
| Alvo de 48dp no chip de data | Alvo não cabe em 40dp | Chip de data com altura mínima 48dp e remover como `IconButton` 48dp |
| Entrada incremental por história | Telas incompletas nas primeiras histórias (sem abas/busca/etiqueta/prazo) | Cada seção indica a história; componentes aparecem apenas quando a história dona entra (PRD §4) |
| Date picker e locale | Formato fora de pt-BR | `Locale("pt","BR")` no `DatePicker`/formatter; testes de data |
| Contraste apenas documentado | Teste automatizado usar limite errado | `COLOR_SCHEME.md` §8 publica os valores medidos e proíbe limites genéricos |
| S2 a 200% sem rolagem | Campos de etiqueta/data inalcançáveis | Corpo rolável com app bar fixa (§2.3) e empty states roláveis (§8.2) |
| `full`/`none` fora de `Shapes` | Cada tela cria seu próprio pill | Tokens definidos em um único ponto do design system (§5, §13.1) |

## 15. Referência ao preview

`PREVIEW.html` (autocontido, CSS inline, sem rede/assets externos) mostra
exatamente:

- S1 nas abas Ativas e Concluídas (claro), com busca, abas, ordenação, chips de
  etiqueta, badges de prazo e FAB;
- empty states de S1 (lista ativa, concluídas, busca sem resultado) **com o
  chrome completo** (app bar, busca, abas e ordenação), e o empty de etiquetas;
- S2 (claro e escuro) com contadores, "Obrigatório", erro inline, linha de
  etiqueta somente leitura, chip de data único (48dp mínimo) e banner de erro;
- S3 (lista e vazio);
- S4 (sheet com nome, contador 30 e grade de 8 cores com pré-seleção);
- S5 (sheet de ordenação com 6 opções);
- S6 (dialog de exclusão) e snackbar de erro com "Tentar novamente";
- galeria de badges de prazo, chips de etiqueta, botões (incluindo disabled e
  loading) e campos;
- versão escura de S1, S2 e dos componentes (uma badge por tarefa);
- galeria completa dos 15 tokens de tipografia, da escala de shape e da tabela
  de motion;
- **cards de estresse a 200% de fonte** em S1 (linha, metadados e ordenação) e em
  **S2** (título, descrição sem rolagem interna, linha de etiqueta e chip de data),
  com o corpo rolando sob a top app bar fixa.

O preview é **apoio visual**; em caso de divergência, vale este documento.

## 16. Traceability

| Story | Cobertura de design |
|:--|:--|
| US-01 | S1 (linha com título/descrição em 2 linhas, FAB, empty state), S2 (criação/edição com `←` + "Salvar" na app bar, validação inline, sem ação destrutiva no formulário), S6 (exclusão pelo menu da linha), snackbar de erro de armazenamento com "Tentar novamente" e preservação do conteúdo; semântica de linha com texto integral; corpo de S2 rolável a 200% |
| US-02 | S2: limites 120/2000, contadores sempre visíveis (0/120, 0/2000), bloqueio no limite (limite atingido não é estado de erro), altura de linha estável (§8.3) |
| US-03 | S1: checkbox 48dp, abas Ativas/Concluídas, título riscado, marca "Concluída em dd/MM/aaaa", empty de Concluídas, foco e `stateDescription` |
| US-04 | S3 + S4 + S7: gestão de etiquetas; S4 com nome `singleLine` limite 30 e contador, erro inline de vazio e duplicata, grade de 8 cores em 4 colunas com alvo 48dp, anel/check e "Vermelho" pré-selecionada, loading/erro; S7 como **único** seletor ("Sem etiqueta" primeiro + atalho "Criar etiqueta") e linha somente leitura (`extraSmall`) no S2; estados vazios; chip com nome + cor e ellipsis com TalkBack completo; exclusão com confirmação |
| US-05 | S2 (chip de data único — data **ou** "Definir data", remover data, date picker pt-BR), S1 (4 estados de badge com texto + ícone, ausência de badge >7 dias, "Concluída em" substitui prazo), motion sem animação de cor |
| US-06 | S1: SearchField com placeholder e limpar, estado vazio com **eco da consulta** em 2 linhas + `contentDescription` completo + "Limpar busca", chrome (busca/abas/ordenação) preservado, consulta **preservada na troca de aba** e reaplicada, escopo por aba, foco visível |
| US-07 | S1 + S5: botão com ordenação vigente sempre visível (até 2 linhas a 200%, sem corte), sheet com as 6 opções em radio, seleção aplicada sem perder busca, estado persistido |
