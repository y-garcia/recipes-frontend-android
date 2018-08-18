package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.RecipeTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Dao
public abstract class RecipeTagDao extends BaseDao<RecipeTag> {

    @Query("DELETE FROM recipe_tag")
    public abstract void deleteAll();

    @Query("SELECT r.id, r.name, r.portions, r.duration, r.url " +
            "FROM recipe_tag rt " +
            "INNER JOIN recipe r ON rt.recipe_id = r.id " +
            "WHERE rt.tag_id IN (:tagIds) " +
            "GROUP BY r.id, r.name, r.portions, r.duration, r.url " +
            "HAVING count(rt.tag_id) = :tagCount")
    public abstract LiveData<List<Recipe>> findRecipesByAllTagIds(List<UUID> tagIds, int tagCount);

    @Query("SELECT * FROM recipe_tag")
    public abstract LiveData<List<RecipeTag>> findAll();

    @Query("DELETE FROM recipe_tag WHERE id NOT IN (:ids)")
    abstract void deleteIfIdNotIn(List<UUID> ids);

    public void deleteIfNotIn(List<RecipeTag> entities) {
        List<UUID> ids = new ArrayList<>();
        for (RecipeTag entity : entities) {
            ids.add(entity.getId());
        }
        deleteIfIdNotIn(ids);
    }
}
