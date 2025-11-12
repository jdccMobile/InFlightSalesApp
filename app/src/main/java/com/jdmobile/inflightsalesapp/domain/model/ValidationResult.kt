package com.jdmobile.inflightsalesapp.domain.model

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)