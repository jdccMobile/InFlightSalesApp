package com.jdmobile.inflightsalesapp.data.local.product

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val unit: Int,
    val priceUSD: Double,
    val priceEUR: Double,
    val priceGBP: Double,
    val imageUrl: String,
    val category: Int
)