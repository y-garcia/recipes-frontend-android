package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static android.arch.persistence.room.ForeignKey.RESTRICT;

@Entity(
        tableName = "shopping_list_item",
        indices = {
                @Index("recipe_id"),
                @Index("ingredient_id"),
                @Index("unit_id")
        },
        foreignKeys = {
                @ForeignKey(entity = Recipe.class, parentColumns = "id", childColumns = "recipe_id", onDelete = CASCADE),
                @ForeignKey(entity = Ingredient.class, parentColumns = "id", childColumns = "ingredient_id", onDelete = CASCADE),
                @ForeignKey(entity = Unit.class, parentColumns = "id", childColumns = "unit_id", onDelete = RESTRICT)
        }
)
public class ShoppingListItem {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "recipe_id")
    private Long recipeId;

    @ColumnInfo(name = "ingredient_id")
    private Long ingredientId;

    @NonNull
    private String name;

    protected Double quantity;

    @ColumnInfo(name = "unit_id")
    private Long unitId;

    @NonNull
    @ColumnInfo(name = "sort_order")
    private Long sortOrder;

    @NonNull
    private Boolean completed;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public ShoppingListItem(Long recipeId, Long ingredientId, @NonNull String name, Double quantity, Long unitId, @NonNull Long sortOrder, @NonNull Boolean completed) {
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.name = name;
        this.quantity = quantity;
        this.unitId = unitId;
        this.sortOrder = sortOrder;
        this.completed = completed;
    }

    public ShoppingListItem(long id, Long recipeId, Long ingredientId, @NonNull String name, Double quantity, Long unitId, @NonNull Long sortOrder, @NonNull Boolean completed) {
        this.id = id;
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.name = name;
        this.quantity = quantity;
        this.unitId = unitId;
        this.sortOrder = sortOrder;
        this.completed = completed;
    }

    // Getters and Setters /////////////////////////////////////////////////////////////////////////

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    @NonNull
    public Long getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(@NonNull Long sortOrder) {
        this.sortOrder = sortOrder;
    }

    @NonNull
    public Boolean isCompleted() {
        return completed;
    }

    public void setCompleted(@NonNull Boolean completed) {
        this.completed = completed;
    }
}
