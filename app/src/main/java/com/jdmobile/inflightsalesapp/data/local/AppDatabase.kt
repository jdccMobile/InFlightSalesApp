package com.jdmobile.inflightsalesapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jdmobile.inflightsalesapp.data.local.product.ProductDao
import com.jdmobile.inflightsalesapp.data.local.product.ProductEntity

@Database(entities = [ProductEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getProductDao(): ProductDao
}
