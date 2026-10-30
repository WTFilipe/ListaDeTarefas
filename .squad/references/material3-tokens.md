# MATERIAL DESIGN 3 — REFERÊNCIA COMPACTA DE TOKENS

Use esta referência para criar `.squad/artifacts/design/COLOR_SCHEME.md` e
`.squad/artifacts/design/DESIGN_SPEC.md`.

## Regra de ouro de cores

- A spec de design **nunca** cita cor literal (`#RRGGBB`). Cite papéis/tokens:
  `colorScheme.primary`, `colorScheme.onPrimaryContainer`,
  `MaterialTheme.colorScheme.surfaceContainerHigh`.
- O arquivo `COLOR_SCHEME.md` é a única fonte onde valores hex podem existir,
  porque ele **define** o color scheme do projeto.
- Se o projeto já tiver um `ColorScheme`, reutilize-o e documente apenas os
  tokens. Se não tiver, o Designer cria um.

## 1. Color roles (light/dark)

Pares obrigatórios:

| Papel | Uso | Papel "on" |
|:--|:--|:--|
| `primary` | ação principal, ícones ativos | `onPrimary` |
| `primaryContainer` | containers tonais, FAB tonal | `onPrimaryContainer` |
| `secondary` | ações secundárias | `onSecondary` |
| `secondaryContainer` | chips, filtros selecionados | `onSecondaryContainer` |
| `tertiary` | acento/contraste | `onTertiary` |
| `tertiaryContainer` | destaque pontual | `onTertiaryContainer` |
| `error` | erro/destrutivo | `onError` |
| `errorContainer` | banner de erro | `onErrorContainer` |
| `surface` | fundo principal | `onSurface` |
| `surfaceVariant` | fundo alternativo | `onSurfaceVariant` |
| `surfaceContainerLowest/Low/Container/High/Highest` | níveis tonais | `onSurface` |
| `outline` | bordas fortes | — |
| `outlineVariant` | divisores/bordas suaves | — |
| `inverseSurface` / `inverseOnSurface` | snackbars/tooltips | — |
| `inversePrimary` | destaque sobre inverseSurface | — |
| `scrim` | overlay de modal | — |

Também documente: `surfaceTint`, `surfaceBright`, `surfaceDim`, `primaryFixed`,
`primaryFixedDim`, `onPrimaryFixed`, `onPrimaryFixedVariant` e equivalentes de
secondary/tertiary quando o projeto usar dynamic color/tonal elevation.

Regras:

- Contraste mínimo: 4.5:1 para texto normal; 3:1 para texto grande e ícones.
- `primaryContainer` ≠ `primary`; containers tonais evitam botões "gritantes".
- Não use `primary` como fundo de FAB padrão se o sistema for tonal; prefira
  `primaryContainer`.
- Em dark theme, `surface` não é preto puro; use os tokens tonais.

## 2. Tonal palette

- Gere 13 tons por matiz: 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 95, 99, 100.
- Light: `primary` = tom 40, `primaryContainer` = tom 90,
  `onPrimaryContainer` = tom 10, `surface` = tom 99, `onSurface` = tom 10.
- Dark: `primary` = tom 80, `primaryContainer` = tom 30,
  `onPrimaryContainer` = tom 90, `surface` = tom 10, `onSurface` = tom 90.
- Documente a matiz de origem e a lógica de geração no `COLOR_SCHEME.md`.

## 3. Typography

Escala MD3 (família padrão Roboto; use a fonte do projeto se houver):

| Token | Tamanho | Peso | Line height | Uso |
|:--|--:|--:|--:|:--|
| `displayLarge` | 57 | 400 | 64 | herói/raro |
| `displayMedium` | 45 | 400 | 52 | herói |
| `displaySmall` | 36 | 400 | 44 | títulos grandes |
| `headlineLarge` | 32 | 400 | 40 | cabeçalho de tela |
| `headlineMedium` | 28 | 400 | 36 | seções |
| `headlineSmall` | 24 | 400 | 32 | subtítulos |
| `titleLarge` | 22 | 400 | 28 | top app bar |
| `titleMedium` | 16 | 500 | 24 | títulos de card |
| `titleSmall` | 14 | 500 | 20 | labels fortes |
| `bodyLarge` | 16 | 400 | 24 | corpo |
| `bodyMedium` | 14 | 400 | 20 | corpo secundário |
| `bodySmall` | 12 | 400 | 16 | metadados |
| `labelLarge` | 14 | 500 | 20 | botões |
| `labelMedium` | 12 | 500 | 16 | chips |
| `labelSmall` | 11 | 500 | 16 | timestamps |

Regras: não invente tamanhos; use `MaterialTheme.typography.*`. Suporte font
scale do sistema sem truncar conteúdo crítico.

## 4. Shape

