package fr.jorisfavier.youshallnotpass.model

import android.view.autofill.AutofillId

data class AutofillParsedStructure(
    val webDomain: String?,
    val appName: String?,
    val certificatesHashes: List<String>,
    val items: List<AutofillItem>,
    val ignoreIds: List<AutofillId>,
)