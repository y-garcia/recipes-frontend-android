package com.yeraygarcia.recipes.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.yeraygarcia.recipes.database.AppDatabase;

public class RecipeDetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppDatabase mDb;
    private final long mTaskId;

    public RecipeDetailViewModelFactory(AppDatabase database, long taskId) {
        mDb = database;
        mTaskId = taskId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new RecipeDetailViewModel(mDb, mTaskId);
    }
}
