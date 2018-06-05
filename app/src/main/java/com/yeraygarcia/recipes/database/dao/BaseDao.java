package com.yeraygarcia.recipes.database.dao;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Update;

interface BaseDao<T> {

    @Insert
    long[] insert(T... entities);

    @Update
    int update(T... entities);

    @Delete
    int delete(T... entities);

}
