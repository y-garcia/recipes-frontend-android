package com.yeraygarcia.recipes.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.yeraygarcia.recipes.AppExecutors
import com.yeraygarcia.recipes.database.dao.*
import com.yeraygarcia.recipes.database.entity.*
import com.yeraygarcia.recipes.database.entity.Unit

@Database(
    entities = [
        Aisle::class, Store::class, Tag::class, Unit::class, Ingredient::class, Placement::class,
        Recipe::class, RecipeIngredient::class, RecipeStep::class, RecipeTag::class, TagUsage::class,
        ShoppingListItem::class, LastUpdate::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(UUIDTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val aisleDao: AisleDao
    abstract val tagDao: TagDao
    abstract val unitDao: UnitDao
    abstract val ingredientDao: IngredientDao
    abstract val recipeDao: RecipeDao
    abstract val recipeIngredientDao: RecipeIngredientDao
    abstract val recipeStepDao: RecipeStepDao
    abstract val recipeTagDao: RecipeTagDao
    abstract val tagUsageDao: TagUsageDao
    abstract val shoppingListDao: ShoppingListDao
    abstract val lastUpdateDao: LastUpdateDao

    companion object {

        private const val DATABASE_NAME = "recipes"
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE
                ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    AppDatabase.DATABASE_NAME
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            AppExecutors().diskIO {
                                getDatabase(context).lastUpdateDao.upsert(LastUpdate(0L))
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }
}
