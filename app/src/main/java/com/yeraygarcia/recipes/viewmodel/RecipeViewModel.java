package com.yeraygarcia.recipes.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.repository.RecipeRepository;

import java.util.List;

public class RecipeViewModel extends AndroidViewModel {

    private RecipeRepository mRepository;
    private LiveData<List<Recipe>> mAllRecipes;
    private MutableLiveData<Recipe> selected = new MutableLiveData<>();

    public RecipeViewModel(Application application) {
        super(application);
        mRepository = new RecipeRepository(application);
        mAllRecipes = mRepository.getAllRecipes();
    }

    public LiveData<List<Recipe>> getAllRecipes() {
        return mAllRecipes;
    }

    public MutableLiveData<Recipe> getSelected() {
        return selected;
    }

    public void setSelected(Recipe recipe) {
        this.selected.setValue(recipe);
    }

    public LiveData<Recipe> getRecipeById(long id) {
        return mRepository.getRecipeById(id);
    }

    public void insert(Recipe recipe) {
        mRepository.insert(recipe);
    }
}
