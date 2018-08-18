package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Dao
public abstract class TagDao extends BaseDao<Tag> {

    @Query("DELETE FROM tag")
    public abstract void deleteAll();

    @Query("SELECT * FROM tag ORDER BY usage_count DESC, name COLLATE NOCASE ASC")
    public abstract LiveData<List<Tag>> findAll();

    @Query("DELETE FROM tag WHERE id NOT IN (:ids)")
    abstract void deleteIfIdNotIn(List<UUID> ids);

    public void deleteIfNotIn(List<Tag> entities) {
        List<UUID> ids = new ArrayList<>();
        for (Tag entity : entities) {
            ids.add(entity.getId());
        }
        deleteIfIdNotIn(ids);
    }
}
