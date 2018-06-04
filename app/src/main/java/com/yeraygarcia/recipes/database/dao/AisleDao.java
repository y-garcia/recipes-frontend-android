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
public interface AisleDao {

    @Insert
    long[] insert(Aisle... aisles);

    @Update
    int update(Aisle... aisles);

    @Delete
    int delete(Aisle... aisles);

    @Query("DELETE FROM aisle")
    void deleteAll();

    @Query("SELECT * from aisle ORDER BY name ASC")
    LiveData<List<Aisle>> findAll();

    @Query("SELECT * from aisle WHERE id = :id")
    LiveData<Aisle> findById(long id);

}
