package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.RecipeTag;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class RecipeTagDao extends BaseDao<RecipeTag> {

    @Query("DELETE FROM recipe_tag")
    public abstract void deleteAll();

    /*
        @Query("SELECT r.* " +
                "FROM recipe_tag rt " +
                "INNER JOIN recipe r ON rt.recipe_id = r.id " +
                "WHERE rt.tag_id IN (:tagIds) ")
        public abstract LiveData<List<Recipe>> findRecipesByAnyTagId(List<Long> tagIds);
    */
    @Query("SELECT r.id, r.name, r.portions, r.duration, r.url " +
            "FROM recipe_tag rt " +
            "INNER JOIN recipe r ON rt.recipe_id = r.id " +
            "WHERE rt.tag_id IN (:tagIds) " +
            "GROUP BY r.id, r.name, r.portions, r.duration, r.url " +
            "HAVING count(rt.tag_id) = :tagCount")
    public abstract LiveData<List<Recipe>> findRecipesByAllTagIds(List<Long> tagIds, int tagCount);

    @Query("SELECT * FROM recipe_tag")
    public abstract LiveData<List<RecipeTag>> findAll();

    @Query("DELETE FROM recipe_tag WHERE id NOT IN (:ids)")
    abstract void deleteIfIdNotIn(List<Long> ids);

    public void deleteIfNotIn(List<RecipeTag> entities) {
        List<Long> ids = new ArrayList<>();
        for (RecipeTag entity : entities) {
            ids.add(entity.getId());
        }
        deleteIfIdNotIn(ids);
    }
}
