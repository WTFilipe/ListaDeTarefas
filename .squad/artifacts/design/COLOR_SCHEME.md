---
artifact: color_scheme
phase: design
version: 2
author: designer
status: in_review
reviewer: chaos-design
verdict: pending
next_action: chaos-design
summary: "Color scheme MD3 claro/escuro (azul profundo + âmbar), paleta fixa de 8 cores de etiqueta e tabela de contraste recalculada (v2)."
updated_at: 2026-10-30T02:30:00Z
---

# COLOR SCHEME — App de Lista de Tarefas

> **Este é o único artefato que contém valores hex.** Ele **define** o color scheme
> do projeto. O `DESIGN_SPEC.md` e o `PREVIEW.html` referenciam apenas papéis e
> tokens.
>
> **Correção da v2:** §8 recalculada com luminância relativa (WCAG 2.1) — a
> decisão de cor não mudou; §3.7 publica os tons de papel que não derivam
> diretamente das 13 tabelas de §3 (m8).

## 1. Contexto e decisão sobre o tema existente

O projeto tem, hoje, apenas o tema de *template* do Android Studio:

- `ui/theme/Color.kt` com três cores do template (roxo/lilás/rosa);
- `ui/theme/Theme.kt` com `lightColorScheme`/`darkColorScheme` preenchendo **só**
  `primary`, `secondary` e `tertiary`;
- `dynamicColor = true` (dynamic color do Android 12+);
- `ui/theme/Type.kt` com o `Typography` padrão (apenas `bodyLarge` sobrescrito).

Esse conjunto **não é** um color scheme de produto: os containers, superfícies e
papeis "on" ficariam nos defaults do Material, e o dynamic color mudaria as cores
a cada aparelho. Isso conflita com dois requisitos do produto:

1. a **paleta fixa de etiquetas** (PRD US-04 AC 4) precisa de contraste previsível
   contra os containers de chip em claro **e** escuro;
2. o indicador de prazo usa papéis semânticos fixos (`error`, `tertiary`,
   `secondary`), que não podem variar por fabricante.

**Decisão de design:** o projeto passa a usar o color scheme definido abaixo,
completo (todos os papéis preenchidos) e **com dynamic color desligado**
(`dynamicColor = false`). O tema **segue o claro/escuro do sistema**
(`isSystemInDarkTheme()`), sem seletor no app — conforme PRD §2 e §6.

> Ajustar `Theme.kt`/`Color.kt` é responsabilidade da fase de desenvolvimento; esta
> especificação é a fonte dos valores.

## 2. Matizes de origem e lógica de geração

| Paleta | Matiz de origem | Lógica |
|:--|--:|:--|
| Primary | 215° (azul profundo) | cor de marca/ação; tom 40 no claro, 80 no escuro |
| Secondary | 205° com croma reduzido | apoio (abas, ordenação, badge "Amanhã"); evita dois azuis "gritantes" |
| Tertiary | 45° (âmbar/dourado) | acento semântico de atenção ("Hoje"); tom 40 claro / 80 escuro |
| Neutral | 215° com croma mínimo | superfícies e texto; `surface` claro = tom 99, escuro = tom 10 |
| NeutralVariant | 215° | `outline`, `outlineVariant`, `onSurfaceVariant` |
| Error | baseline M3 (~10°) | erro/destrutivo, "Atrasada" |

Regras aplicadas (referência `material3-tokens.md` §2):

- Light: `primary` = tom 40, `primaryContainer` = tom 90,
  `onPrimaryContainer` = tom 10, `surface` = tom 99, `onSurface` = tom 10.
- Dark: `primary` = tom 80, `primaryContainer` = tom 30,
  `onPrimaryContainer` = tom 90, `surface` = tom 10, `onSurface` = tom 90.
