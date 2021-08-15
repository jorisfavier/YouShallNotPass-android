package fr.jorisfavier.youshallnotpass.ui.autofill

import android.service.autofill.FillRequest
import fr.jorisfavier.youshallnotpass.model.AutofillItem
import fr.jorisfavier.youshallnotpass.model.Item

data class AutofillDataSetInfo(
    val autofillItems: List<AutofillItem>,
    val item: Item,
    val itemPassword: String,
    val fillRequest: FillRequest,
)