package com.yeraygarcia.recipes.database.dao;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

abstract class BaseDao<T> {

    @Delete
    public abstract int delete(T entity);

    @Delete
    public abstract int delete(List<T> entities);

    @Insert
    public abstract long insert(T entity);

    @Insert
    public abstract List<Long> insert(List<T> entities);

    @Insert(onConflict = IGNORE)
    abstract List<Long> insertOrIgnore(List<T> entities);

    @Update
    public abstract void update(T entity);

    @Update
    public abstract void update(List<T> entities);

    @Transaction
    public void upsert(T entity) {
        upsert(Collections.singletonList(entity));
    }

    @Transaction
    public void upsert(List<T> entities) {
        List<Long> insertResult = insertOrIgnore(entities);
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
