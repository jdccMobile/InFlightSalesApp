package com.jdmobile.inflightsalesapp.domain.model

data class CardData(
    val number: String = "",
    val expirationDate: String = "",
    val cvv: String = "",
    val holderName: String = ""
) {
    fun isComplete(): Boolean {
        return number.isNotBlank() &&
                expirationDate.isNotBlank() &&
                cvv.isNotBlank() &&
                holderName.isNotBlank()
    }
}