- Dark não usa preto puro em `surface` (tom 10 da neutral).
- `primaryContainer` ≠ `primary`; FAB usa container tonal.
- Papéis que usam tom ajustado (superfícies, contornos e alguns pares
  semânticos) estão consolidados em **§3.7**; §4/§5 são os valores normativos.

## 3. Tonal palettes (13 tons)

### 3.1 Primary — hue 215

| Tom | Hex |
|--:|:--|
| 0 | `#000000` |
| 10 | `#001D33` |
| 20 | `#003259` |
| 30 | `#00497A` |
| 40 | `#175A94` |
| 50 | `#3773AE` |
| 60 | `#5590C8` |
| 70 | `#7AABE0` |
| 80 | `#9FCAFF` |
| 90 | `#D1E4FF` |
| 95 | `#E8F1FF` |
| 99 | `#FAFCFF` |
| 100 | `#FFFFFF` |

### 3.2 Secondary — hue 205, croma reduzido

| Tom | Hex |
|--:|:--|
| 0 | `#000000` |
| 10 | `#0B2434` |
| 20 | `#203548` |
| 30 | `#33495D` |
| 40 | `#46617B` |
| 50 | `#5F7690` |
| 60 | `#7890AA` |
| 70 | `#93AAC5` |
| 80 | `#B3C8DC` |
| 90 | `#D0E4F8` |
| 95 | `#E7F1FC` |
| 99 | `#FAFCFF` |
| 100 | `#FFFFFF` |

### 3.3 Tertiary — hue 45

| Tom | Hex |
|--:|:--|
| 0 | `#000000` |
| 10 | `#261A00` |
| 20 | `#3F2E00` |
| 30 | `#5C4300` |
| 40 | `#7A5900` |
| 50 | `#997000` |
| 60 | `#B98A26` |
| 70 | `#D6A94A` |
| 80 | `#E8C76A` |
| 90 | `#F6E2A8` |
| 95 | `#FCF0CB` |
| 99 | `#FFFBF0` |
| 100 | `#FFFFFF` |

### 3.4 Neutral — hue 215, croma mínimo

| Tom | Hex |
|--:|:--|
| 0 | `#000000` |
| 10 | `#191C20` |
| 20 | `#2E3135` |
| 30 | `#44474C` |
| 40 | `#5C5F64` |
| 50 | `#75777C` |
| 60 | `#8F9196` |
| 70 | `#A9ABB1` |
| 80 | `#C5C6CB` |
| 90 | `#E1E2E8` |
| 95 | `#EFF0F6` |
| 99 | `#F8FAFD` |
| 100 | `#FFFFFF` |

### 3.5 NeutralVariant — hue 215

| Tom | Hex |
|--:|:--|
| 0 | `#000000` |
| 10 | `#191C20` |
| 20 | `#2D3036` |
| 30 | `#43474E` |
| 40 | `#5B5F66` |
| 50 | `#73777F` |
| 60 | `#8D9199` |
| 70 | `#A8ABB3` |
| 80 | `#C3C7CF` |
| 90 | `#DFE3EA` |
| 95 | `#EDF0F7` |
| 99 | `#F9FAFF` |
| 100 | `#FFFFFF` |

### 3.6 Error — baseline M3

| Tom | Hex |
|--:|:--|
| 0 | `#000000` |
| 10 | `#410E0B` |
| 20 | `#601410` |
| 30 | `#8C1D18` |
| 40 | `#B3261E` |
| 50 | `#DC362E` |
| 60 | `#E46962` |
| 70 | `#EC928E` |
| 80 | `#F2B8B5` |
| 90 | `#F9DEDC` |
| 95 | `#FCEEEE` |
| 99 | `#FFFBF9` |
| 100 | `#FFFFFF` |

### 3.7 Tons ajustados (papéis fora das 13 tabelas)

Os papéis abaixo não coincidem com os tons de §3.1–§3.6 (arredondamento de
superfície/contorno e pares semânticos). Ficam publicados **aqui** para manter
uma única fonte; nenhum outro tom pode ser usado sem entrar nesta tabela.

