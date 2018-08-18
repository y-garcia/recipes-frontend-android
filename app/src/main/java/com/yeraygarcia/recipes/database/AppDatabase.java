package com.yeraygarcia.recipes.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.yeraygarcia.recipes.database.dao.AisleDao;
import com.yeraygarcia.recipes.database.dao.IngredientDao;
import com.yeraygarcia.recipes.database.dao.LastUpdateDao;
import com.yeraygarcia.recipes.database.dao.RecipeDao;
import com.yeraygarcia.recipes.database.dao.RecipeDetailDao;
import com.yeraygarcia.recipes.database.dao.RecipeIngredientDao;
import com.yeraygarcia.recipes.database.dao.RecipeStepDao;
import com.yeraygarcia.recipes.database.dao.RecipeTagDao;
import com.yeraygarcia.recipes.database.dao.ShoppingListDao;
import com.yeraygarcia.recipes.database.dao.TagDao;
import com.yeraygarcia.recipes.database.dao.TagUsageDao;
import com.yeraygarcia.recipes.database.dao.UnitDao;
import com.yeraygarcia.recipes.database.entity.Aisle;
import com.yeraygarcia.recipes.database.entity.Ingredient;
import com.yeraygarcia.recipes.database.entity.LastUpdate;
import com.yeraygarcia.recipes.database.entity.Placement;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.RecipeIngredient;
import com.yeraygarcia.recipes.database.entity.RecipeStep;
import com.yeraygarcia.recipes.database.entity.RecipeTag;
import com.yeraygarcia.recipes.database.entity.ShoppingListItem;
import com.yeraygarcia.recipes.database.entity.Store;
import com.yeraygarcia.recipes.database.entity.Tag;
import com.yeraygarcia.recipes.database.entity.TagUsage;
import com.yeraygarcia.recipes.database.entity.Unit;
import com.yeraygarcia.recipes.util.Debug;

@Database(
        entities = {
                Aisle.class, Store.class, Tag.class, Unit.class, Ingredient.class, Placement.class,
                Recipe.class, RecipeIngredient.class, RecipeStep.class, RecipeTag.class,
                TagUsage.class, ShoppingListItem.class, LastUpdate.class
        },
        version = 2,
        exportSchema = false
)
@TypeConverters({UUIDTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "recipes";
    private static AppDatabase sInstance;
    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    new PopulateDbAsync(sInstance).execute();
                }
            };

    public static AppDatabase getDatabase(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Debug.d(TAG, "getDatabase(context) -> new instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .addCallback(sRoomDatabaseCallback)
                        .fallbackToDestructiveMigration()
                        .build();
            }
        }
        Debug.d(TAG, "getDatabase(context) -> get existing instance");
        return sInstance;
    }

    public abstract AisleDao getAisleDao();

    public abstract TagDao getTagDao();

    public abstract UnitDao getUnitDao();

    public abstract IngredientDao getIngredientDao();

    public abstract RecipeDao getRecipeDao();

    public abstract RecipeIngredientDao getRecipeIngredientDao();

    public abstract RecipeStepDao getRecipeStepDao();

    public abstract RecipeDetailDao getRecipeDetailDao();

    public abstract RecipeTagDao getRecipeTagDao();

    public abstract TagUsageDao getTagUsageDao();

    public abstract ShoppingListDao getShoppingListDao();

    public abstract LastUpdateDao getLastUpdateDao();

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final LastUpdateDao mLastUpdateDao;

        PopulateDbAsync(AppDatabase db) {
            mLastUpdateDao = db.getLastUpdateDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {

            if (mLastUpdateDao.getLastUpdate() == null) {
                mLastUpdateDao.insert(new LastUpdate(0L));
            }

            return null;
        }
    }
}
