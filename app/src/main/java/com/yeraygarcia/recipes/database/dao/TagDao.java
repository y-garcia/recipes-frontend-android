package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.Tag;

import java.util.List;

@Dao
public abstract class TagDao extends BaseDao<Tag> {

    @Query("DELETE FROM tag")
    public abstract void deleteAll();

    @Query("SELECT * FROM tag ORDER BY usage_count DESC, name ASC")
    public abstract LiveData<List<Tag>> findAll();

    @Query("SELECT count(1) = 0 AS is_empty FROM tag")
    public abstract boolean isEmpty();
}
