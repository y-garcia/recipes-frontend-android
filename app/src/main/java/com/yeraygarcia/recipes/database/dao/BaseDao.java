package com.yeraygarcia.recipes.database.dao;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

abstract class BaseDao<T> {

    @Insert
    public abstract long[] insert(T... entities);

    @Insert(onConflict = REPLACE)
    public abstract long[] save(T... entities);

    @Update
    public abstract int update(T... entities);

    @Delete
    public abstract int delete(T... entities);

    @Insert(onConflict = IGNORE)
    public abstract List<Long> insert(List<T> entities);

    @Update
    public abstract void update(List<T> entities);

    @Transaction
    public void upsert(List<T> entities) {
        List<Long> insertResult = insert(entities);
        List<T> updateList = new ArrayList<>();

        for (int i = 0; i < insertResult.size(); i++) {
            if (insertResult.get(i) == -1) {
                updateList.add(entities.get(i));
            }
        }

        if (!updateList.isEmpty()) {
            update(updateList);
        }
    }

}