| Token | Raio | Uso |
|:--|--:|:--|
| `none` | 0dp | listas/dividers |
| `extraSmall` | 4dp | inputs filled (topo), badges |
| `small` | 8dp | chips, cards compactos |
| `medium` | 12dp | cards, containers |
| `large` | 16dp | FAB padrão, sheets |
| `extraLarge` | 28dp | dialogs, bottom sheets |
| `full` | 50% | pills, avatares, chips |

Regras: FAB padrão de 56dp usa `large` (16dp), não `extraLarge` (vira pill).
Não use formas orgânicas/zero-radius sem justificativa no sistema.

## 5. Elevation e superfícies tonais

- MD3 prefere **tonal elevation** a sombras.
- Níveis: 0 (surface), 1 (surfaceContainerLow), 2 (surfaceContainer),
  3 (surfaceContainerHigh), 4/5 (surfaceContainerHighest).
- FAB padrão: elevação 3; dialogs/sheets: elevação maior que o conteúdo.
- Em sistemas flat/tonais, mantenha a sombra do FAB constante entre estados e
  comunique press com mudança de tom/state layer.

## 6. State layers

Opacidade do conteúdo sobre o container:

| Estado | Opacidade |
|:--|--:|
| hover | 0.08 |
| focus | 0.10 (ou 0.12) |
| pressed | 0.10 (ou 0.12) |
| dragged | 0.16 |
| disabled | conteúdo 0.38; container 0.12 |

Use `MutableInteractionSource` + `collectIsPressedAsState`,
`collectIsHoveredAsState`, `collectIsFocusedAsState`. Não invente debounce por
relógio.

## 7. Motion

Durações:

| Token | ms | Uso |
|:--|--:|:--|
| `short1` | 50 | micro-feedback |
| `short2` | 100 | hover/press |
| `short3` | 150 | transições simples |
| `short4` | 200 | entrada/saída |
| `medium1` | 250 | mudanças de conteúdo |
| `medium2` | 300 | sheets/dialogs |
| `medium3` | 350 | navegação |
| `medium4` | 400 | transições complexas |
| `long1` | 450 | transições de tela |
| `long2` | 500 | grandes mudanças |
| `long3` | 550 | ambient |
| `long4` | 600 | ambient |

Easing: `standard`, `standardAccelerate`, `standardDecelerate`,
`emphasized`, `emphasizedAccelerate`, `emphasizedDecelerate`,
`linear`. Use `tween`/`spring` com justificativa; evite bounce/scale-spring em
sistemas sóbrios.

## 8. Componentes (mínimo)

- **Button**: filled, tonal, outlined, text, elevated; altura 40dp; label
  `labelLarge`; shape `full`; touch target 48dp.
- **FAB**: 56dp, shape `large` (16dp), container tonal, ícone 24dp, elevação 3;
  extended FAB altura 56dp com label `labelLarge`.
- **Card**: filled/elevated/outlined; shape `medium` (12dp); padding 16dp.
- **Chip**: assist/filter/input/suggestion; shape `small` (8dp); altura 32dp.
- **TextField**: filled ou outlined; shape `extraSmall` (filled, topo) ou
  `extraSmall` (outlined); label/placeholder claros; erro com
  `error`/`onErrorContainer`.
- **Dialog**: shape `extraLarge` (28dp); scrim; ações claras.
- **BottomSheet**: shape `extraLarge` no topo; drag handle.
- **NavigationBar**: 80dp; ícones 24dp; label `labelMedium`; item ativo com
  `secondaryContainer`.
- **TopAppBar**: small/medium/large; título `titleLarge`/`headlineSmall`.

## 9. Acessibilidade

- Alvo de toque mínimo 48x48dp.
- `contentDescription` em ícones interativos; `null` apenas quando o nó pai
  mesclado já descreve o elemento (`semantics(mergeDescendants = true)`).
- Não dependa só de cor para estado; use forma/texto/semântica.
- Suporte RTL (`start`/`end`, `Alignment.Start/End`).
- Suporte font scale; evite alturas fixas para texto.
- Contraste: 4.5:1 texto, 3:1 ícones/limites.
- Foco visível para teclado/switch.

## 10. Checklist do DESIGN_SPEC.md

- [ ] Objetivo e telas cobertas.
- [ ] Color scheme referenciado por tokens (`colorScheme.*`), sem hex.
- [ ] Tipografia por token.
- [ ] Shape por token.
- [ ] Elevação/tonalidade.
- [ ] Motion com durações/easing.
- [ ] Estados: rest, hover, focus, pressed, disabled, loading, empty, error.
- [ ] Acessibilidade (touch target, content description, contraste, RTL, font
      scale).
- [ ] Componentes e variantes.
- [ ] Do's and don'ts.
- [ ] Mapeamento para Compose (`MaterialTheme.colorScheme.*`,
      `MaterialTheme.typography.*`, `MaterialTheme.shapes.*`).
- [ ] Nenhuma cor literal na spec.
