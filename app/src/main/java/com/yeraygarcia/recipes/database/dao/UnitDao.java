package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Dao
public abstract class UnitDao extends BaseDao<Unit> {

    @Query("DELETE FROM unit")
    public abstract void deleteAll();

    @Query("SELECT * FROM unit")
    public abstract LiveData<List<Unit>> findAll();

    @Query("SELECT name_singular FROM unit UNION SELECT name_plural FROM unit")
    public abstract LiveData<List<String>> getUnitNames();

    @Query("DELETE FROM unit WHERE id NOT IN (:ids)")
    abstract void deleteIfIdNotIn(List<UUID> ids);

    public void deleteIfNotIn(List<Unit> entities) {
        List<UUID> ids = new ArrayList<>();
        for (Unit entity : entities) {
            ids.add(entity.getId());
        }
        deleteIfIdNotIn(ids);
    }
}
