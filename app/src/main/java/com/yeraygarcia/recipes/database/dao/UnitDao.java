package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.Unit;

import java.util.List;

@Dao
public abstract class UnitDao implements BaseDao<Unit> {

    @Query("DELETE FROM unit")
    public abstract void deleteAll();

    @Query("SELECT * from unit ORDER BY name_singular ASC")
    abstract LiveData<List<Unit>> findAll();

    @Query("SELECT * from unit WHERE id = :id")
    abstract LiveData<Unit> findById(long id);

}
