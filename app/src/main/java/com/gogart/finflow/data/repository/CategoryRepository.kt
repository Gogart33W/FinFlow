package com.gogart.finflow.data.repository

import com.gogart.finflow.data.local.dao.CategoryDao
import com.gogart.finflow.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val dao: CategoryDao) {
    fun getAllCategories(): Flow<List<CategoryEntity>> = dao.getAllCategories()

    fun getCategoriesByType(isIncome: Boolean): Flow<List<CategoryEntity>> =
        dao.getCategoriesByType(isIncome)

    suspend fun insertCategory(category: CategoryEntity): Long = dao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) = dao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) = dao.deleteCategory(category)
}
