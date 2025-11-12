package com.jdmobile.inflightsalesapp.domain.model

enum class CustomerType(val discount: Double) {
    RETAIL( 1.0),
    CREW( 0.85),
    HAPPY_HOUR(0.80),
    BUSINESS_INVITATION(0.70),
    TOURIST_INVITATION(0.75),
}