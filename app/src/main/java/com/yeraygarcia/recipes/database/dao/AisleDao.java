package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.Aisle;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class AisleDao extends BaseDao<Aisle> {

    @Query("DELETE FROM aisle")
    public abstract void deleteAll();

    @Query("SELECT * FROM aisle")
    public abstract LiveData<List<Aisle>> findAll();

    @Query("DELETE FROM aisle WHERE id NOT IN (:ids)")
    abstract void deleteIfIdNotIn(List<Long> ids);

    public void deleteIfNotIn(List<Aisle> entities) {
        List<Long> ids = new ArrayList<>();
        for (Aisle entity : entities) {
            ids.add(entity.getId());
        }
        deleteIfIdNotIn(ids);
    }
}