**Neutros de superfície e contorno (light)**

| Papel | Hex | Equivalente próximo em §3 |
|:--|:--|:--|
| `surfaceDim` (light) | `#D8DADE` | entre N80 e N90 |
| `surfaceContainerLow` (light) | `#F2F4F8` | entre N95 e N99 |
| `surfaceContainer` (light) | `#ECF0F5` | entre N95 e N99 |
| `surfaceContainerHigh` (light) | `#E6EAEF` | entre N90 e N95 |
| `surfaceContainerHighest` (light) | `#E0E5EA` | ≈ N90 |
| `inverseOnSurface` (light) | `#EFF1F5` | ≈ N95 (`#EFF0F6`) |

**Neutros de superfície e contorno (dark)**

| Papel | Hex | Equivalente próximo em §3 |
|:--|:--|:--|
| `surface` / `surfaceDim` (dark) | `#111418` | mais escuro que N10 |
| `surfaceBright` (dark) | `#373A3F` | entre N20 e N30 |
| `surfaceContainerLowest` (dark) | `#0C0F13` | abaixo de N10 |
| `surfaceContainer` (dark) | `#1D2024` | ≈ N10/N20 |
| `surfaceContainerHigh` (dark) | `#272A2F` | entre N10 e N20 |
| `surfaceContainerHighest` (dark) | `#32353A` | ≈ N20 |
| `onSurface` (dark) | `#E1E3E8` | ≈ N90 (`#E1E2E8`) |
| `surfaceVariant` (dark) | `#42474E` | ≈ NV30 (`#43474E`) |
| `onSurfaceVariant` (dark) | `#C2C7CF` | ≈ NV80 (`#C3C7CF`) |
| `outline` (dark) | `#8C9199` | ≈ NV60 (`#8D9199`) |
| `outlineVariant` (dark) | `#42474E` | ≈ NV30 (`#43474E`) |

**Pares semânticos ajustados**

| Papel | Hex | Observação |
|:--|:--|:--|
| `primaryContainer` (dark) | `#0B4A74` | ≈ P30 (`#00497A`) |
| `onPrimary` (dark) | `#003257` | ≈ P20 (`#003259`) |
| `onSecondary` (dark) | `#1C3245` | ≈ S20 (`#203548`) |
| `error` (dark) | `#FFB4AB` | baseline M3 dark; E80 = `#F2B8B5` |
| `errorContainer` (dark) | `#93000A` | E30 alt.; E30 = `#8C1D18` |
| `onError` (dark) | `#690005` | baseline M3 dark |
| `onErrorContainer` (dark) | `#FFDAD6` | baseline M3 dark |

## 4. Color roles — Light

