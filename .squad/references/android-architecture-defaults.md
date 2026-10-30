# ANDROID — DEFAULTS DE ARQUITETURA E CONVENÇÕES

Estes são os defaults que o Architect usa quando o projeto ainda não tem um
`.squad/artifacts/architecture/PROFILE.md`. Ele deve confirmar/ajustar com o
usuário via dashboard antes de escrever a arquitetura.

## 1. Premissas

- Projeto Android nativo, Kotlin + Jetpack Compose.
- Sempre multimódulo, salvo decisão explícita do usuário.
- MVI (state/action/effect) na camada de apresentação.
- Unidirectional Data Flow; estado imutável.
- `minSdk`/`targetSdk` definidos no `PROFILE.md` (default: minSdk 26,
  targetSdk/compileSdk estável atual).
- Gradle Kotlin DSL (`build.gradle.kts`), version catalog
  (`gradle/libs.versions.toml`) e convention plugins em `build-logic`.

## 2. Módulos sugeridos

```
:app
:core:common
:core:designsystem
:core:ui
:core:network
:core:database
:core:datastore
:core:domain
:core:testing
:feature:<feature>
:data:<feature>
:domain:<feature>        # opcional; use :core:domain se não houver regra própria
:build-logic
```

Regras:

- `:app` só compõe features, navegação e DI.
- `:feature:*` depende de `:core:*` e do domínio; nunca de outra feature.
- `:data:*` implementa repositórios definidos no domínio.
- `:core:designsystem` contém tema, tokens, componentes reutilizáveis.
- `:core:network`/`:core:database`/`:core:datastore` contêm infraestrutura.
- Dependências apontam para dentro (features -> core; data -> domain).
- Sem dependência circular.

## 3. MVI (state/action/effect)

Contrato por tela:

```kotlin
data class LoginState(
    val isLoading: Boolean = false,
    val email: String = "",
    val error: String? = null,
)

sealed interface LoginAction {
    data class EmailChanged(val value: String) : LoginAction
    data object SubmitClicked : LoginAction
    data object RetryClicked : LoginAction
}

sealed interface LoginEffect {
    data object NavigateHome : LoginEffect
    data class ShowSnackbar(val message: String) : LoginEffect
}
```

Regras:

- `State` é imutável e serializável quando possível; sem lógica de UI.
- `Action` representa intenção do usuário/sistema; nomes no passado/imperativo
  consistente (ex.: `SubmitClicked`, `EmailChanged`).
- `Effect` é one-shot (navegação, snackbar, share); não persiste no estado.
- ViewModel expõe `StateFlow<State>` e `SharedFlow/Channel<Effect>`.
- Reducer puro quando fizer sentido; side effects na camada de domínio/data.
- Não expor `MutableStateFlow` nem tipos de data/network na UI.
- Coletar estado com `collectAsStateWithLifecycle()`.
- Eventos de UI não fazem I/O na main thread.

## 4. Nomenclatura

### Módulos/pacotes

- Módulo: `feature:login`, `data:login`, `core:network`.
- Pacote base: `com.<empresa>.<app>`.
- Feature: `com.<empresa>.<app>.feature.login`.
- Camadas dentro da feature: `presentation`, `domain`, `data`, `di`,
  `navigation`.
- Core: `com.<empresa>.<app>.core.<área>`.

### Classes/arquivos

| Tipo | Padrão | Exemplo |
|:--|:--|:--|
| Tela/rota | `XxxScreen`, `XxxRoute` | `LoginScreen.kt`, `LoginRoute.kt` |
| ViewModel | `XxxViewModel` | `LoginViewModel.kt` |
| Estado | `XxxState` | `LoginState.kt` |
| Ação | `XxxAction` | `LoginAction.kt` |
| Efeito | `XxxEffect` | `LoginEffect.kt` |
| UI model | `XxxUiModel` | `LoginUiModel.kt` |
| Use case | `XxxUseCase` | `LoginUseCase.kt` |
| Repositório (contrato) | `XxxRepository` | `LoginRepository.kt` |
| Repositório (impl) | `XxxRepositoryImpl` | `LoginRepositoryImpl.kt` |
| Fonte de dados | `XxxRemoteDataSource`, `XxxLocalDataSource` | `LoginRemoteDataSource.kt` |
| DTO | `XxxDto` | `LoginRequestDto.kt` |
| Entity | `XxxEntity` | `LoginEntity.kt` |
| Mapper | `XxxMapper` | `LoginMapper.kt` |
| Módulo DI | `XxxModule` | `LoginModule.kt` |
| Componente Compose | `Xxx` (PascalCase) | `LoginCard.kt` |

