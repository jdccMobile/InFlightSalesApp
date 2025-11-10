package com.jdmobile.inflightsalesapp.data.remote.product

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("id")val id: Int,
    @SerializedName("name")val name: String,
    @SerializedName("unit")val unit: Int,
    @SerializedName("priceUSD")val priceUSD: Double,
    @SerializedName("priceEUR")val priceEUR: Double,
    @SerializedName("priceGBP")val priceGBP: Double,
    @SerializedName("imageUrl")val imageUrl: String,
    @SerializedName("category")val category: Int
)