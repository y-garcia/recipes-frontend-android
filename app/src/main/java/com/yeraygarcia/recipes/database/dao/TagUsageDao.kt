package com.yeraygarcia.recipes.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

import com.yeraygarcia.recipes.database.entity.Tag
import com.yeraygarcia.recipes.database.entity.TagUsage

@Dao
abstract class TagUsageDao : BaseDao<TagUsage>() {

    @get:Query(
        "SELECT t.id, t.name, COUNT(ut.tag_id) AS usage_count, MAX(ut.created) AS last_used " +
                "FROM usage_tag ut " +
                "LEFT OUTER JOIN tag t ON ut.tag_id = t.id " +
                "GROUP BY t.id, t.name"
    )
    abstract val tagsWithUpdatedUsage: List<Tag>

    @Query("DELETE FROM usage_tag")
    abstract fun deleteAll()
}
