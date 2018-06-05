package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.Nullable;

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

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "recipe_id")
    private long recipeId;

    @ColumnInfo(name = "ingredient_id")
    private long ingredientId;

    @Nullable
    private Double quantity;

    @Nullable
    @ColumnInfo(name = "unit_id")
    private Long unitId;

    @ColumnInfo(name = "sort_order")
    private int sortOrder;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public RecipeIngredient(long recipeId, long ingredientId, @Nullable Double quantity, @Nullable Long unitId, int sortOrder) {
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unitId = unitId;
        this.sortOrder = sortOrder;
    }

    public RecipeIngredient(long id, long recipeId, long ingredientId, @Nullable Double quantity, @Nullable Long unitId, int sortOrder) {
        this.id = id;
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unitId = unitId;
        this.sortOrder = sortOrder;
    }

    // Getters and Setters /////////////////////////////////////////////////////////////////////////

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(long ingredientId) {
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
    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(@Nullable Long unitId) {
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
        return sortOrder + ". " + quantity + " (unit: " + unitId + ") (ingredient: " + ingredientId + ")";
    }
}