| Papel MD3 | Token Compose | Hex | Par "on" | Hex do par |
|:--|:--|:--|:--|:--|
| primary | `primary` | `#175A94` | `onPrimary` | `#FFFFFF` |
| primaryContainer | `primaryContainer` | `#D1E4FF` | `onPrimaryContainer` | `#001D33` |
| secondary | `secondary` | `#46617B` | `onSecondary` | `#FFFFFF` |
| secondaryContainer | `secondaryContainer` | `#D0E4F8` | `onSecondaryContainer` | `#0B2434` |
| tertiary | `tertiary` | `#7A5900` | `onTertiary` | `#FFFFFF` |
| tertiaryContainer | `tertiaryContainer` | `#F6E2A8` | `onTertiaryContainer` | `#261A00` |
| error | `error` | `#B3261E` | `onError` | `#FFFFFF` |
| errorContainer | `errorContainer` | `#F9DEDC` | `onErrorContainer` | `#410E0B` |
| surface | `surface` | `#F8FAFD` | `onSurface` | `#191C20` |
| background (legado M3) | `background` | `#F8FAFD` | `onBackground` | `#191C20` |
| surfaceDim | `surfaceDim` | `#D8DADE` | — | — |
| surfaceBright | `surfaceBright` | `#F8FAFD` | — | — |
| surfaceContainerLowest | `surfaceContainerLowest` | `#FFFFFF` | `onSurface` | `#191C20` |
| surfaceContainerLow | `surfaceContainerLow` | `#F2F4F8` | `onSurface` | `#191C20` |
| surfaceContainer | `surfaceContainer` | `#ECF0F5` | `onSurface` | `#191C20` |
| surfaceContainerHigh | `surfaceContainerHigh` | `#E6EAEF` | `onSurface` | `#191C20` |
| surfaceContainerHighest | `surfaceContainerHighest` | `#E0E5EA` | `onSurface` | `#191C20` |
| surfaceVariant | `surfaceVariant` | `#DFE3EA` | `onSurfaceVariant` | `#43474E` |
| outline | `outline` | `#73777F` | — | — |
| outlineVariant | `outlineVariant` | `#C3C7CF` | — | — |
| inverseSurface | `inverseSurface` | `#2E3135` | `inverseOnSurface` | `#EFF1F5` |
| inversePrimary | `inversePrimary` | `#9FCAFF` | — | — |
| surfaceTint | `surfaceTint` | `#175A94` | — | — |
| scrim | `scrim` | `#000000` | — | — |

Papéis *fixed* (usados apenas se houver dynamic color/institucional; no MVP ficam
disponíveis para consistência):

| Papel | Hex |
|:--|:--|
| primaryFixed | `#D1E4FF` |
| primaryFixedDim | `#9FCAFF` |
| onPrimaryFixed | `#001D33` |
| onPrimaryFixedVariant | `#00497A` |
| secondaryFixed | `#D0E4F8` |
| secondaryFixedDim | `#B3C8DC` |
| onSecondaryFixed | `#0B2434` |
| onSecondaryFixedVariant | `#33495D` |
| tertiaryFixed | `#F6E2A8` |
| tertiaryFixedDim | `#E8C76A` |
| onTertiaryFixed | `#261A00` |
| onTertiaryFixedVariant | `#5C4300` |

## 5. Color roles — Dark

| Papel MD3 | Token Compose | Hex | Par "on" | Hex do par |
|:--|:--|:--|:--|:--|
| primary | `primary` | `#9FCAFF` | `onPrimary` | `#003257` |
| primaryContainer | `primaryContainer` | `#0B4A74` | `onPrimaryContainer` | `#D1E4FF` |
| secondary | `secondary` | `#B3C8DC` | `onSecondary` | `#1C3245` |
| secondaryContainer | `secondaryContainer` | `#33495D` | `onSecondaryContainer` | `#D0E4F8` |
| tertiary | `tertiary` | `#E8C76A` | `onTertiary` | `#3F2E00` |
| tertiaryContainer | `tertiaryContainer` | `#5C4300` | `onTertiaryContainer` | `#F6E2A8` |
| error | `error` | `#FFB4AB` | `onError` | `#690005` |
| errorContainer | `errorContainer` | `#93000A` | `onErrorContainer` | `#FFDAD6` |
| surface | `surface` | `#111418` | `onSurface` | `#E1E3E8` |
| background (legado M3) | `background` | `#111418` | `onBackground` | `#E1E3E8` |
| surfaceDim | `surfaceDim` | `#111418` | — | — |
| surfaceBright | `surfaceBright` | `#373A3F` | — | — |
| surfaceContainerLowest | `surfaceContainerLowest` | `#0C0F13` | `onSurface` | `#E1E3E8` |
| surfaceContainerLow | `surfaceContainerLow` | `#191C20` | `onSurface` | `#E1E3E8` |
| surfaceContainer | `surfaceContainer` | `#1D2024` | `onSurface` | `#E1E3E8` |
| surfaceContainerHigh | `surfaceContainerHigh` | `#272A2F` | `onSurface` | `#E1E3E8` |
| surfaceContainerHighest | `surfaceContainerHighest` | `#32353A` | `onSurface` | `#E1E3E8` |
| surfaceVariant | `surfaceVariant` | `#42474E` | `onSurfaceVariant` | `#C2C7CF` |
| outline | `outline` | `#8C9199` | — | — |
| outlineVariant | `outlineVariant` | `#42474E` | — | — |
| inverseSurface | `inverseSurface` | `#E1E3E8` | `inverseOnSurface` | `#2E3135` |
| inversePrimary | `inversePrimary` | `#175A94` | — | — |
| surfaceTint | `surfaceTint` | `#9FCAFF` | — | — |
| scrim | `scrim` | `#000000` | — | — |

