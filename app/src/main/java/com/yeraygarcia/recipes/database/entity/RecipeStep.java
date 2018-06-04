package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(
        tableName = "recipe_step",
        indices = {
                @Index("recipe_id")
        },
        foreignKeys = {
                @ForeignKey(entity = Recipe.class, parentColumns = "id", childColumns = "recipe_id")
        }
)
public class RecipeStep {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "recipe_id")
    private long recipeId;

    private String description;

    @ColumnInfo(name = "is_section")
    private boolean section;

    @ColumnInfo(name = "sort_order")
    private int sortOrder;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public RecipeStep(long recipeId, String description, boolean section, int sortOrder) {
        this.recipeId = recipeId;
        this.description = description;
        this.section = section;
        this.sortOrder = sortOrder;
    }

    public RecipeStep(long id, long recipeId, String description, boolean section, int sortOrder) {
        this.id = id;
        this.recipeId = recipeId;
        this.description = description;
        this.section = section;
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
        return sortOrder + ". " + description + (section ? ":" : "");
    }
}
