package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.yeraygarcia.recipes.database.entity.Ingredient;

import java.util.List;

@Dao
public interface IngredientDao {

    @Insert
    long[] insert(Ingredient... ingredients);

    @Update
    int update(Ingredient... ingredients);

    @Delete
    int delete(Ingredient... ingredients);

    @Query("DELETE FROM ingredient")
    void deleteAll();

    @Query("SELECT * from ingredient ORDER BY name ASC")
    LiveData<List<Ingredient>> findAll();

    @Query("SELECT * from ingredient WHERE id = :id")
    LiveData<Ingredient> findById(long id);

}
