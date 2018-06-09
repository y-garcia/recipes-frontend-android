package com.yeraygarcia.recipes.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository;

public class RecipeDetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final RecipeDetailRepository mRepository;
    private final long mTaskId;

    public RecipeDetailViewModelFactory(RecipeDetailRepository repository, long taskId) {
        mRepository = repository;
        mTaskId = taskId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new RecipeDetailViewModel(mRepository, mTaskId);
    }
}