## 6. Paleta fixa de etiquetas (produto)

Paleta **fechada** de 8 cores (US-04 AC 4). Cada entrada define:

- **container** → fundo do chip da etiqueta na linha da tarefa;
- **onContainer** → texto/ícone do chip (contraste alto);
- **dot** → amostra circular (lista de etiquetas) e marcador opcional dentro do
  chip; contraste ≥ 3:1 contra `surface` e ≥ 3:1 contra o próprio container.

Nomes exibidos (também usados como `contentDescription` do seletor de cor):
`Vermelho`, `Laranja`, `Mostarda`, `Verde`, `Verde-água`, `Azul`, `Roxo`, `Rosa`.
A **primeira** (`Vermelho`) é a pré-selecionada na criação (US-04 AC 2).

| # | Nome | Light container | Light onContainer | Light dot | Dark container | Dark onContainer | Dark dot |
|--:|:--|:--|:--|:--|:--|:--|:--|
| 1 | Vermelho | `#FFDAD6` | `#410002` | `#B3261E` | `#93000A` | `#FFDAD6` | `#FFB4AB` |
| 2 | Laranja | `#FFDCC5` | `#3B1A00` | `#9A4500` | `#7A3500` | `#FFDCC5` | `#FFB784` |
| 3 | Mostarda | `#F8E3AE` | `#3A2E00` | `#8A6A00` | `#564500` | `#F8E3AE` | `#E2C46B` |
| 4 | Verde | `#C8EBC6` | `#0B2B0A` | `#2F6B2D` | `#1B4D1A` | `#C8EBC6` | `#A5D6A3` |
| 5 | Verde-água | `#C2ECE7` | `#033731` | `#00695F` | `#00504A` | `#C2ECE7` | `#9FE0D9` |
| 6 | Azul | `#D2E4FA` | `#0B2A47` | `#175A94` | `#0B4A74` | `#D2E4FA` | `#9FCAFF` |
| 7 | Roxo | `#E5DDFF` | `#211043` | `#5B3FA8` | `#3A2A6B` | `#E5DDFF` | `#CBBEFF` |
| 8 | Rosa | `#FFD9E3` | `#3B0718` | `#A32B54` | `#641F30` | `#FFD9E3` | `#FFB1C3` |

Regras de uso da paleta:

- A etiqueta é identificada por **nome + cor**; a cor nunca aparece sozinha
  (US-04 AC 10). No chip, o nome é sempre visível (com reticências se faltar
  largura), e o `contentDescription` anuncia o nome completo.
- `dot` nunca é usado como cor de texto (contraste insuficiente para texto
  pequeno em alguns pares no claro).
- Não criar cores fora desta tabela; não há seletor de cor livre (US-04 AC 4).
- A paleta é independente do dynamic color e **não** muda com o fabricante.

## 7. Estados semânticos do indicador de prazo

Mapeamento fechado (US-05 AC 3 e 4). Texto + ícone sempre juntos; a cor é
reforço, nunca o único sinal.

