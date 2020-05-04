package com.yeraygarcia.recipes.database.remote

import android.content.Context
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import com.yeraygarcia.recipes.AppExecutors
import com.yeraygarcia.recipes.database.AppDatabase
import com.yeraygarcia.recipes.database.dao.DeletedDao.Companion.TABLE_AISLE
import com.yeraygarcia.recipes.database.dao.DeletedDao.Companion.TABLE_INGREDIENT
import com.yeraygarcia.recipes.database.dao.DeletedDao.Companion.TABLE_RECIPE
import com.yeraygarcia.recipes.database.dao.DeletedDao.Companion.TABLE_RECIPE_INGREDIENT
import com.yeraygarcia.recipes.database.dao.DeletedDao.Companion.TABLE_RECIPE_STEP
import com.yeraygarcia.recipes.database.dao.DeletedDao.Companion.TABLE_RECIPE_TAG
import com.yeraygarcia.recipes.database.dao.DeletedDao.Companion.TABLE_TAG
import com.yeraygarcia.recipes.database.dao.DeletedDao.Companion.TABLE_UNIT
import com.yeraygarcia.recipes.database.entity.LastUpdate
import com.yeraygarcia.recipes.database.entity.custom.SyncDto
import com.yeraygarcia.recipes.util.NetworkUtil
import com.yeraygarcia.recipes.util.fromJson
import com.yeraygarcia.recipes.util.toJson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class SyncRequest(val context: Context, val db: AppDatabase, val webservice: Webservice) {

    private fun canFetch(): Boolean {
        return NetworkUtil.isOnline(context)
    }

    fun send() {
        if (!canFetch()) {
            return
        }

        AppExecutors.diskIO {
            val dataToSync = getDataToSync()

            Timber.d("Data to send: ${dataToSync.toJson()}")

            val call = webservice.sync(dataToSync)

            call.enqueue(object : Callback<ResourceData<SyncDto>> {

                override fun onResponse(
                    call: Call<ResourceData<SyncDto>>,
                    response: Response<ResourceData<SyncDto>>
                ) {
                    Timber.d("We got a response from the server: ${response.toJson()}")
                    processResponse(response)
                }

                override fun onFailure(call: Call<ResourceData<SyncDto>>, t: Throwable) {
                    Timber.d("Call failed: ${t.message}")
                    onError(errorMessage = "${t.message}")
                }
            })
        }
    }

    private fun processResponse(response: Response<ResourceData<SyncDto>>) {
        response.apply {
            if (isSuccessful) {
                Timber.d("Response was successful: ${code()}")
                onSuccess(body()?.result)
            } else {
                val body = errorBody()?.string()
                val error = body?.fromJson(ResourceData::class.java)
                val code = error?.code ?: code().toString()
                val message = error?.message ?: message()

                Timber.d("Response was not successful: $code - $body")
                onError(code, message)
            }
        }
    }

    @WorkerThread
    private fun getDataToSync(): SyncDto {
        val lastSync = db.lastUpdateDao.lastUpdate

        return SyncDto(
            db.lastUpdateDao.lastUpdateAsString,

            db.aisleDao.findModified(lastSync),
            db.unitDao.findModified(lastSync),
            db.tagDao.findModified(lastSync),
            db.ingredientDao.findModified(lastSync),
            db.recipeDao.findModified(lastSync),
            db.recipeIngredientDao.findModified(lastSync),
            db.recipeStepDao.findModified(lastSync),
            db.recipeTagDao.findModified(lastSync),

            db.deletedDao.findDeleted(lastSync, TABLE_AISLE),
            db.deletedDao.findDeleted(lastSync, TABLE_UNIT),
            db.deletedDao.findDeleted(lastSync, TABLE_TAG),
            db.deletedDao.findDeleted(lastSync, TABLE_INGREDIENT),
            db.deletedDao.findDeleted(lastSync, TABLE_RECIPE),
            db.deletedDao.findDeleted(lastSync, TABLE_RECIPE_INGREDIENT),
            db.deletedDao.findDeleted(lastSync, TABLE_RECIPE_STEP),
            db.deletedDao.findDeleted(lastSync, TABLE_RECIPE_TAG)
        )
    }

    @WorkerThread
    private fun syncLocalDb(response: SyncDto?) {
        response?.apply {
            db.recipeTagDao.deleteByIds(deletedRecipeTags)
            db.recipeStepDao.deleteByIds(deletedRecipeSteps)
            db.recipeIngredientDao.deleteByIds(deletedRecipeIngredients)
            db.recipeDao.deleteByIds(deletedRecipes)
            db.ingredientDao.deleteByIds(deletedIngredients)
            db.tagDao.deleteByIds(deletedTags)
            db.unitDao.deleteByIds(deletedUnits)
            db.aisleDao.deleteByIds(deletedAisles)

            db.aisleDao.upsert(aisles)
            db.unitDao.upsert(units)
            db.tagDao.upsert(tags)
            db.ingredientDao.upsert(ingredients)
            db.recipeDao.upsert(recipes)
            db.recipeIngredientDao.upsert(recipeIngredients)
            db.recipeStepDao.upsert(recipeSteps)
            db.recipeTagDao.upsert(recipeTags)

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                .parse(lastSync)
                .time

            // TODO convert string date to timestamp?
            db.lastUpdateDao.upsert(LastUpdate(timestamp))
            db.tagDao.update(db.tagUsageDao.tagsWithUpdatedUsage)
        }
    }

    @MainThread
    fun onSuccess(response: SyncDto?) {
        AppExecutors.diskIO { syncLocalDb(response) }
    }

    @MainThread
    fun onError(errorCode: String = "0", errorMessage: String = "Unknown error") {
        // TODO (2) either notify the user or log the error and try again later
        Timber.d("$errorCode: $errorMessage")
    }
}