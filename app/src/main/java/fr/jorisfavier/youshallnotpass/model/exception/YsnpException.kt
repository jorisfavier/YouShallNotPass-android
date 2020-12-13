package fr.jorisfavier.youshallnotpass.model.exception

import androidx.annotation.StringRes

class YsnpException(@StringRes val messageResId: Int) : Exception()