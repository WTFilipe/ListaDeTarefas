package com.foli.listadetarefas.ui.designsystem.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Ícones locais como `ImageVector` (ADR-0011): o projeto não depende de
 * `material-icons-extended`. Os caminhos são os do Material Symbols e são
 * tingidos pelo `Icon` conforme o `LocalContentColor`.
 */
object AppIcons {

    val Add: ImageVector = icon(
        name = "Add",
        pathData = "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z",
    )

    val MoreVert: ImageVector = icon(
        name = "MoreVert",
        pathData = "M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z",
    )

    val Edit: ImageVector = icon(
        name = "Edit",
        pathData = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z",
    )

    val Delete: ImageVector = icon(
        name = "Delete",
        pathData = "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z",
    )

    val Back: ImageVector = icon(
        name = "Back",
        pathData = "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z",
    )

    val CheckCircle: ImageVector = icon(
        name = "CheckCircle",
        pathData = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z",
    )

    val Check: ImageVector = icon(
        name = "Check",
        pathData = "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z",
    )

    val Tag: ImageVector = icon(
        name = "Tag",
        pathData = "M17.63 5.84C17.27 5.33 16.67 5 16 5H5c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h11c.67 0 1.27-.33 1.63-.84L22 12l-4.37-6.16z",
    )

    val ExpandMore: ImageVector = icon(
        name = "ExpandMore",
        pathData = "M16.59 8.59L12 13.17 7.41 8.59 6 10l6 6 6-6z",
    )

    val Calendar: ImageVector = icon(
        name = "Calendar",
        pathData = "M17 12h-5v5h5v-5zM16 1v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 " +
            "19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-1V1h-2zm3 " +
            "18H5V8h14v11z",
    )

    val Close: ImageVector = icon(
        name = "Close",
        pathData = "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 " +
            "6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z",
    )

    val Warning: ImageVector = icon(
        name = "Warning",
        pathData = "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z",
    )

    val Event: ImageVector = icon(
        name = "Event",
        pathData = "M17 12h-5v5h5v-5zM16 1v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 " +
            "19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-1V1h-2zm3 " +
            "18H5V8h14v11z",
    )

    val Schedule: ImageVector = icon(
        name = "Schedule",
        pathData = "M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 " +
            "17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 " +
            "8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z",
    )

    val Search: ImageVector = icon(
        name = "Search",
        pathData = "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 " +
            "5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 " +
            "4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 " +
            "11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z",
    )

    private fun icon(name: String, pathData: String): ImageVector {
        val nodes = PathParser().parsePathString(pathData).toNodes()
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).addPath(
            pathData = nodes,
            fill = SolidColor(Color.Black),
        ).build()
    }
}
