package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.Recipe
import com.yeraygarcia.recipes.database.entity.RecipeTag
import java.util.*

@Dao
abstract class RecipeTagDao : BaseDao<RecipeTag>() {

    @Query("DELETE FROM recipe_tag")
    abstract fun deleteAll()

    @Query(
        "SELECT r.id, r.name, r.portions, r.duration, r.url " +
                "FROM recipe_tag rt " +
                "INNER JOIN recipe r ON rt.recipe_id = r.id " +
                "WHERE rt.tag_id IN (:tagIds) " +
                "GROUP BY r.id, r.name, r.portions, r.duration, r.url " +
                "HAVING count(rt.tag_id) = :tagCount"
    )
    abstract fun findRecipesByAllTagIds(tagIds: List<UUID>, tagCount: Int): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipe_tag")
    abstract fun findAll(): LiveData<List<RecipeTag>>

    @Query("DELETE FROM recipe_tag WHERE id NOT IN (:ids)")
    internal abstract fun deleteIfIdNotIn(ids: List<UUID>)

    fun deleteIfNotIn(entities: List<RecipeTag>) {
        val ids = ArrayList<UUID>()
        for (entity in entities) {
            ids.add(entity.id)
        }
        deleteIfIdNotIn(ids)
    }
}
