package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.yeraygarcia.recipes.database.entity.Aisle;

import java.util.List;

@Dao
public abstract class AisleDao implements BaseDao<Aisle> {

    @Query("DELETE FROM aisle")
    public abstract void deleteAll();

    @Query("SELECT * from aisle ORDER BY name ASC")
    abstract LiveData<List<Aisle>> findAll();

    @Query("SELECT * from aisle WHERE id = :id")
    abstract LiveData<Aisle> findById(long id);

}