- Um arquivo por declaração pública principal; nome do arquivo = nome da
  declaração.
- Constantes: `UPPER_SNAKE_CASE`.
- Variáveis/funções: `camelCase`.
- Booleanos: `isX`, `hasX`, `canX`.
- Coleções: plural.
- Evitar abreviações obscuras; siglas conhecidas ok (`Id`, `Url`, `Api`).

## 5. Hierarquia de pastas (feature)

```
feature/login/
  src/main/kotlin/com/empresa/app/feature/login/
    presentation/
      LoginRoute.kt
      LoginScreen.kt
      LoginViewModel.kt
      LoginState.kt
      LoginAction.kt
      LoginEffect.kt
      components/
    domain/
      LoginUseCase.kt
      model/
    data/
      LoginRepositoryImpl.kt
      remote/
      local/
      mapper/
    di/
      LoginModule.kt
    navigation/
      LoginNavigation.kt
  src/test/kotlin/...
  src/androidTest/kotlin/...
```

- `components/` para composables internos da feature.
- `model/` para modelos de domínio.
- `mapper/` para conversões DTO/Entity/UI.
- `navigation/` para rotas/grafo da feature.

## 6. Eventos (Firebase/Analytics)

- Nome do evento: `snake_case`, `screen_action_result`.
- Exemplos: `login_submit_clicked`, `login_success`, `login_error`,
  `task_created_success`, `task_deleted`.
- Parâmetros: `snake_case`, sem PII; nunca e-mail, nome, token ou ID de usuário.
- Centralizar nomes em `object AnalyticsEvents` ou `sealed interface` para evitar
  strings soltas.
- Não logar conteúdo de tarefas/usuários.

## 7. Camadas e dependências

- `presentation` -> `domain` (use cases/modelos), nunca `data`.
- `data` -> `domain` (implementa contratos) e infraestrutura.
- `domain` -> Kotlin puro; sem Android, Retrofit, Room ou Compose.
- `core:designsystem` -> Compose/Material3; não conhece features.
- `:app` -> features + DI + navegação.
- Injeção de dependência: Hilt (default) ou Koin; interfaces no domínio,
  implementações em data.
- Erros: mapear para um `sealed interface` de erro de domínio; não vazar
  exceptions de infraestrutura para a UI.
- Navegação: Navigation Compose ou tipo-safe routes; sem strings de rota
  espalhadas.

## 8. Rede, persistência e assincronismo

- Rede: Retrofit + OkHttp (default) ou Ktor; serialização Kotlinx.
- Persistência: Room (default) ou DataStore para preferências.
- Corrotinas/Flow; `Dispatchers.IO` para I/O; `viewModelScope` para UI.
- Timeouts, retry com backoff e tratamento de offline documentados.
- Sem HTTP em claro; HTTPS obrigatório.
- Sem secrets no código; usar `local.properties`/variáveis de ambiente e
  `BuildConfig` apenas quando seguro.

## 9. Testes

- Unitários: ViewModel/reducer/use cases com `kotlinx-coroutines-test` e Turbine.
- UI: Compose UI tests para fluxos críticos.
- Integração: Room/Retrofit com fakes/MockWebServer.
- Contratos: garantir que `State`/`Effect` cubram os cenários de aceite da PRD.
- Cobertura mínima definida no `PROFILE.md` (default: fluxos críticos cobertos,
  não perseguir % cego).

## 10. Perguntas que o Architect deve fazer se não houver PROFILE.md

1. Nome do app/empresa e pacote base.
2. Módulos: confirmar multi-módulo e lista inicial de features.
3. `minSdk`/`targetSdk`/`compileSdk`.
4. DI: Hilt, Koin ou outro.
5. Rede: Retrofit/Ktor/GraphQL; autenticação.
6. Persistência: Room/DataStore/nenhuma.
7. Eventos: Firebase Analytics? Prefixos e parâmetros padrão.
8. Testes: estratégia e cobertura mínima.
9. Navegação: Navigation Compose/outra.
10. Restrições de segurança/compliance.

Use no máximo 8 perguntas por rodada, com opções e defaults. Grave o resultado
em `.squad/artifacts/architecture/PROFILE.md`.

## 11. ADRs e traceabilidade

- Registre decisões relevantes em `architecture/adr/ADR-XXXX-<slug>.md`:
  Contexto, Decisão, Consequências, Alternativas, Status, Data.
- Inclua `## Traceability` no `ARCHITECTURE.md` com `| Story | Módulo/feature |`
  usando os IDs `US-XX` da PRD.
- Mantenha consistência entre `PROFILE.md`, `ARCHITECTURE.md`, ADRs e a matriz
  global `traceability/TRACEABILITY.md`.
