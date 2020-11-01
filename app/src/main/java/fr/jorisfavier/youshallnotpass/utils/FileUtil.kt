package fr.jorisfavier.youshallnotpass.utils

object FileUtil {

    const val CSS = "text/css"
    const val CSV = "text/csv"
    const val CSV_2 = "text/comma-separated-values"
    const val HTML = "text/html"
    const val PLAIN = "text/plain"
    const val RICH_TEXT = "text/richtext"
    const val SGML = "text/sgml"
    const val YAML = "text/yaml"

    private val textMimeTypes = setOf(CSS, CSV, HTML, PLAIN, RICH_TEXT, SGML, YAML, CSV_2)


    fun isTextFile(mimeType: String?) = textMimeTypes.contains(mimeType)

}