package com.jdmobile.inflightsalesapp.data.local.product

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category")
    fun getProductsByCategory(category: Int): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("UPDATE products SET unit = :newStock WHERE id = :id")
    suspend fun updateProductStock(id: Int, newStock: Int)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}