| Estado | Container (claro / escuro) | Conteúdo (claro / escuro) | Ícone |
|:--|:--|:--|:--|
| Atrasada | `errorContainer` (`#F9DEDC` / `#93000A`) | `onErrorContainer` (`#410E0B` / `#FFDAD6`) | alerta (`error`) |
| Hoje | `tertiaryContainer` (`#F6E2A8` / `#5C4300`) | `onTertiaryContainer` (`#261A00` / `#F6E2A8`) | calendário do dia |
| Amanhã | `secondaryContainer` (`#D0E4F8` / `#33495D`) | `onSecondaryContainer` (`#0B2434` / `#D0E4F8`) | evento |
| Até 7 dias | `surfaceContainerHigh` (`#E6EAEF` / `#272A2F`) + borda `outlineVariant` | `onSurfaceVariant` (`#43474E` / `#C2C7CF`) | relógio |
| Sem indicador (> 7 dias / sem data) | — | — | — |

Tarefa concluída: marca textual "Concluída em dd/MM/aaaa" em
`onSurfaceVariant` + ícone de check; nunca usa os containers de prazo
(US-05 AC 6).

## 8. Contraste (WCAG 2.1 AA)

Pares críticos medidos (razão aproximada):

| Par | Claro | Escuro |
|:--|--:|--:|
| `onSurface` sobre `surface` | ≈ 16,3:1 | ≈ 14,4:1 |
| `onSurfaceVariant` sobre `surface` | ≈ 8,9:1 | ≈ 10,9:1 |
| `onPrimary` sobre `primary` | ≈ 7,2:1 | ≈ 7,8:1 |
| `onPrimaryContainer` sobre `primaryContainer` | ≈ 13,3:1 | ≈ 7,2:1 |
| `onTertiaryContainer` sobre `tertiaryContainer` | ≈ 13,3:1 | ≈ 7,3:1 |
| `onErrorContainer` sobre `errorContainer` | ≈ 12,8:1 | ≈ 7,2:1 |
| `onSecondaryContainer` sobre `secondaryContainer` | ≈ 12,3:1 | ≈ 7,2:1 |
| `outline` sobre `surface` (gráfico) | ≈ 4,3:1 | ≈ 5,8:1 |
| `dot` de etiqueta sobre `surface` (gráfico) | ≥ 4,9:1 | ≥ 10,8:1 |
| `onContainer` de etiqueta sobre o container | 10,3:1 – 13,3:1 | 7,0:1 – 9,4:1 |
| `dot` de etiqueta sobre o próprio container | ≥ 4,0:1 | ≥ 5,5:1 |

- Método: luminância relativa WCAG 2.1 (sRGB), calculada par a par sobre os
  valores de §4, §5 e §6. Números arredondados para uma casa decimal.
- Texto normal ≥ 4,5:1 e texto grande/ícones ≥ 3:1 conforme `material3-tokens.md`.
- Contraste de `outline` é usado apenas para bordas/divisores (≥ 3:1 exigido).
- A paleta de etiquetas passa AA em todos os pares medidos; o pior caso de
  `onContainer` no escuro é 7,0:1 (Laranja) e o pior de `dot` sobre o container
  no claro é 4,0:1 (Mostarda), ainda acima de 3:1 (elemento gráfico).
- Se algum teste automatizado de contraste for criado, ele deve usar estes
  números como limite inferior — **não** valores genéricos como "≥ 10:1".

## 9. Tokens de estado (state layers)

| Estado | Opacidade do conteúdo sobre o container |
|:--|--:|
| hover | 0,08 |
| focus | 0,10 |
| pressed | 0,12 |
| dragged | 0,16 |
| disabled (conteúdo) | 0,38 |
| disabled (container) | 0,12 |

## 10. Mapeamento para Compose

`lightColorScheme(...)` / `darkColorScheme(...)` com os parâmetros abaixo (nomes
idênticos aos papéis da tabela; exige `material3` ≥ 1.2 para os
`surfaceContainer*`):

