package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.yeraygarcia.recipes.database.UUIDTypeConverter;

import java.util.UUID;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = "recipe_step",
        indices = {
                @Index("recipe_id")
        },
        foreignKeys = {
                @ForeignKey(entity = Recipe.class, parentColumns = "id", childColumns = "recipe_id", onDelete = CASCADE)
        }
)
public class RecipeStep {

    @PrimaryKey
    @NonNull
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private UUID id;

    @NonNull
    @ColumnInfo(name = "recipe_id", typeAffinity = ColumnInfo.BLOB)
    @SerializedName("recipe_id")
    private UUID recipeId;

    private String description;

    @ColumnInfo(name = "is_section")
    @SerializedName("is_section")
    private boolean section;

    @ColumnInfo(name = "sort_order")
    @SerializedName("sort_order")
    private int sortOrder;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public RecipeStep(@NonNull UUID recipeId, String description, boolean section, int sortOrder) {
        this.id = UUIDTypeConverter.newUUID();
        this.recipeId = recipeId;
        this.description = description;
        this.section = section;
        this.sortOrder = sortOrder;
    }

    public RecipeStep(@NonNull UUID id, @NonNull UUID recipeId, String description, boolean section, int sortOrder) {
        this.id = id;
        this.recipeId = recipeId;
        this.description = description;
        this.section = section;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSection() {
        return section;
    }

    public void setSection(boolean section) {
        this.section = section;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public String toString() {
        return sortOrder + ". " + description;
    }
}
