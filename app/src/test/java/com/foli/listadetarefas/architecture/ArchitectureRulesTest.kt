package com.foli.listadetarefas.architecture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Regras puras (sem I/O) do gate de arquitetura. Ficam em um `object` separado
 * para serem exercitadas por testes próprios: se uma regra for enfraquecida, o
 * caso sintético que a cobre falha antes de o gate chegar em produção.
 */
internal object ArchitectureRules {

    private val visibleTextSink = Regex(
        """(?:\b(?:Text|BasicText)\s*\(|contentDescription\s*=|supportingText\s*=|\blabel\s*=)""",
    )

    private val stringLiteral = Regex("(?<!\")" + "\"[^\"]")

    private val systemClockApi = Regex(
        """(?:(?:LocalDate|LocalDateTime|LocalTime|Instant|OffsetDateTime|ZonedDateTime|Clock)\.now\s*\()""" +
            """|(?:Clock\.system[A-Za-z]*\s*\()""" +
            """|(?:System\.(?:currentTimeMillis|nanoTime)\s*\()""" +
            """|(?:SystemClock\.)""",
    )

    /** Literais de texto visível em qualquer posição de argumento dos sinks do Compose. */
    fun hardcodedVisibleText(source: String): List<String> {
        val violations = mutableListOf<String>()
        val lines = source.lines()
        var index = 0
        while (index < lines.size) {
            if (visibleTextSink.containsMatchIn(lines[index])) {
                val statement = StringBuilder(lines[index])
                var balance = parenthesisBalance(lines[index])
                var end = index
                while (balance > 0 && end + 1 < lines.size) {
                    end++
                    statement.append('\n').append(lines[end])
                    balance += parenthesisBalance(lines[end])
                }
                if (stringLiteral.containsMatchIn(statement)) {
                    violations += "linha ${index + 1}: ${statement.toString().replace('\n', ' ')}"
                }
                index = end + 1
            } else {
                index++
            }
        }
        return violations
    }

    /** Leituras de relógio do sistema, em qualquer API conhecida. */
    fun systemClockReads(source: String): List<String> =
        systemClockApi.findAll(source).map { it.value.trim() }.toList()

    private fun parenthesisBalance(line: String): Int {
        var balance = 0
        var inString = false
        var escaped = false
        for (character in line) {
            when {
                escaped -> escaped = false
                inString && character == '\\' -> escaped = true
                character == '"' -> inString = !inString
                !inString && character == '(' -> balance++
                !inString && character == ')' -> balance--
            }
        }
        return balance
    }
}

/**
 * Gate de arquitetura obrigatório por tarefa (`ARCHITECTURE.md` §2, §12).
 *
 * Lê as fontes de produção e falha o `testDebugUnitTest` quando uma fronteira de
 * camada é violada, quando alguém fora de `SystemTimeProvider` consulta o
 * relógio do sistema, quando há texto visível hardcoded ou quando o manifest/
 * tema de janela deixa de estar endurecido.
 */
class ArchitectureRulesTest {

    private val moduleDir: File = findModuleDir()

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private fun findModuleDir(): File {
        val candidates = listOf(File("."), File("app"), File("..").resolve("app"))
        return candidates.firstOrNull { File(it, "src/main/java").isDirectory }
            ?: error("Não foi possível localizar app/src/main/java a partir de ${File(".").absolutePath}")
    }

    private fun mainSources(): File = moduleDir.resolve("src/main/java")

    private fun filesIn(relativePath: String): List<File> {
        val dir = mainSources().resolve(relativePath)
        if (!dir.isDirectory) return emptyList()
        return dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }

    private fun packageOf(file: File): String = file.readLines()
        .firstOrNull { it.trimStart().startsWith("package ") }
        ?.trim()
        ?.removePrefix("package ")
        ?.removeSuffix(";")
        ?.trim()
        ?: ""

    private fun importsOf(file: File): List<String> = file.readLines()
        .map { it.trim() }
        .filter { it.startsWith("import ") }
        .map { it.removePrefix("import ").substringBefore(" as ").trim() }

    private fun assertNoImport(
        files: List<File>,
        forbidden: (String) -> Boolean,
        rule: String,
    ) {
        val violations = files.flatMap { file ->
            importsOf(file)
                .filter(forbidden)
                .map { "${file.relativeTo(mainSources()).path} -> $it" }
        }
        assertTrue("$rule\nViolações:\n${violations.joinToString("\n")}", violations.isEmpty())
    }

    // ------------------------------------------------------------------
    // Fronteiras de camada
    // ------------------------------------------------------------------

