package fr.jorisfavier.youshallnotpass.model

import android.view.autofill.AutofillId

data class AutofillItem(
    val id: AutofillId,
    val value: String,
    val type: ItemDataType,
)
