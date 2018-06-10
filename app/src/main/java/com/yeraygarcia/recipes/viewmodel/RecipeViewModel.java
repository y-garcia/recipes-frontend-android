package com.yeraygarcia.recipes.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;

import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.Tag;
import com.yeraygarcia.recipes.database.repository.RecipeRepository;
import com.yeraygarcia.recipes.util.Debug;

import java.util.ArrayList;
import java.util.List;

public class RecipeViewModel extends AndroidViewModel {

    private RecipeRepository mRepository;

    private final MutableLiveData<List<Long>> mTagFilter = new MutableLiveData<>();
    private final LiveData<List<Recipe>> mRecipes = Transformations.switchMap(mTagFilter, tagIds -> mRepository.getRecipesByTagId(tagIds));
    private final LiveData<List<Tag>> mTags;


    public RecipeViewModel(Application application) {
        super(application);
        Debug.d(this, "RecipeViewModel(application)");
        mRepository = new RecipeRepository(application);
        //mRecipes = mRepository.getRecipes();
        mTags = mRepository.getTags();
        mTagFilter.setValue(new ArrayList<>());
    }

    public LiveData<List<Recipe>> getRecipes() {
        Debug.d(this, "getRecipes()");
        return mRecipes;
    }

    public LiveData<List<Tag>> getTags() {
        Debug.d(this, "getTags()");
        return mTags;
    }

    public MutableLiveData<List<Long>> getTagFilter() {
        Debug.d(this, "getTagFilter()");
        return mTagFilter;
    }

    public long[] getTagFilterAsArray() {
        Debug.d(this, "getTagFilterAsArray()");
        List<Long> tagFilter = mTagFilter.getValue();
        if(tagFilter == null) {
            return new long[]{};
        }
        long[] result = new long[tagFilter.size()];

        for (int i = 0; i < tagFilter.size(); i++) {
            result[i] = tagFilter.get(i);
        }
        return result;
    }

    public void addTagToFilter(Tag tag) {
        Debug.d(this, "addTagToFilter("+tag.getId()+")");
        List<Long> newTags = mTagFilter.getValue();
        if (newTags == null) {
            newTags = new ArrayList<>();
        }
        newTags.add(tag.getId());
        mTagFilter.setValue(newTags);
        mRepository.logTagUsage(tag.getId());
    }

    public void removeTagFromFilter(Tag tag) {
        Debug.d(this, "removeTagFromFilter("+tag.getId()+")");
        List<Long> newTags = mTagFilter.getValue();
        if (newTags == null) {
            return;
        }
        newTags.remove(tag.getId());
        mTagFilter.setValue(newTags);
    }

    public void setTagFilter(long[] tagFilter) {
        Debug.d(this, "setTagFilter(tagFilter)");
        List<Long> tagFilterList = new ArrayList<>();
        for (long tagId : tagFilter){
            tagFilterList.add(tagId);
        }
        mTagFilter.setValue(tagFilterList);
    }

    public void updateTagUsage() {
        Debug.d(this, "updateTagUsage()");
        mRepository.updateTagUsage();
    }
}