    @Test
    fun feature_doesNotImportDataDiOrAnotherFeature() {
        val files = filesIn("com/foli/listadetarefas/ui/feature")
        val violations = files.flatMap { file ->
            val ownFeature = packageOf(file)
                .removePrefix("com.foli.listadetarefas.ui.feature.")
                .substringBefore('.')
            importsOf(file)
                .filter { import ->
                    val sameFeature = import.startsWith("com.foli.listadetarefas.ui.feature.") &&
                        import.removePrefix("com.foli.listadetarefas.ui.feature.").substringBefore('.') != ownFeature
                    import.startsWith("com.foli.listadetarefas.data") ||
                        import.startsWith("com.foli.listadetarefas.di") ||
                        sameFeature
                }
                .map { "${file.name} -> $it" }
        }
        assertTrue(
            "ui.feature.* não pode importar data.*, di.* nem outra ui.feature.*\n" +
                violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }

    @Test
    fun designsystem_doesNotImportDomainDataOrFeature() {
        assertNoImport(
            files = filesIn("com/foli/listadetarefas/ui/designsystem"),
            forbidden = {
                it.startsWith("com.foli.listadetarefas.domain") ||
                    it.startsWith("com.foli.listadetarefas.data") ||
                    it.startsWith("com.foli.listadetarefas.ui.feature")
            },
            rule = "ui.designsystem.* não pode importar domain.*, data.* nem ui.feature.*",
        )
    }

    @Test
    fun uiModel_doesNotImportDomainDataDiOrCompose() {
        assertNoImport(
            files = filesIn("com/foli/listadetarefas/ui/model"),
            forbidden = {
                it.startsWith("com.foli.listadetarefas.domain") ||
                    it.startsWith("com.foli.listadetarefas.data") ||
                    it.startsWith("com.foli.listadetarefas.di") ||
                    it.startsWith("androidx.compose")
            },
            rule = "ui.model.* não pode importar domain/data/di nem Compose",
        )
    }

    @Test
    fun domain_hasNoAndroidOrFrameworkImports() {
        assertNoImport(
            files = filesIn("com/foli/listadetarefas/domain"),
            forbidden = {
                it.startsWith("android.") ||
                    it.startsWith("androidx.") ||
                    it.startsWith("retrofit") ||
                    it.startsWith("kotlinx.serialization") ||
                    it.startsWith("androidx.room")
            },
            rule = "domain.* é Kotlin puro (sem android/androidx/room/retrofit/serialization)",
        )
    }

    @Test
    fun data_doesNotImportUi() {
        assertNoImport(
            files = filesIn("com/foli/listadetarefas/data"),
            forbidden = { it.startsWith("com.foli.listadetarefas.ui") },
            rule = "data.* não pode importar ui.*",
        )
    }

    @Test
    fun core_doesNotImportDomainDataOrUi() {
        assertNoImport(
            files = filesIn("com/foli/listadetarefas/core"),
            forbidden = {
                it.startsWith("com.foli.listadetarefas.domain") ||
                    it.startsWith("com.foli.listadetarefas.data") ||
                    it.startsWith("com.foli.listadetarefas.ui")
            },
            rule = "core.* não pode importar domain/data/ui",
        )
    }

    // ------------------------------------------------------------------
    // Tempo, textos e tema
    // ------------------------------------------------------------------

    @Test
    fun onlySystemTimeProviderReadsTheSystemClock() {
        val offenders = filesIn("com/foli/listadetarefas")
            .filter { it.name != "SystemTimeProvider.kt" }
            .flatMap { file ->
                ArchitectureRules.systemClockReads(file.readText())
                    .map { "${file.relativeTo(mainSources()).path} -> $it" }
            }
        assertTrue(
            "Somente core/time/SystemTimeProvider.kt pode consultar o relógio do sistema " +
                "(LocalDate.now, Instant.now, Clock.system, System.currentTimeMillis...).\n" +
                offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }

    @Test
    fun composables_haveNoHardcodedVisibleText() {
        val offenders = filesIn("com/foli/listadetarefas")
            .flatMap { file ->
                ArchitectureRules.hardcodedVisibleText(file.readText())
                    .map { "${file.relativeTo(mainSources()).path} -> $it" }
            }
        assertTrue(
            "Texto visível deve vir de stringResource, nunca literal " +
                "(Text/BasicText, contentDescription, supportingText, label).\n" +
                offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }

    @Test
    fun theme_disablesDynamicColor() {
        val theme = mainSources().resolve("com/foli/listadetarefas/ui/theme/Theme.kt")
        if (!theme.exists()) return
        val content = theme.readText()
        assertFalse(
            "Theme.kt não pode usar dynamic color (COLOR_SCHEME.md §10).",
            content.contains("dynamicLightColorScheme") || content.contains("dynamicDarkColorScheme"),
        )
    }

    @Test
    fun windowTheme_isAlignedWithAppSurface() {
        val themes = moduleDir.resolve("src/main/res/values/themes.xml")
        assertTrue("themes.xml ausente", themes.exists())
        assertTrue(
            "O tema de janela deve usar @color/window_background (COLOR_SCHEME.md §4/§5).",
            themes.readText().contains("@color/window_background"),
        )

        val colors = moduleDir.resolve("src/main/res/values/colors.xml")
        assertTrue("colors.xml ausente", colors.exists())
        val colorContent = colors.readText()
        assertFalse(
            "Cores do template devem ser removidas do colors.xml.",
            colorContent.contains("purple_") || colorContent.contains("teal_"),
        )
        assertTrue(
            "values-night/colors.xml deve sobrescrever o fundo da janela.",
            moduleDir.resolve("src/main/res/values-night/colors.xml").exists(),
        )
    }

    // ------------------------------------------------------------------
    // Manifest e strings
    // ------------------------------------------------------------------

    @Test
    fun manifest_isHardenedAndHasNoBackup() {
        val manifest = moduleDir.resolve("src/main/AndroidManifest.xml")
        assertTrue("AndroidManifest.xml ausente", manifest.exists())
        val content = manifest.readText()

        assertFalse("Manifest não pode declarar permissões.", content.contains("<uses-permission"))
        assertTrue("Manifest exige allowBackup=\"false\".", content.contains("android:allowBackup=\"false\""))
        assertFalse("Manifest não pode ter dataExtractionRules.", content.contains("dataExtractionRules"))
        assertFalse("Manifest não pode ter fullBackupContent.", content.contains("fullBackupContent"))
        assertTrue("MainActivity deve ser exported=true.", content.contains("android:exported=\"true\""))

        assertFalse(
            "res/xml/backup_rules.xml deve ser deletado.",
            moduleDir.resolve("src/main/res/xml/backup_rules.xml").exists(),
        )
        assertFalse(
            "res/xml/data_extraction_rules.xml deve ser deletado.",
            moduleDir.resolve("src/main/res/xml/data_extraction_rules.xml").exists(),
        )
    }

    @Test
    fun strings_containUs01KeySetWithoutLaterStoryKeys() {
        val strings = moduleDir.resolve("src/main/res/values/strings.xml")
        assertTrue("strings.xml ausente", strings.exists())
        val content = strings.readText()

        val required = setOf(
            "app_name", "task_list_title", "new_task", "edit_task",
            "save", "saving", "cancel", "delete", "menu_edit", "retry",
            "delete_task_title", "delete_task_body",
            "task_title_label", "field_required", "task_title_required",
            "task_description_label_optional",
            "empty_active_title", "empty_active_body",
            "storage_error_message", "storage_error_inline",
            "storage_delete_error_message", "task_load_error",
            "a11y_more_options", "a11y_loading_tasks", "a11y_edit_task",
            "a11y_task", "a11y_back",
        )
        val declared = Regex("""<string\s+name="([^"]+)"""")
            .findAll(content)
            .map { it.groupValues[1] }
            .toSet()

        val missing = required - declared
        assertTrue("Strings da US-01 ausentes: $missing", missing.isEmpty())

        val laterStories = setOf(
            "tab_active", "tab_completed", "search_placeholder", "sort_label",
            "sort_sheet_title", "counter_format", "label_none", "labels_title",
            "deadline_section", "completed_on", "a11y_checkbox_completed",
        )
        val leaked = laterStories intersect declared
        assertTrue("Strings de histórias posteriores não pertencem à US-01: $leaked", leaked.isEmpty())
    }

    // ------------------------------------------------------------------
    // Auto-verificação das regras (falham se o gate for enfraquecido)
    // ------------------------------------------------------------------

    @Test
    fun hardcodedTextRule_detectsLiteralInEveryTextPosition() {
        val samples = mapOf(
            "Text(\"Olá\")" to true,
            "Text(text = \"Olá\")" to true,
            "Modifier.semantics { contentDescription = \"Olá\" }" to true,
            "BasicText(\n    text = \"Olá\",\n)" to true,
            "label = \"Olá\"" to true,
            "supportingText = \"Olá\"" to true,
            "Text(stringResource(R.string.app_name))" to false,
            "Text(text = greeting)" to false,
            "Text(\"\" )" to false,
        )
        samples.forEach { (source, shouldDetect) ->
            val detected = ArchitectureRules.hardcodedVisibleText(source).isNotEmpty()
            assertTrue("Detecção incorreta para: $source", detected == shouldDetect)
        }
    }

    @Test
    fun clockRule_detectsEverySystemClockApi() {
        val detected = ArchitectureRules.systemClockReads(
            """
            val a = LocalDate.now()
            val b = LocalDateTime.now()
            val c = Instant.now()
            val d = Clock.systemUTC()
            val e = System.currentTimeMillis()
            val f = System.nanoTime()
            val g = SystemClock.elapsedRealtime()
            """.trimIndent(),
        )
        assertTrue("Esperadas 7 leituras, obtidas: ${detected.size}", detected.size == 7)
        assertTrue(
            "Chamadas ao TimeProvider não podem ser sinalizadas.",
            ArchitectureRules.systemClockReads("val today = timeProvider.today()").isEmpty(),
        )
    }
}
