package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.yeraygarcia.recipes.database.entity.Unit;

import java.util.List;

@Dao
public interface UnitDao {

    @Insert
    long[] insert(Unit... units);

    @Update
    int update(Unit... units);

    @Delete
    int delete(Unit... units);

    @Query("DELETE FROM unit")
    void deleteAll();

    @Query("SELECT * from unit ORDER BY name_singular ASC")
    LiveData<List<Unit>> findAll();

    @Query("SELECT * from unit WHERE id = :id")
    LiveData<Unit> findById(long id);

}
