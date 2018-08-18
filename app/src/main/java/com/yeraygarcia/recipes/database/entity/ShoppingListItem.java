package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.yeraygarcia.recipes.database.UUIDTypeConverter;

import java.util.UUID;

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

    @PrimaryKey
    @NonNull
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private UUID id;

    @ColumnInfo(name = "recipe_id", typeAffinity = ColumnInfo.BLOB)
    private UUID recipeId;

    @ColumnInfo(name = "ingredient_id", typeAffinity = ColumnInfo.BLOB)
    private UUID ingredientId;

    @NonNull
    private String name;

    protected Double quantity;

    @ColumnInfo(name = "unit_id", typeAffinity = ColumnInfo.BLOB)
    private UUID unitId;

    @NonNull
    @ColumnInfo(name = "sort_order")
    private Long sortOrder;

    @NonNull
    private Boolean completed;

    @NonNull
    private Boolean visible;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    public ShoppingListItem(UUID id, UUID recipeId, UUID ingredientId, @NonNull String name, Double quantity, UUID unitId, @NonNull Long sortOrder, @NonNull Boolean completed, @NonNull Boolean visible) {
        this.id = id == null ? UUIDTypeConverter.newUUID() : id;
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.name = name;
        this.quantity = quantity;
        this.unitId = unitId;
        this.sortOrder = sortOrder;
        this.completed = completed;
        this.visible = visible;
    }

    @Ignore
    public ShoppingListItem(UUID recipeId, UUID ingredientId, @NonNull String name, Double quantity, UUID unitId, @NonNull Long sortOrder, @NonNull Boolean completed, @NonNull Boolean visible) {
        this.id = UUIDTypeConverter.newUUID();
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.name = name;
        this.quantity = quantity;
        this.unitId = unitId;
        this.sortOrder = sortOrder;
        this.completed = completed;
        this.visible = visible;
    }

    @Ignore
    public ShoppingListItem(@NonNull String name) {
        this.id = UUIDTypeConverter.newUUID();
        this.name = name;
        this.sortOrder = 0L;
        this.completed = false;
        this.visible = true;
    }

    @Ignore
    public ShoppingListItem(@NonNull String name, Double quantity, UUID unitId) {
        this.id = UUIDTypeConverter.newUUID();
        this.name = name;
        this.quantity = quantity;
        this.unitId = unitId;
        this.sortOrder = 0L;
        this.completed = false;
        this.visible = true;
    }

    // Getters and Setters /////////////////////////////////////////////////////////////////////////

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    public UUID getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(UUID recipeId) {
        this.recipeId = recipeId;
    }

    public UUID getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(UUID ingredientId) {
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

    public UUID getUnitId() {
        return unitId;
    }

    public void setUnitId(UUID unitId) {
        this.unitId = unitId;
    }

    @NonNull
    public Long getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(@NonNull Long sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setCompleted(@NonNull Boolean completed) {
        this.completed = completed;
    }

    @NonNull
    public Boolean getCompleted() {
        return completed;
    }

    @NonNull
    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(@NonNull Boolean visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        return "ShoppingListItem{" +
                "id=" + id +
                ", recipeId=" + recipeId +
                ", ingredientId=" + ingredientId +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", unitId=" + unitId +
                ", sortOrder=" + sortOrder +
                ", completed=" + completed +
                ", visible=" + visible +
                '}';
    }
}
