package com.yeraygarcia.recipes.database.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.yeraygarcia.recipes.database.AppDatabase;
import com.yeraygarcia.recipes.database.dao.RecipeDao;
import com.yeraygarcia.recipes.database.dao.RecipeTagDao;
import com.yeraygarcia.recipes.database.dao.TagDao;
import com.yeraygarcia.recipes.database.dao.TagUsageDao;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.Tag;
import com.yeraygarcia.recipes.database.entity.TagUsage;

import java.util.List;

public class RecipeRepository {

    private RecipeDao mRecipeDao;
    private RecipeTagDao mRecipeTagDao;
    private TagUsageDao mTagUsageDao;
    private TagDao mTagDao;
    private LiveData<List<Recipe>> mRecipes;
    private LiveData<List<Tag>> mTags;

    public RecipeRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mRecipeDao = db.getRecipeDao();
        mRecipeTagDao = db.getRecipeTagDao();
        mTagUsageDao = db.getTagUsageDao();
        mTagDao = db.getTagDao();

        mRecipes = mRecipeDao.findAll();
        mTags = mTagDao.findAll();
    }

    public LiveData<List<Recipe>> getRecipes() {
        return mRecipes;
    }

    public LiveData<List<Tag>> getTags() {
        return mTags;
    }

    public LiveData<List<Recipe>> getRecipesByTagId(List<Long> tagIds) {
        if (tagIds.size() == 0) {
            return mRecipeDao.findAll();
        }
        return mRecipeTagDao.findRecipesByAllTagId(tagIds, tagIds.size());
    }

    public void insert(Recipe recipe) {
        new RecipeInsertAsyncTask(mRecipeDao).execute(recipe);
    }

    public void logTagUsage(Long tagId) {
        TagUsage tagUsage = new TagUsage(tagId);
        new TagUsageInsertAsyncTask(mTagUsageDao).execute(tagUsage);
    }

    public void updateTagUsage() {
        new TagUpdateAsyncTask(mTagUsageDao, mTagDao).execute();
    }

    private static class RecipeInsertAsyncTask extends AsyncTask<Recipe, Void, Void> {
        private RecipeDao mDao;

        RecipeInsertAsyncTask(RecipeDao dao) {
            mDao = dao;
        }

        @Override
        protected Void doInBackground(final Recipe... params) {
            mDao.insert(params);
            return null;
        }
    }

    private static class TagUsageInsertAsyncTask extends AsyncTask<TagUsage, Void, Void> {
        private TagUsageDao mDao;

        TagUsageInsertAsyncTask(TagUsageDao dao) {
            mDao = dao;
        }

        @Override
        protected Void doInBackground(final TagUsage... params) {
            mDao.insert(params);
            return null;
        }
    }

    private static class TagUpdateAsyncTask extends AsyncTask<Void, Void, Void> {
        private TagUsageDao tagUsageDao;
        private TagDao tagDao;

        TagUpdateAsyncTask(TagUsageDao tagUsageDao, TagDao tagDao) {
            this.tagUsageDao = tagUsageDao;
            this.tagDao = tagDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<Tag> tags = tagUsageDao.getTagsWithUpdatedUsage();
            tagDao.update(tags.toArray(new Tag[]{}));
            return null;
        }
    }
}
