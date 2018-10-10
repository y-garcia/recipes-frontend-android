package com.yeraygarcia.recipes.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository
import java.util.*

class RecipeDetailViewModelFactory(
    private val mRepository: RecipeDetailRepository,
    private val mTaskId: UUID
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RecipeDetailViewModel(mRepository, mTaskId) as T
    }
}
