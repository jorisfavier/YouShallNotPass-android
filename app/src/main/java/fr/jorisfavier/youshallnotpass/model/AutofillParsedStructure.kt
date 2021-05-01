package fr.jorisfavier.youshallnotpass.model

data class AutofillParsedStructure(
    val webDomain: String?,
    val appName: String?,
    val certificatesHashes: List<String>,
    val items: List<AutofillItem>,
)