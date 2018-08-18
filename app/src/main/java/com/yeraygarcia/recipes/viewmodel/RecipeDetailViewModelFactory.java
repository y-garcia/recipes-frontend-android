package com.yeraygarcia.recipes.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository;

import java.util.UUID;

public class RecipeDetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final RecipeDetailRepository mRepository;
    private final UUID mTaskId;

    public RecipeDetailViewModelFactory(RecipeDetailRepository repository, UUID taskId) {
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
