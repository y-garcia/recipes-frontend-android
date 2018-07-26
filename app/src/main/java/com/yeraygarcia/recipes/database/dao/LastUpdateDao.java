package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.LastUpdate;

@Dao
public abstract class LastUpdateDao extends BaseDao<LastUpdate> {

    @Query("DELETE FROM last_update")
    public abstract void deleteAll();

    @Query("SELECT last_update FROM last_update WHERE id = 1")
    public abstract LiveData<Long> getLastUpdate();
}