| Parâmetro Compose | Origem neste documento |
|:--|:--|
| `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer` | §4 / §5 |
| `secondary`, `onSecondary`, `secondaryContainer`, `onSecondaryContainer` | §4 / §5 |
| `tertiary`, `onTertiary`, `tertiaryContainer`, `onTertiaryContainer` | §4 / §5 |
| `error`, `onError`, `errorContainer`, `onErrorContainer` | §4 / §5 |
| `surface`, `onSurface`, `surfaceVariant`, `onSurfaceVariant` | §4 / §5 |
| `background`, `onBackground` (legado; espelham `surface`/`onSurface`) | §4 / §5 |
| `surfaceDim`, `surfaceBright` | §4 / §5 |
| `surfaceContainerLowest|Low|Container|High|Highest` | §4 / §5 |
| `outline`, `outlineVariant` | §4 / §5 |
| `inverseSurface`, `inverseOnSurface`, `inversePrimary` | §4 / §5 |
| `scrim`, `surfaceTint` | §4 / §5 |
| `*Fixed`, `*FixedDim`, `on*Fixed`, `on*FixedVariant` | §4 |

Fora do `ColorScheme` do Material, a **paleta de etiquetas** é exposta como
tokens próprios do projeto (design system), por exemplo um `CompositionLocal`
`LocalLabelPalette` com `container`, `onContainer` e `dot` por entrada, mais o
nome localizado da cor. O `DESIGN_SPEC.md` §13 descreve o contrato sem código.

Requisitos de implementação do tema:

1. `dynamicColor = false` (fixo), mantendo `darkTheme = isSystemInDarkTheme()`.
2. Nomear as cores por papel (ex.: `PrimaryLight`, `OnPrimaryLight`,
   `SurfaceContainerHighDark`) — **nunca** por matiz ("Purple80").
3. Remover as três cores do template que não pertencem a este scheme.
4. `scrim` é preto com 32% de alpha no uso de modal (definido no componente, não
   no token).

## 11. Uso e restrições

- `primary` é cor de ação/seleção; não usar `primary` como fundo de FAB (usar
  `primaryContainer`, sistema tonal).
- `error` só para erro/destrutivo e para o estado "Atrasada".
- `tertiary` sinaliza atenção ("Hoje") e ações pontuais; não usar em botões
  primários.
- `secondary` apoia hierarquia (abas, chip de ordenação, badge "Amanhã").
- Divisores e bordas suaves: `outlineVariant`. Bordas fortes/foco: `outline`.
- Em dark, superfícies sobem de tom (`surfaceContainerLow` →
  `surfaceContainerHighest`), nunca por preto/branco puros.
- Texto sobre superfície sempre `onSurface` ou `onSurfaceVariant`; nunca
  `primary` em corpo de texto longo.

## 12. Riscos e limitações

| Risco | Mitigação |
|:--|:--|
| Desligar dynamic color contraria o template atual | Decisão registrada aqui e no `DESIGN_SPEC.md` §14; dev ajusta `Theme.kt` |
| Paleta fixa de 8 cores pode ter pares com contraste < 4,5:1 no escuro | Medido em §8: `onContainer` ≥ 7,0:1 e `dot` ≥ 5,5:1 no escuro; qualquer nova cor precisa entrar na tabela e ser medida |
| Tabela de contraste divergir do runtime | §8 é a evidência; testes de contraste devem usar os valores medidos como limite inferior |
| "Até 7 dias" em `surfaceContainerHigh` pode parecer desabilitado | Borda `outlineVariant` + ícone e texto; o estado nunca depende de cor |
| Tema do sistema muda enquanto o app está aberto | Recomposição padrão do Compose; nenhum estado de cor persistido |
| Uso indevido de `primary` como fundo de chip de etiqueta | Chips usam apenas a paleta de etiquetas; revisão de design valida |
