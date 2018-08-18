package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.yeraygarcia.recipes.database.UUIDTypeConverter;

import java.util.UUID;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static android.arch.persistence.room.ForeignKey.RESTRICT;

@Entity(
        tableName = "recipe_ingredient",
        indices = {
                @Index("recipe_id"),
                @Index("ingredient_id"),
                @Index("unit_id")
        },
        foreignKeys = {
                @ForeignKey(entity = Recipe.class, parentColumns = "id", childColumns = "recipe_id", onDelete = CASCADE),
                @ForeignKey(entity = Ingredient.class, parentColumns = "id", childColumns = "ingredient_id", onDelete = RESTRICT),
                @ForeignKey(entity = Unit.class, parentColumns = "id", childColumns = "unit_id", onDelete = RESTRICT)
        }
)
public class RecipeIngredient {

    @PrimaryKey
    @NonNull
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private UUID id;

    @NonNull
    @ColumnInfo(name = "recipe_id", typeAffinity = ColumnInfo.BLOB)
    @SerializedName("recipe_id")
    private UUID recipeId;

    @NonNull
    @ColumnInfo(name = "ingredient_id", typeAffinity = ColumnInfo.BLOB)
    @SerializedName("ingredient_id")
    private UUID ingredientId;

    @Nullable
    private Double quantity;

    @Nullable
    @ColumnInfo(name = "unit_id", typeAffinity = ColumnInfo.BLOB)
    @SerializedName("unit_id")
    private UUID unitId;

    @ColumnInfo(name = "sort_order")
    @SerializedName("sort_order")
    private int sortOrder;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public RecipeIngredient(@NonNull UUID recipeId, @NonNull UUID ingredientId, @Nullable Double quantity, @Nullable UUID unitId, int sortOrder) {
        this.id = UUIDTypeConverter.newUUID();
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unitId = unitId;
        this.sortOrder = sortOrder;
    }

    public RecipeIngredient(@NonNull UUID id, @NonNull UUID recipeId, @NonNull UUID ingredientId, @Nullable Double quantity, @Nullable UUID unitId, int sortOrder) {
        this.id = id;
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unitId = unitId;
        this.sortOrder = sortOrder;
    }

    // Getters and Setters /////////////////////////////////////////////////////////////////////////

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    @NonNull
    public UUID getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(@NonNull UUID recipeId) {
        this.recipeId = recipeId;
    }

    @NonNull
    public UUID getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(@NonNull UUID ingredientId) {
        this.ingredientId = ingredientId;
    }

    @Nullable
    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(@Nullable Double quantity) {
        this.quantity = quantity;
    }

    @Nullable
    public UUID getUnitId() {
        return unitId;
    }

    public void setUnitId(@Nullable UUID unitId) {
        this.unitId = unitId;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
